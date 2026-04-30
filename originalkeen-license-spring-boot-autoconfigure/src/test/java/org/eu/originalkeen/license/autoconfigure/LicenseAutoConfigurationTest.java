package org.eu.originalkeen.license.autoconfigure;

import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    LicenseAutoConfiguration.class
            ));

    @Test
    void shouldNotCreateRuntimeBeansWhenLicenseIsDisabled() {
        contextRunner
                .withPropertyValues("originalkeen.license.enabled=false")
                .run(context -> {
                    assertTrue(context.getBeansOfType(LicenseRuntime.class).isEmpty());
                    assertTrue(context.getBeansOfType(LicenseVerifyService.class).isEmpty());
                });
    }

    @Test
    void shouldCreateRuntimeAndCompatibilityBeansWhenEnabledAndConfigured() {
        contextRunner
                .withPropertyValues(
                        "originalkeen.license.enabled=true",
                        "originalkeen.license.subject=demo-subject",
                        "originalkeen.license.public-alias=publiccert",
                        "originalkeen.license.public-key-store-path=classpath:publicCerts.keystore",
                        "originalkeen.license.public-password=changeit1"
                )
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(LicenseRuntime.class).size());
                    assertEquals(1, context.getBeansOfType(LicenseVerifyService.class).size());
                });
    }
}
