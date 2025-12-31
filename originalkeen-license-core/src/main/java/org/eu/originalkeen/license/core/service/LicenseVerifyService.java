package org.eu.originalkeen.license.core.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public LicenseVerifyService(LicenseManagerAdapter licenseManager) {
        this.licenseManager = licenseManager;
    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Cache the timestamp of last successful verification
    private volatile long lastSuccessTimestamp = 0;
    // Cache duration 60 seconds
    private static final long CACHE_DURATION_MS = 60 * 1000L;
    // Async refresh flag
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    /**
     * Install the license
     *
     * @param licensePath path to license file
     */
    public synchronized void install(String licensePath) {
        log.info("Start installing License. Path: {}", licensePath);
        lock.writeLock().lock();
        try {
            log.info("Installing License...");
            licenseManager.uninstall();
            licenseManager.install(new File(licensePath));
            log.info("License installed successfully");
            // Reset cache after successful installation
            lastSuccessTimestamp = 0;
        } catch (Exception e) {
            log.error("License installation failed", e);
            // Optionally log current hardware info to help troubleshoot mismatches
            try {
                LicenseCheckModel currentHardware = licenseManager.getServerHardwareInfo();
                log.info("Installation failed, current server hardware info: {}", currentHardware);
            } catch (Exception ex) { /* ignore */ }
            throw new RuntimeException("License installation failed: " + e.getMessage(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Verify the license
     *
     * <p>This method caches successful verifications for a short duration to
     * reduce overhead. If verification fails, the failure is not cached and
     * will be retried on the next call.</p>
     *
     * <p>High concurrency support:
     * if a verification is in progress asynchronously, other threads will
     * optimistically assume success and avoid blocking.</p>
     *
     * @return true if license is valid, false otherwise
     */
    public boolean verify() {
        long now = System.currentTimeMillis();

        // Return cached success if within duration
        if (lastSuccessTimestamp > 0 && (now - lastSuccessTimestamp) < CACHE_DURATION_MS) {
            return true;
        }

        // Async refresh: if no refresh in progress, start one
        if (refreshing.compareAndSet(false, true)) {
            CompletableFuture.runAsync(() -> {
                lock.readLock().lock();
                try {
                    licenseManager.verify();
                    lastSuccessTimestamp = System.currentTimeMillis();
                } catch (Exception e) {
                    log.warn("License verification failed asynchronously: {}", e.getMessage());
                    lastSuccessTimestamp = 0;
                } finally {
                    lock.readLock().unlock();
                    refreshing.set(false);
                }
            });
            // Optimistically allow the request to pass
            return true;
        }

        // Normal synchronous verification (blocking)
        lock.readLock().lock();
        try {
            licenseManager.verify();
            lastSuccessTimestamp = now;
            return true;
        } catch (Exception e) {
            log.debug("License verification failed: {}", e.getMessage());
            lastSuccessTimestamp = 0;
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

}
