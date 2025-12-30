package org.eu.originalkeen.license.autoconfigure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

public class LicenseCheckListener implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger log = LogManager.getLogger(LicenseCheckListener.class);

    private final LicenseProperties licenseProperties;
    private final LicenseVerifyService licenseVerifyService;

    /**
     * Constructor
     *
     * @param licenseProperties    the license configuration properties
     * @param licenseVerifyService the license verification service
     */
    public LicenseCheckListener(
            LicenseProperties licenseProperties,
            LicenseVerifyService licenseVerifyService
    ) {
        this.licenseProperties = licenseProperties;
        this.licenseVerifyService = licenseVerifyService;
    }

    /**
     * Triggered when the Spring Boot application has started.
     * Attempts to install the license if a license path is configured.
     *
     * @param event the application started event
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        // If a license path is configured, install the license
        if (StringUtils.hasText(licenseProperties.getLicensePath())) {
            try {
                licenseVerifyService.install(licenseProperties.getLicensePath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // Warn if no license path is configured
            log.warn("License path not configured (license.license-path), skipping license installation");
        }
    }
}
