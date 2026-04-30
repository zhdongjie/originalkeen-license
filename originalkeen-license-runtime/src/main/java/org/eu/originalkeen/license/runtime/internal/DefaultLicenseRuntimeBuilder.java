package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseRuntimeBuilder;
import org.eu.originalkeen.license.runtime.exception.LicenseConfigurationException;
import org.eu.originalkeen.license.runtime.internal.bootstrap.LicenseRuntimeBootstrap;

import java.util.Arrays;

/**
 * Default builder implementation for {@link LicenseRuntime}.
 */
public final class DefaultLicenseRuntimeBuilder implements LicenseRuntimeBuilder {

    private String subject;
    private String licensePath;
    private String publicAlias;
    private String publicKeyStorePath;
    private char[] publicPassword;
    private HardwareDataProvider hardwareDataProvider;
    private String preferencesNodeName;

    @Override
    public LicenseRuntimeBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    @Override
    public LicenseRuntimeBuilder licensePath(String licensePath) {
        this.licensePath = licensePath;
        return this;
    }

    @Override
    public LicenseRuntimeBuilder publicAlias(String publicAlias) {
        this.publicAlias = publicAlias;
        return this;
    }

    @Override
    public LicenseRuntimeBuilder publicKeyStorePath(String publicKeyStorePath) {
        this.publicKeyStorePath = publicKeyStorePath;
        return this;
    }

    @Override
    public LicenseRuntimeBuilder publicPassword(String publicPassword) {
        this.publicPassword = publicPassword == null ? null : publicPassword.toCharArray();
        return this;
    }

    @Override
    public LicenseRuntimeBuilder publicPassword(char[] publicPassword) {
        this.publicPassword = publicPassword == null ? null : Arrays.copyOf(publicPassword, publicPassword.length);
        return this;
    }

    @Override
    public LicenseRuntimeBuilder hardwareDataProvider(HardwareDataProvider hardwareDataProvider) {
        this.hardwareDataProvider = hardwareDataProvider;
        return this;
    }

    @Override
    public LicenseRuntimeBuilder preferencesNodeName(String preferencesNodeName) {
        this.preferencesNodeName = preferencesNodeName;
        return this;
    }

    @Override
    public LicenseRuntime build() {
        return LicenseRuntimeBootstrap.assemble(this).getRuntime();
    }

    public ResolvedRuntimeOptions resolveOptions() {
        String normalizedSubject = trimToNull(subject);
        String normalizedLicensePath = trimToNull(licensePath);
        String normalizedPublicAlias = trimToNull(publicAlias);
        String normalizedPublicKeyStorePath = trimToNull(publicKeyStorePath);
        String normalizedRequestedPreferencesNodeName = trimToNull(preferencesNodeName);
        String effectivePreferencesNodeName = normalizedRequestedPreferencesNodeName == null
                ? LicenseRuntimeBootstrap.DEFAULT_PREFERENCES_NODE_NAME
                : normalizedRequestedPreferencesNodeName;

        if (normalizedSubject == null) {
            throw new LicenseConfigurationException("Runtime subject must not be blank");
        }
        if (normalizedPublicAlias == null) {
            throw new LicenseConfigurationException("Runtime publicAlias must not be blank");
        }
        if (normalizedPublicKeyStorePath == null) {
            throw new LicenseConfigurationException("Runtime publicKeyStorePath must not be blank");
        }
        if (publicPassword == null || publicPassword.length == 0) {
            throw new LicenseConfigurationException("Runtime publicPassword must not be blank");
        }
        if (normalizedPublicKeyStorePath.startsWith("classpath:")
                && trimToNull(normalizedPublicKeyStorePath.substring("classpath:".length())) == null) {
            throw new LicenseConfigurationException("Runtime classpath keystore path must not be blank");
        }

        return new ResolvedRuntimeOptions(
                normalizedSubject,
                normalizedLicensePath,
                normalizedPublicAlias,
                normalizedPublicKeyStorePath,
                publicPassword,
                hardwareDataProvider,
                normalizedRequestedPreferencesNodeName,
                effectivePreferencesNodeName,
                hardwareDataProvider == null ? null : hardwareDataProvider.getClass().getName()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
