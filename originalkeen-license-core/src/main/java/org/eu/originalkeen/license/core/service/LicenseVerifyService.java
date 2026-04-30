package org.eu.originalkeen.license.core.service;

import de.schlichtherle.license.LicenseContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.io.File;
import java.time.Instant;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@code LicenseVerifyService} provides a thread-safe facade for
 * license installation and runtime verification.
 *
 * <p>This service wraps {@link LicenseManagerAdapter} and adds:</p>
 * <ul>
 *   <li>Read-write locking to protect concurrent install and verify operations</li>
 *   <li>A short-term success cache to reduce verification overhead on hot paths</li>
 *   <li>Optional file-based hot reload when the configured license file changes</li>
 *   <li>Centralized logging and error handling</li>
 * </ul>
 *
 * <p>License verification results are cached only after a successful
 * verification. Failed verifications are never cached, ensuring that
 * recovery is attempted immediately once the environment is fixed.</p>
 *
 * <p>This service is designed to be used by infrastructure components
 * such as startup listeners, servlet filters, or schedulers, and does
 * not depend on any web or framework-specific APIs.</p>
 *
 * <p>All public operations are safe to be called concurrently.</p>
 *
 * @author Original Keen
 * @see LicenseManagerAdapter
 */
public class LicenseVerifyService {

    private static final Logger log = LogManager.getLogger(LicenseVerifyService.class);

    private final LicenseManagerAdapter licenseManager;

    private final Path configuredLicensePath;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Timestamp of the last successful verification.
     *
     * <p>The cache stores only successful checks. A failure always clears the
     * timestamp so the next caller performs a real verification attempt.</p>
     */
    private volatile long lastSuccessTimestamp = 0;

    /**
     * Last successfully verified license content.
     *
     * <p>This is used only for higher-level adapters that need expiry metadata
     * while still reusing the service's built-in cache behavior.</p>
     */
    private volatile LicenseContent lastSuccessfulContent;

    /**
     * Tracks the last observed file modification time to avoid unnecessary reload checks.
     */
    private volatile long lastKnownLicenseFileModified = 0;

    /**
     * Cache duration for successful verifications.
     */
    private static final long CACHE_DURATION_MS = 60 * 1000L;

    public LicenseVerifyService(LicenseManagerAdapter licenseManager) {
        this(licenseManager, null);
    }

    public LicenseVerifyService(LicenseManagerAdapter licenseManager, String licensePath) {
        this.licenseManager = licenseManager;
        this.configuredLicensePath = normalizeLicensePath(licensePath);
    }

    /**
     * Install the license.
     *
     * @param licensePath path to the license file
     */
    public synchronized void install(String licensePath) {
        log.info("Start installing License. Path: {}", licensePath);
        lock.writeLock().lock();
        try {
            log.info("Installing License...");
            licenseManager.uninstall();
            licenseManager.install(new File(licensePath));
            log.info("License installed successfully");

            // Clear the cache because the installed license content has changed.
            clearSuccessCache();
            updateKnownLastModified(Paths.get(licensePath));
        } catch (Exception e) {
            log.error("License installation failed", e);
            clearSuccessCache();

            // Log the current hardware snapshot to help diagnose binding mismatches.
            try {
                LicenseCheckModel currentHardware = licenseManager.getServerHardwareInfo();
                log.info("Installation failed, current server hardware info: {}", currentHardware);
            } catch (Exception ex) {
                log.debug("Failed to collect hardware information after installation failure", ex);
            }
            throw new RuntimeException("License installation failed: " + e.getMessage(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Verify the license.
     *
     * <p>This method caches successful verifications for a short duration to
     * reduce overhead. If verification fails, the failure is not cached and
     * will be retried on the next call.</p>
     *
     * @return {@code true} if the license is valid, otherwise {@code false}
     */
    public boolean verify() {
        return verifyDetailed().isValid();
    }

    /**
     * Verify the license and expose detailed outcome metadata.
     *
     * <p>This method preserves the existing verification behavior but returns
     * enough information for higher-level runtime adapters to build a structured
     * verification result.</p>
     *
     * @return detailed verification outcome
     */
    public CoreVerificationOutcome verifyDetailed() {
        long now = System.currentTimeMillis();

        if (shouldReloadConfiguredLicense()) {
            lock.writeLock().lock();
            try {
                LicenseContent reloadedContent = reloadConfiguredLicenseIfNeeded();
                if (reloadedContent != null) {
                    long successAt = System.currentTimeMillis();
                    rememberSuccessfulVerification(reloadedContent, successAt);
                    return CoreVerificationOutcome.success(
                            reloadedContent,
                            false,
                            true,
                            Instant.ofEpochMilli(successAt),
                            isConfiguredLicensePathPresent(),
                            isConfiguredLicenseFileReadable()
                    );
                }
            } catch (Exception e) {
                log.debug("License hot reload failed: {}", e.getMessage());
                clearSuccessCache();
                return CoreVerificationOutcome.failure(
                        e,
                        Instant.now(),
                        isConfiguredLicensePathPresent(),
                        isConfiguredLicenseFileReadable()
                );
            } finally {
                lock.writeLock().unlock();
            }
        }

        if (isCacheValid(now)) {
            return CoreVerificationOutcome.success(
                    lastSuccessfulContent,
                    true,
                    false,
                    Instant.ofEpochMilli(now),
                    isConfiguredLicensePathPresent(),
                    isConfiguredLicenseFileReadable()
            );
        }

        lock.readLock().lock();
        try {
            /*
             * Another thread may have refreshed the cache while this thread was waiting
             * for the lock, so check the timestamp again before calling the manager.
             */
            long recheckedNow = System.currentTimeMillis();
            if (isCacheValid(recheckedNow)) {
                return CoreVerificationOutcome.success(
                        lastSuccessfulContent,
                        true,
                        false,
                        Instant.ofEpochMilli(recheckedNow),
                        isConfiguredLicensePathPresent(),
                        isConfiguredLicenseFileReadable()
                );
            }

            LicenseContent content = licenseManager.verify();
            long successAt = System.currentTimeMillis();
            rememberSuccessfulVerification(content, successAt);
            return CoreVerificationOutcome.success(
                    content,
                    false,
                    false,
                    Instant.ofEpochMilli(successAt),
                    isConfiguredLicensePathPresent(),
                    isConfiguredLicenseFileReadable()
            );
        } catch (Exception e) {
            log.debug("License verification failed: {}", e.getMessage());
            clearSuccessCache();
            return CoreVerificationOutcome.failure(
                    e,
                    Instant.now(),
                    isConfiguredLicensePathPresent(),
                    isConfiguredLicenseFileReadable()
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    private LicenseContent reloadConfiguredLicenseIfNeeded() throws Exception {
        if (configuredLicensePath == null) {
            return null;
        }

        if (!hasReadableLicenseFile(configuredLicensePath)) {
            return null;
        }

        if (readLastModified(configuredLicensePath) == lastKnownLicenseFileModified) {
            return null;
        }

        LicenseContent reloaded = licenseManager.reloadIfNeeded(configuredLicensePath);
        if (reloaded != null) {
            updateKnownLastModified(configuredLicensePath);
            return reloaded;
        }

        updateKnownLastModified(configuredLicensePath);
        return null;
    }

    private boolean shouldReloadConfiguredLicense() {
        if (configuredLicensePath == null) {
            return false;
        }
        if (!hasReadableLicenseFile(configuredLicensePath)) {
            return false;
        }
        return readLastModified(configuredLicensePath) != lastKnownLicenseFileModified;
    }

    private void updateKnownLastModified(Path licensePath) {
        if (licensePath != null && hasReadableLicenseFile(licensePath)) {
            lastKnownLicenseFileModified = readLastModified(licensePath);
        }
    }

    private boolean hasReadableLicenseFile(Path licensePath) {
        File file = licensePath.toFile();
        return file.exists() && file.isFile() && file.canRead();
    }

    private long readLastModified(Path licensePath) {
        return licensePath.toFile().lastModified();
    }

    private Path normalizeLicensePath(String licensePath) {
        if (licensePath == null || licensePath.isBlank()) {
            return null;
        }
        return Paths.get(licensePath.trim());
    }

    private boolean isCacheValid(long now) {
        return lastSuccessTimestamp > 0 && (now - lastSuccessTimestamp) < CACHE_DURATION_MS;
    }

    private void rememberSuccessfulVerification(LicenseContent content, long timestamp) {
        lastSuccessfulContent = content;
        lastSuccessTimestamp = timestamp;
    }

    private void clearSuccessCache() {
        lastSuccessfulContent = null;
        lastSuccessTimestamp = 0;
    }

    private boolean isConfiguredLicensePathPresent() {
        return configuredLicensePath != null;
    }

    private boolean isConfiguredLicenseFileReadable() {
        return configuredLicensePath != null && hasReadableLicenseFile(configuredLicensePath);
    }
}
