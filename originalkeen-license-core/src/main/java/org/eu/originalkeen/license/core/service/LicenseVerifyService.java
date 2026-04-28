package org.eu.originalkeen.license.core.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.io.File;
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

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Timestamp of the last successful verification.
     *
     * <p>The cache stores only successful checks. A failure always clears the
     * timestamp so the next caller performs a real verification attempt.</p>
     */
    private volatile long lastSuccessTimestamp = 0;

    /**
     * Cache duration for successful verifications.
     */
    private static final long CACHE_DURATION_MS = 60 * 1000L;

    public LicenseVerifyService(LicenseManagerAdapter licenseManager) {
        this.licenseManager = licenseManager;
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
            lastSuccessTimestamp = 0;
        } catch (Exception e) {
            log.error("License installation failed", e);

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
        long now = System.currentTimeMillis();
        if (isCacheValid(now)) {
            return true;
        }

        lock.readLock().lock();
        try {
            /*
             * Another thread may have refreshed the cache while this thread was waiting
             * for the lock, so check the timestamp again before calling the manager.
             */
            if (isCacheValid(System.currentTimeMillis())) {
                return true;
            }

            licenseManager.verify();
            lastSuccessTimestamp = System.currentTimeMillis();
            return true;
        } catch (Exception e) {
            log.debug("License verification failed: {}", e.getMessage());
            lastSuccessTimestamp = 0;
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isCacheValid(long now) {
        return lastSuccessTimestamp > 0 && (now - lastSuccessTimestamp) < CACHE_DURATION_MS;
    }
}
