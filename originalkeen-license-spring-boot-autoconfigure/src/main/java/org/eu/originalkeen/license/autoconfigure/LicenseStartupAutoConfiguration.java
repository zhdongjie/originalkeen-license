package org.eu.originalkeen.license.autoconfigure;

import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Startup auto-configuration that delegates optional install behavior to the V2 runtime.
 */
@AutoConfiguration(after = LicenseAutoConfiguration.class)
@ConditionalOnBean(LicenseRuntime.class)
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseStartupAutoConfiguration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LicenseStartupAutoConfiguration.class);

    private final LicenseRuntime licenseRuntime;
    private final LicenseProperties properties;

    public LicenseStartupAutoConfiguration(
            LicenseRuntime licenseRuntime,
            LicenseProperties properties
    ) {
        this.licenseRuntime = licenseRuntime;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("License verification is disabled (enabled=false), skipping license installation");
            return;
        }

        try {
            boolean installed = licenseRuntime.installIfPresent();
            if (installed) {
                log.info("License installed successfully through runtime startup flow");
            } else {
                log.warn("License installation skipped because no readable configured license file is currently available");
            }
        } catch (Exception e) {
            log.error("License installation failed", e);
            throw new RuntimeException("License installation failed: " + e.getMessage(), e);
        }
    }
}
