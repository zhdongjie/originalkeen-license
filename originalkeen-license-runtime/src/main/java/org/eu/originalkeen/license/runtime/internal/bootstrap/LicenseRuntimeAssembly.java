package org.eu.originalkeen.license.runtime.internal.bootstrap;

import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseRuntimeConfig;

/**
 * Infrastructure-facing bundle returned by runtime bootstrap.
 */
public final class LicenseRuntimeAssembly {

    private final LicenseRuntime runtime;
    private final LicenseRuntimeConfig config;
    private final LicenseVerifyService verifyService;
    private final LicenseManagerAdapter licenseManager;
    private final HardwareDataProvider hardwareDataProvider;

    public LicenseRuntimeAssembly(
            LicenseRuntime runtime,
            LicenseRuntimeConfig config,
            LicenseVerifyService verifyService,
            LicenseManagerAdapter licenseManager,
            HardwareDataProvider hardwareDataProvider
    ) {
        this.runtime = runtime;
        this.config = config;
        this.verifyService = verifyService;
        this.licenseManager = licenseManager;
        this.hardwareDataProvider = hardwareDataProvider;
    }

    public LicenseRuntime getRuntime() {
        return runtime;
    }

    public LicenseRuntimeConfig getConfig() {
        return config;
    }

    public LicenseVerifyService getVerifyService() {
        return verifyService;
    }

    public LicenseManagerAdapter getLicenseManager() {
        return licenseManager;
    }

    public HardwareDataProvider getHardwareDataProvider() {
        return hardwareDataProvider;
    }
}
