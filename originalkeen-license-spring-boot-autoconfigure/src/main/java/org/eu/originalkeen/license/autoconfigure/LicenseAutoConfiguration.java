package org.eu.originalkeen.license.autoconfigure;

import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseRuntimeBuilder;
import org.eu.originalkeen.license.runtime.internal.bootstrap.LicenseRuntimeAssembly;
import org.eu.originalkeen.license.runtime.internal.bootstrap.LicenseRuntimeBootstrap;
import org.eu.originalkeen.license.runtime.spi.LicenseRuntimeCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring auto-configuration for the V2 runtime-backed license stack.
 */
@AutoConfiguration
@EnableConfigurationProperties(LicenseProperties.class)
@ConditionalOnProperty(
        prefix = "originalkeen.license",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseAutoConfiguration {

    /**
     * Provides a default hardware provider bean if none is defined.
     */
    @Bean
    @ConditionalOnMissingBean(HardwareDataProvider.class)
    public HardwareDataProvider hardwareDataProvider() {
        return LicenseRuntimeBootstrap.createDefaultHardwareDataProvider();
    }

    /**
     * Builds a shared runtime assembly from properties, provider beans, and customizers.
     */
    @Bean
    @ConditionalOnMissingBean({LicenseRuntime.class, LicenseRuntimeAssembly.class})
    public LicenseRuntimeAssembly licenseRuntimeAssembly(
            LicenseProperties properties,
            ObjectProvider<HardwareDataProvider> hardwareDataProvider,
            ObjectProvider<LicenseRuntimeCustomizer> customizers
    ) {
        LicenseRuntimeBuilder builder = LicenseRuntime.builder()
                .subject(properties.getSubject())
                .licensePath(properties.getLicensePath())
                .publicAlias(properties.getPublicAlias())
                .publicKeyStorePath(properties.getPublicKeyStorePath())
                .publicPassword(properties.getPublicPassword());

        HardwareDataProvider provider = hardwareDataProvider.getIfAvailable();
        if (provider != null) {
            builder.hardwareDataProvider(provider);
        }

        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return LicenseRuntimeBootstrap.assemble(builder);
    }

    /**
     * Exposes the primary V2 runtime bean.
     */
    @Bean
    @ConditionalOnMissingBean(LicenseRuntime.class)
    public LicenseRuntime licenseRuntime(LicenseRuntimeAssembly assembly) {
        return assembly.getRuntime();
    }

    /**
     * Keeps the core verification service available as a compatibility bean.
     */
    @Bean
    @ConditionalOnMissingBean(LicenseVerifyService.class)
    public LicenseVerifyService licenseVerifyService(LicenseRuntimeAssembly assembly) {
        return assembly.getVerifyService();
    }

    /**
     * Exposes the assembled manager as an advanced bean for diagnostic scenarios.
     */
    @Bean
    @ConditionalOnMissingBean(LicenseManagerAdapter.class)
    public LicenseManagerAdapter licenseManagerAdapter(LicenseRuntimeAssembly assembly) {
        return assembly.getLicenseManager();
    }
}
