package org.eu.originalkeen.license.runtime.internal.bootstrap;

import org.eu.originalkeen.license.runtime.exception.LicenseConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LicenseRuntimeBootstrapTest {

    @Test
    void shouldFailFastOnUnsupportedOperatingSystemWhenNoCustomProviderExists() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Plan9");
            assertThrows(LicenseConfigurationException.class, LicenseRuntimeBootstrap::createDefaultHardwareDataProvider);
        } finally {
            if (originalOsName == null) {
                System.clearProperty("os.name");
            } else {
                System.setProperty("os.name", originalOsName);
            }
        }
    }
}
