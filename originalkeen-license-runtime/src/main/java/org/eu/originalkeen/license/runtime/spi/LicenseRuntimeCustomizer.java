package org.eu.originalkeen.license.runtime.spi;

import org.eu.originalkeen.license.runtime.LicenseRuntimeBuilder;

/**
 * Hook for refining runtime creation before the builder is executed.
 */
public interface LicenseRuntimeCustomizer {

    void customize(LicenseRuntimeBuilder builder);
}
