package org.eu.originalkeen.license.autoconfigure;

import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseWebFilterAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    LicenseAutoConfiguration.class,
                    LicenseWebFilterAutoConfiguration.class
            ));

    @Test
    void shouldBackOffCompletelyWhenLicenseIsDisabled() {
        contextRunner
                .withPropertyValues("originalkeen.license.enabled=false")
                .run(context -> {
                    assertTrue(context.getBeansOfType(LicenseFilter.class).isEmpty());
                    assertTrue(context.getBeansOfType(LicenseRuntime.class).isEmpty());
                });
    }

    @Test
    void shouldNotRegisterFilterWhenWebEnforcementIsDisabled() {
        contextRunner
                .withPropertyValues(
                        "originalkeen.license.enabled=true",
                        "originalkeen.license.web-enabled=false",
                        "originalkeen.license.subject=demo-subject",
                        "originalkeen.license.public-alias=publiccert",
                        "originalkeen.license.public-key-store-path=classpath:publicCerts.keystore",
                        "originalkeen.license.public-password=changeit1"
                )
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(LicenseRuntime.class).size());
                    assertTrue(context.getBeansOfType(LicenseFilter.class).isEmpty());
                });
    }
}
