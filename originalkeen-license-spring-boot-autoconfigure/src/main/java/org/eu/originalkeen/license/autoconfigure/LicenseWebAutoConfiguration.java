package org.eu.originalkeen.license.autoconfigure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.autoconfigure.interceptor.LicenseInterceptor;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "web-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseWebAutoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LogManager.getLogger(LicenseWebAutoConfiguration.class);

    private final LicenseVerifyService licenseVerifyService;
    private final LicenseProperties licenseProperties;

    public LicenseWebAutoConfiguration(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties licenseProperties
    ) {
        this.licenseVerifyService = licenseVerifyService;
        this.licenseProperties = licenseProperties;
    }

    /**
     * Registers the LicenseInterceptor for web requests and applies exclusion paths.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(
                        new LicenseInterceptor(licenseVerifyService, licenseProperties)
                )
                .addPathPatterns("/**");

        if (licenseProperties.getExcludePaths() != null && !licenseProperties.getExcludePaths().isEmpty()) {
            registration.excludePathPatterns(licenseProperties.getExcludePaths());
            log.info("LicenseInterceptor exclusion paths: {}", licenseProperties.getExcludePaths());
        }

        log.info("LicenseInterceptor registered, intercepting path patterns: /**");
    }
}
