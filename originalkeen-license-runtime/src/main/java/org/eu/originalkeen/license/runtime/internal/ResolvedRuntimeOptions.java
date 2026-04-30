package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;

import java.util.Arrays;

/**
 * Normalized and validated runtime builder state.
 */
public final class ResolvedRuntimeOptions {

    private final String subject;
    private final String licensePath;
    private final String publicAlias;
    private final String publicKeyStorePath;
    private final char[] publicPassword;
    private final HardwareDataProvider hardwareDataProvider;
    private final String requestedPreferencesNodeName;
    private final String effectivePreferencesNodeName;
    private final String requestedHardwareProviderClassName;

    public ResolvedRuntimeOptions(
            String subject,
            String licensePath,
            String publicAlias,
            String publicKeyStorePath,
            char[] publicPassword,
            HardwareDataProvider hardwareDataProvider,
            String requestedPreferencesNodeName,
            String effectivePreferencesNodeName,
            String requestedHardwareProviderClassName
    ) {
        this.subject = subject;
        this.licensePath = licensePath;
        this.publicAlias = publicAlias;
        this.publicKeyStorePath = publicKeyStorePath;
        this.publicPassword = publicPassword == null ? null : Arrays.copyOf(publicPassword, publicPassword.length);
        this.hardwareDataProvider = hardwareDataProvider;
        this.requestedPreferencesNodeName = requestedPreferencesNodeName;
        this.effectivePreferencesNodeName = effectivePreferencesNodeName;
        this.requestedHardwareProviderClassName = requestedHardwareProviderClassName;
    }

    public String getSubject() {
        return subject;
    }

    public String getLicensePath() {
        return licensePath;
    }

    public String getPublicAlias() {
        return publicAlias;
    }

    public String getPublicKeyStorePath() {
        return publicKeyStorePath;
    }

    public char[] getPublicPassword() {
        return publicPassword == null ? null : Arrays.copyOf(publicPassword, publicPassword.length);
    }

    public String getPublicPasswordAsString() {
        return publicPassword == null ? null : new String(publicPassword);
    }

    public HardwareDataProvider getHardwareDataProvider() {
        return hardwareDataProvider;
    }

    public String getRequestedPreferencesNodeName() {
        return requestedPreferencesNodeName;
    }

    public String getEffectivePreferencesNodeName() {
        return effectivePreferencesNodeName;
    }

    public String getRequestedHardwareProviderClassName() {
        return requestedHardwareProviderClassName;
    }
}
