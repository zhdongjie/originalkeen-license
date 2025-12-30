package org.eu.originalkeen.license.core.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class LicenseVerifyService {

    private static final Logger log = LogManager.getLogger(LicenseVerifyService.class);

    private final LicenseManagerAdapter licenseManager;

    public LicenseVerifyService(LicenseManagerAdapter licenseManager) {
        this.licenseManager = licenseManager;
    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Cache the timestamp of last successful verification
    private final AtomicLong lastSuccessTimestamp = new AtomicLong(0);
    // Cache duration 60 seconds
    private static final long CACHE_DURATION_MS = 60 * 1000L;

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
            lastSuccessTimestamp.set(0);
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
     * @return true if license is valid, false otherwise
     */
    public boolean verify() {
        long now = System.currentTimeMillis();
        long last = lastSuccessTimestamp.get();

        // 1. If within cache duration, return true (assuming last verification was successful)
        // Note: only successful verification is cached; failures are not cached
        if (last > 0 && (now - last) < CACHE_DURATION_MS) {
            return true;
        }

        lock.readLock().lock();
        // 2. Perform actual verification
        try {
            licenseManager.verify();
            // Verification succeeded, update timestamp
            lastSuccessTimestamp.set(now);
            return true;
        } catch (Exception e) {
            log.debug("License verification failed: {}", e.getMessage());
            // Verification failed, clear cache to ensure immediate retry next time
            lastSuccessTimestamp.set(0);
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

}
