package org.eu.originalkeen.license.runtime;

/**
 * Sanitized immutable runtime configuration snapshot.
 */
public interface LicenseRuntimeConfig {

    String getSubject();

    String getLicensePath();

    String getPublicAlias();

    String getPublicKeyStorePath();

    boolean hasPublicPassword();

    String getRequestedPreferencesNodeName();

    String getEffectivePreferencesNodeName();

    String getRequestedHardwareProviderClassName();

    String getEffectiveHardwareProviderClassName();

    boolean isHotReloadEnabled();
}
