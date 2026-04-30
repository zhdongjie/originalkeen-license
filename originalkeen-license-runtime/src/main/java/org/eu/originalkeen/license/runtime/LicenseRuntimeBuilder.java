package org.eu.originalkeen.license.runtime;

import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;

/**
 * Builder for creating {@link LicenseRuntime} instances.
 */
public interface LicenseRuntimeBuilder {

    LicenseRuntimeBuilder subject(String subject);

    LicenseRuntimeBuilder licensePath(String licensePath);

    LicenseRuntimeBuilder publicAlias(String publicAlias);

    LicenseRuntimeBuilder publicKeyStorePath(String publicKeyStorePath);

    LicenseRuntimeBuilder publicPassword(String publicPassword);

    LicenseRuntimeBuilder publicPassword(char[] publicPassword);

    LicenseRuntimeBuilder hardwareDataProvider(HardwareDataProvider hardwareDataProvider);

    LicenseRuntimeBuilder preferencesNodeName(String preferencesNodeName);

    LicenseRuntime build();
}
