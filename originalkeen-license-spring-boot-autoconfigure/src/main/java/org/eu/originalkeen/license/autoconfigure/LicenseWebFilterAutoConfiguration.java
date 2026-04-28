package org.eu.originalkeen.license.autoconfigure;

import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for registering {@link LicenseFilter} in a servlet-based
 * Spring Web application.
 *
 * <p>This configuration is activated only when:</p>
 * <ul>
 *   <li>The application is a servlet web application.</li>
 *   <li>{@code originalkeen.license.web-enabled=true}, or the property is absent.</li>
 * </ul>
 */
@AutoConfiguration(after = LicenseAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "web-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseWebFilterAutoConfiguration {

    /**
     * Creates the {@link LicenseFilter} bean only in web applications.
     *
     * @param licenseVerifyService the service used to verify license validity
     * @param licenseProperties configuration properties controlling filter behavior
     * @return a new {@link LicenseFilter} instance
     */
    @Bean
    @ConditionalOnMissingBean(LicenseFilter.class)
    public LicenseFilter licenseFilter(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties licenseProperties
    ) {
        return new LicenseFilter(licenseVerifyService, licenseProperties);
    }

    /**
     * Registers the {@link LicenseFilter} with the servlet container.
     *
     * @param licenseFilter the filter instance to register
     * @return the configured {@link FilterRegistrationBean}
     */
    @Bean(name = "licenseFilterRegistration")
    @ConditionalOnMissingBean(name = "licenseFilterRegistration")
    public FilterRegistrationBean<LicenseFilter> licenseFilterRegistration(
            LicenseFilter licenseFilter
    ) {
        FilterRegistrationBean<LicenseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(licenseFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }
}
