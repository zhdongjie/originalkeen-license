package org.eu.originalkeen.license.autoconfigure;

import jakarta.servlet.Filter;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for registering the runtime-backed servlet filter.
 */
@AutoConfiguration(after = LicenseAutoConfiguration.class)
@ConditionalOnClass({Filter.class, FilterRegistrationBean.class})
@ConditionalOnBean(LicenseRuntime.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "web-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseWebFilterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LicenseFilter.class)
    public LicenseFilter licenseFilter(
            LicenseRuntime licenseRuntime,
            LicenseProperties licenseProperties
    ) {
        return new LicenseFilter(licenseRuntime, licenseProperties);
    }

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
