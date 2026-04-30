package org.eu.originalkeen.license.runtime;

import org.eu.originalkeen.license.model.LicenseCheckModel;
import org.eu.originalkeen.license.runtime.internal.DefaultLicenseRuntimeBuilder;

/**
 * Main public runtime entry point for install, verify, and hardware inspection operations.
 */
public interface LicenseRuntime {

    static LicenseRuntimeBuilder builder() {
        return new DefaultLicenseRuntimeBuilder();
    }

    LicenseRuntimeConfig config();

    LicenseCheckModel currentHardwareInfo();

    void install(String licensePath);

    boolean installIfPresent();

    LicenseVerificationResult verify();

    void verifyOrThrow();

    default boolean isValid() {
        return verify().isValid();
    }
}
