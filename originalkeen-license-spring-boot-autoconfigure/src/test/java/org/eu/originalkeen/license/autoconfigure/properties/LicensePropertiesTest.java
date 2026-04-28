package org.eu.originalkeen.license.autoconfigure.properties;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicensePropertiesTest {

    @Test
    void shouldAllowMissingLicensePathWhenOtherVerificationSettingsExist() {
        LicenseProperties properties = createEnabledProperties();

        properties.afterPropertiesSet();

        assertTrue(properties.getExcludePaths().contains("/actuator/**"));
    }

    @Test
    void shouldMergeCustomAndDefaultExcludePathsWithoutDuplicates() {
        LicenseProperties properties = createEnabledProperties();
        properties.setExcludePaths(new ArrayList<>(List.of("/custom/**", "/actuator/**")));

        properties.afterPropertiesSet();

        assertTrue(properties.getExcludePaths().contains("/custom/**"));
        assertEquals(1, properties.getExcludePaths().stream().filter("/actuator/**"::equals).count());
    }

    private LicenseProperties createEnabledProperties() {
        LicenseProperties properties = new LicenseProperties();
        properties.setEnabled(true);
        properties.setSubject("demo-subject");
        properties.setPublicAlias("publiccert");
        properties.setPublicKeyStorePath("classpath:publicCerts.keystore");
        properties.setPublicPassword("changeit");
        return properties;
    }
}
