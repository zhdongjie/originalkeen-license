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

/**
 * {@code LicenseAutoConfiguration} provides the default beans
 * necessary to enable the Original Keen License system in a Spring Boot application.
 *
 * <p>All beans are conditional on missing beans, allowing users to
 * override any part of the license system by providing their own implementation.</p>
 *
 * <p>Beans provided include:</p>
 * <ul>
 *     <li>{@link HardwareDataProvider} – default OS-specific provider</li>
 *     <li>{@link LicenseParam} – license parameters configured from {@link LicenseProperties}</li>
 *     <li>{@link LicenseManagerAdapter} – encapsulates license creation, installation, and verification</li>
 *     <li>{@link LicenseVerifyService} – service API for license installation and verification</li>
 *     <li>{@link LicenseFilter} – web filter for HTTP request license enforcement</li>
 * </ul>
 *
 * <p>This configuration is automatically enabled when the application
 * is a Spring Boot application and {@link LicenseProperties} are
 * bound from configuration.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(LicenseProperties.class)
public class LicenseAutoConfiguration {

    /**
     * Provides a default {@link HardwareDataProvider} bean if none is defined.
     * The implementation is chosen based on the underlying OS.
     * Windows -> {@link WindowsHardwareProvider}, others -> {@link LinuxHardwareProvider}.
     *
     * @return OS-specific hardware data provider
     */
    @Bean
    @ConditionalOnMissingBean(HardwareDataProvider.class)
    public HardwareDataProvider hardwareDataProvider() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("windows") ? new WindowsHardwareProvider() : new LinuxHardwareProvider();
    }

    /**
     * Provides a default {@link LicenseParam} bean if none is defined.
     * Initializes the license parameters using {@link LicenseProperties}.
     *
     * @param properties License configuration properties
     * @return {@link LicenseParam} instance
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
     * Provides a default {@link LicenseManagerAdapter} bean if none is defined.
     * Binds {@link LicenseParam} and {@link HardwareDataProvider} together.
     *
     * @param licenseParam the license parameter
     * @param provider the hardware data provider
     * @return {@link LicenseManagerAdapter} instance
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
     * Provides a default {@link LicenseVerifyService} bean if none is defined.
     * Encapsulates license installation and verification logic.
     *
     * @param licenseManagerAdapter the license manager adapter
     * @return {@link LicenseVerifyService} instance
     */
    @Bean
    @ConditionalOnMissingBean(LicenseVerifyService.class)
    public LicenseVerifyService licenseVerifyService(LicenseManagerAdapter licenseManagerAdapter) {
        return new LicenseVerifyService(licenseManagerAdapter);
    }

    /**
     * Provides a {@link LicenseFilter} bean for web request license verification.
     * Users can override this filter by providing their own {@link LicenseFilter} bean.
     *
     * @param service the license verification service
     * @param properties license configuration properties
     * @return {@link LicenseFilter} instance
     */
    @Bean
    public LicenseFilter licenseFilter(LicenseVerifyService service, LicenseProperties properties) {
        return new LicenseFilter(service, properties);
    }

}
