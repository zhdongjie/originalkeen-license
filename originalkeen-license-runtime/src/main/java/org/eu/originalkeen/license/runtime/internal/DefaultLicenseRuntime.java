package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.eu.originalkeen.license.model.LicenseCheckModel;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseRuntimeConfig;
import org.eu.originalkeen.license.runtime.LicenseVerificationResult;
import org.eu.originalkeen.license.runtime.internal.bootstrap.LicenseVerificationTranslator;

import java.io.File;

/**
 * Default runtime implementation that wraps the existing core verification stack.
 */
public final class DefaultLicenseRuntime implements LicenseRuntime {

    private final LicenseRuntimeConfig config;
    private final LicenseManagerAdapter licenseManager;
    private final LicenseVerifyService verifyService;
    private final LicenseVerificationTranslator translator;

    public DefaultLicenseRuntime(
            LicenseRuntimeConfig config,
            LicenseManagerAdapter licenseManager,
            LicenseVerifyService verifyService,
            LicenseVerificationTranslator translator
    ) {
        this.config = config;
        this.licenseManager = licenseManager;
        this.verifyService = verifyService;
        this.translator = translator;
    }

    @Override
    public LicenseRuntimeConfig config() {
        return config;
    }

    @Override
    public LicenseCheckModel currentHardwareInfo() {
        return licenseManager.getServerHardwareInfo();
    }

    @Override
    public void install(String licensePath) {
        try {
            verifyService.install(licensePath);
        } catch (RuntimeException ex) {
            throw translator.toInstallationException(licensePath, ex);
        }
    }

    @Override
    public boolean installIfPresent() {
        String configuredLicensePath = config.getLicensePath();
        if (configuredLicensePath == null) {
            return false;
        }

        File file = new File(configuredLicensePath);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return false;
        }

        install(configuredLicensePath);
        return true;
    }

    @Override
    public LicenseVerificationResult verify() {
        return translator.toResult(verifyService.verifyDetailed());
    }

    @Override
    public void verifyOrThrow() {
        LicenseVerificationResult result = verify();
        if (!result.isValid()) {
            throw translator.toVerificationException(result);
        }
    }
}
