package org.eu.originalkeen.license.autoconfigure;

import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Autoconfiguration for registering the {@link LicenseFilter} in a Servlet-based
 * Spring Web application.
 *
 * <p>This configuration is only activated when:</p>
 * <ul>
 *   <li>The application is a Servlet web application.</li>
 *   <li>The property {@code originalkeen.license.web-enabled} is set to {@code true}
 *       or is missing (defaults to enabled).</li>
 * </ul>
 *
 * <p>The filter is registered with a high precedence to ensure that license
 * verification happens early in the request processing pipeline.</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "web-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseWebFilterAutoConfiguration {

    /**
     * Creates the {@link LicenseFilter} bean.
     *
     * <p>The filter depends on {@link LicenseVerifyService} to perform the actual
     * license validation logic and {@link LicenseProperties} to determine runtime
     * behavior such as enablement and excluded paths.</p>
     *
     * @param licenseVerifyService the service used to verify license validity
     * @param licenseProperties the configuration properties controlling license behavior
     * @return a new {@link LicenseFilter} instance
     */
    @Bean
    public LicenseFilter licenseFilter(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties licenseProperties
    ) {
        return new LicenseFilter(licenseVerifyService, licenseProperties);
    }

    /**
     * Registers the {@link LicenseFilter} with the Servlet container.
     *
     * <p>The filter is applied to all URL patterns ({@code /*}) and is ordered
     * with high precedence to ensure license checks are performed before most
     * application-level filters.</p>
     *
     * <p>Using an explicit {@link FilterRegistrationBean} avoids relying solely
     * on component scanning and gives precise control over filter ordering.</p>
     *
     * @param licenseFilter the filter instance to register
     * @return the configured {@link FilterRegistrationBean}
     */
    @Bean
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
