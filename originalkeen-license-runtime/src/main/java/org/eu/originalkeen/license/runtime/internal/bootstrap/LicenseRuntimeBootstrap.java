package org.eu.originalkeen.license.runtime.internal.bootstrap;

import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.LicenseParam;
import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.core.hardware.LinuxHardwareProvider;
import org.eu.originalkeen.license.core.hardware.WindowsHardwareProvider;
import org.eu.originalkeen.license.core.keystore.FileKeyStoreParam;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseRuntimeBuilder;
import org.eu.originalkeen.license.runtime.LicenseRuntimeConfig;
import org.eu.originalkeen.license.runtime.exception.LicenseConfigurationException;
import org.eu.originalkeen.license.runtime.internal.DefaultLicenseRuntime;
import org.eu.originalkeen.license.runtime.internal.DefaultLicenseRuntimeBuilder;
import org.eu.originalkeen.license.runtime.internal.DefaultLicenseRuntimeConfig;
import org.eu.originalkeen.license.runtime.internal.ResolvedRuntimeOptions;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Shared bootstrap logic for building the V2 runtime on top of current core classes.
 */
public final class LicenseRuntimeBootstrap {

    public static final String DEFAULT_PREFERENCES_NODE_NAME = "/org/eu/originalkeen/license/runtime";

    private LicenseRuntimeBootstrap() {
    }

    public static LicenseRuntimeAssembly assemble(LicenseRuntimeBuilder builder) {
        if (!(builder instanceof DefaultLicenseRuntimeBuilder defaultBuilder)) {
            throw new LicenseConfigurationException(
                    "Unsupported LicenseRuntimeBuilder implementation: " + builder.getClass().getName()
            );
        }
        return assemble(defaultBuilder.resolveOptions());
    }

    public static LicenseRuntimeAssembly assemble(ResolvedRuntimeOptions options) {
        HardwareDataProvider provider = options.getHardwareDataProvider();
        if (provider == null) {
            provider = createDefaultHardwareDataProvider();
        }

        Preferences preferences = Preferences.userRoot().node(options.getEffectivePreferencesNodeName());
        String publicPassword = options.getPublicPasswordAsString();

        FileKeyStoreParam publicStoreParam = new FileKeyStoreParam(
                DefaultLicenseRuntime.class,
                options.getPublicKeyStorePath(),
                options.getPublicAlias(),
                publicPassword,
                null
        );

        LicenseParam licenseParam = new DefaultLicenseParam(
                options.getSubject(),
                preferences,
                publicStoreParam,
                new DefaultCipherParam(publicPassword)
        );

        LicenseManagerAdapter licenseManager = new LicenseManagerAdapter(licenseParam, provider);
        LicenseVerifyService verifyService = new LicenseVerifyService(licenseManager, options.getLicensePath());
        LicenseRuntimeConfig config = DefaultLicenseRuntimeConfig.from(options, provider);
        LicenseVerificationTranslator translator = new LicenseVerificationTranslator();
        LicenseRuntime runtime = new DefaultLicenseRuntime(config, licenseManager, verifyService, translator);

        return new LicenseRuntimeAssembly(runtime, config, verifyService, licenseManager, provider);
    }

    public static HardwareDataProvider createDefaultHardwareDataProvider() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.startsWith("windows")) {
            return new WindowsHardwareProvider();
        }
        if (osName.startsWith("linux")) {
            return new LinuxHardwareProvider();
        }
        throw new LicenseConfigurationException(
                "Unsupported operating system for built-in hardware provider resolution: " + osName
        );
    }
}
