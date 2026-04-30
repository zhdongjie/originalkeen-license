package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.runtime.LicenseRuntimeConfig;

/**
 * Default sanitized runtime configuration view.
 */
public final class DefaultLicenseRuntimeConfig implements LicenseRuntimeConfig {

    private final String subject;
    private final String licensePath;
    private final String publicAlias;
    private final String publicKeyStorePath;
    private final boolean hasPublicPassword;
    private final String requestedPreferencesNodeName;
    private final String effectivePreferencesNodeName;
    private final String requestedHardwareProviderClassName;
    private final String effectiveHardwareProviderClassName;
    private final boolean hotReloadEnabled;

    private DefaultLicenseRuntimeConfig(
            String subject,
            String licensePath,
            String publicAlias,
            String publicKeyStorePath,
            boolean hasPublicPassword,
            String requestedPreferencesNodeName,
            String effectivePreferencesNodeName,
            String requestedHardwareProviderClassName,
            String effectiveHardwareProviderClassName,
            boolean hotReloadEnabled
    ) {
        this.subject = subject;
        this.licensePath = licensePath;
        this.publicAlias = publicAlias;
        this.publicKeyStorePath = publicKeyStorePath;
        this.hasPublicPassword = hasPublicPassword;
        this.requestedPreferencesNodeName = requestedPreferencesNodeName;
        this.effectivePreferencesNodeName = effectivePreferencesNodeName;
        this.requestedHardwareProviderClassName = requestedHardwareProviderClassName;
        this.effectiveHardwareProviderClassName = effectiveHardwareProviderClassName;
        this.hotReloadEnabled = hotReloadEnabled;
    }

    public static DefaultLicenseRuntimeConfig from(
            ResolvedRuntimeOptions options,
            HardwareDataProvider provider
    ) {
        return new DefaultLicenseRuntimeConfig(
                options.getSubject(),
                options.getLicensePath(),
                options.getPublicAlias(),
                options.getPublicKeyStorePath(),
                options.getPublicPassword() != null && options.getPublicPassword().length > 0,
                options.getRequestedPreferencesNodeName(),
                options.getEffectivePreferencesNodeName(),
                options.getRequestedHardwareProviderClassName(),
                provider.getClass().getName(),
                options.getLicensePath() != null
        );
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getLicensePath() {
        return licensePath;
    }

    @Override
    public String getPublicAlias() {
        return publicAlias;
    }

    @Override
    public String getPublicKeyStorePath() {
        return publicKeyStorePath;
    }

    @Override
    public boolean hasPublicPassword() {
        return hasPublicPassword;
    }

    @Override
    public String getRequestedPreferencesNodeName() {
        return requestedPreferencesNodeName;
    }

    @Override
    public String getEffectivePreferencesNodeName() {
        return effectivePreferencesNodeName;
    }

    @Override
    public String getRequestedHardwareProviderClassName() {
        return requestedHardwareProviderClassName;
    }

    @Override
    public String getEffectiveHardwareProviderClassName() {
        return effectiveHardwareProviderClassName;
    }

    @Override
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }
}
