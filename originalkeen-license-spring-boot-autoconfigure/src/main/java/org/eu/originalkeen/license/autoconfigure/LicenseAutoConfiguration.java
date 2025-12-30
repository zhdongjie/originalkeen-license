package org.eu.originalkeen.license.autoconfigure;

import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.LicenseParam;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.core.hardware.LinuxHardwareProvider;
import org.eu.originalkeen.license.core.hardware.WindowsHardwareProvider;
import org.eu.originalkeen.license.core.keystore.FileKeyStoreParam;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.prefs.Preferences;

@AutoConfiguration
@EnableConfigurationProperties(LicenseProperties.class)
public class LicenseAutoConfiguration {

    /**
     * Provide a default HardwareDataProvider bean if none is defined.
     * Chooses Windows or Linux implementation based on the OS.
     */
    @Bean
    @ConditionalOnMissingBean(HardwareDataProvider.class)
    public HardwareDataProvider hardwareDataProvider() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("windows") ? new WindowsHardwareProvider() : new LinuxHardwareProvider();
    }

    /**
     * Provide a default LicenseParam bean if none is defined.
     * Initializes the license parameters using LicenseProperties.
     *
     * @param properties License configuration properties
     * @return LicenseParam instance
     */
    @Bean
    @ConditionalOnMissingBean(LicenseParam.class)
    public LicenseParam licenseParam(LicenseProperties properties) {
        Preferences preferences = Preferences.userNodeForPackage(LicenseVerifyService.class);

        // Define the public key store parameter
        FileKeyStoreParam publicStoreParam = new FileKeyStoreParam(
                LicenseVerifyService.class,
                properties.getPublicKeyStorePath(),
                properties.getPublicAlias(),
                properties.getPublicPassword(),
                null
        );

        // Create default license parameter
        return new DefaultLicenseParam(
                properties.getSubject(),
                preferences,
                publicStoreParam,
                new DefaultCipherParam(properties.getPublicPassword())
        );
    }

    /**
     * Provide a default LicenseManagerAdapter bean if none is defined.
     * Binds the LicenseParam and HardwareDataProvider together.
     *
     * @param licenseParam the license parameter
     * @param provider     the hardware data provider
     * @return LicenseManagerAdapter instance
     */
    @Bean
    @ConditionalOnMissingBean(LicenseManagerAdapter.class)
    public LicenseManagerAdapter licenseManagerAdapter(
            LicenseParam licenseParam,
            HardwareDataProvider provider
    ) {
        return new LicenseManagerAdapter(licenseParam, provider);
    }

    /**
     * Provide a default LicenseVerifyService bean if none is defined.
     * Encapsulates license installation and verification logic.
     *
     * @param licenseManagerAdapter the license manager adapter
     * @return LicenseVerifyService instance
     */
    @Bean
    @ConditionalOnMissingBean(LicenseVerifyService.class)
    public LicenseVerifyService licenseVerifyService(LicenseManagerAdapter licenseManagerAdapter) {
        return new LicenseVerifyService(licenseManagerAdapter);
    }

    /**
     * Register a LicenseCheckListener bean to automatically install the License file
     * when the Spring Boot application has started.
     *
     * <p>This listener will be triggered after the application context is refreshed
     * and the application has started, checking if a license path is configured.
     * If a license path is set, it installs the license using LicenseVerifyService.
     *
     * @param properties the LicenseProperties configuration, containing license path and settings
     * @param service the LicenseVerifyService used to install the license
     * @return a LicenseCheckListener instance that listens for ApplicationStartedEvent
     */
    @Bean
    public LicenseCheckListener licenseCheckListener(LicenseProperties properties, LicenseVerifyService service) {
        return new LicenseCheckListener(properties, service);
    }


}
