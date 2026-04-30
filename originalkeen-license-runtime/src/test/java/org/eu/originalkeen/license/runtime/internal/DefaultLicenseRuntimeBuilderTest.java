package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.runtime.exception.LicenseConfigurationException;
import org.eu.originalkeen.license.runtime.internal.bootstrap.LicenseRuntimeBootstrap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultLicenseRuntimeBuilderTest {

    @Test
    void shouldNormalizeOptionalValuesAndApplyDefaults() {
        DefaultLicenseRuntimeBuilder builder = new DefaultLicenseRuntimeBuilder();

        builder.subject("  demo-subject  ");
        builder.licensePath("   ");
        builder.publicAlias("  publiccert  ");
        builder.publicKeyStorePath(" classpath:publicCerts.keystore ");
        builder.publicPassword("changeit1");
        builder.preferencesNodeName("   ");

        ResolvedRuntimeOptions options = builder.resolveOptions();

        assertEquals("demo-subject", options.getSubject());
        assertNull(options.getLicensePath());
        assertEquals("publiccert", options.getPublicAlias());
        assertEquals("classpath:publicCerts.keystore", options.getPublicKeyStorePath());
        assertNull(options.getRequestedPreferencesNodeName());
        assertEquals(LicenseRuntimeBootstrap.DEFAULT_PREFERENCES_NODE_NAME, options.getEffectivePreferencesNodeName());
        assertArrayEquals("changeit1".toCharArray(), options.getPublicPassword());
    }

    @Test
    void shouldDefensivelyCopyProvidedPasswordArray() {
        DefaultLicenseRuntimeBuilder builder = new DefaultLicenseRuntimeBuilder();
        char[] password = "changeit1".toCharArray();

        builder.subject("demo-subject")
                .publicAlias("publiccert")
                .publicKeyStorePath("classpath:publicCerts.keystore")
                .publicPassword(password);

        password[0] = 'X';

        ResolvedRuntimeOptions options = builder.resolveOptions();
        assertArrayEquals("changeit1".toCharArray(), options.getPublicPassword());
    }

    @Test
    void shouldRejectBlankClasspathResourceAfterPrefix() {
        DefaultLicenseRuntimeBuilder builder = new DefaultLicenseRuntimeBuilder();

        builder.subject("demo-subject")
                .publicAlias("publiccert")
                .publicKeyStorePath("classpath:   ")
                .publicPassword("changeit1");

        assertThrows(LicenseConfigurationException.class, builder::resolveOptions);
    }
}
