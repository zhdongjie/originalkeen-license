package org.eu.originalkeen.license.autoconfigure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;

@AutoConfiguration(after = LicenseAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseStartupAutoConfiguration implements ApplicationRunner {

    private static final Logger log = LogManager.getLogger(LicenseStartupAutoConfiguration.class);

    private final LicenseVerifyService licenseVerifyService;
    private final LicenseProperties properties;

    public LicenseStartupAutoConfiguration(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties properties
    ) {
        this.licenseVerifyService = licenseVerifyService;
        this.properties = properties;
    }

    /**
     * Runs on application startup and attempts to install the license if enabled.
     *
     * @param args Application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("License verification is disabled (enabled=false), skipping license installation");
            return;
        }

        String licensePath = properties.getLicensePath();
        if (!StringUtils.hasText(licensePath)) {
            log.warn("License path not configured (originalkeen.license.license-path), skipping installation");
            return;
        }

        try {
            log.info("Starting license installation, path: {}", licensePath);
            licenseVerifyService.install(licensePath);
            log.info("License installed successfully");
        } catch (Exception e) {
            log.error("License installation failed", e);
            throw new RuntimeException("License installation failed: " + e.getMessage(), e);
        }
    }
}
