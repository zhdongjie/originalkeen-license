package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.eu.originalkeen.license.runtime.LicenseRuntimeConfig;
import org.eu.originalkeen.license.runtime.exception.LicenseInstallationException;
import org.eu.originalkeen.license.runtime.internal.bootstrap.LicenseVerificationTranslator;
import org.eu.originalkeen.license.runtime.support.LicenseRuntimeTestSupport;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultLicenseRuntimeTest {

    @Test
    void installIfPresentShouldWrapInstallationFailureForReadableConfiguredFile() throws Exception {
        Path tempDir = LicenseRuntimeTestSupport.createWorkspaceTempDirectory("default-runtime-test-");
        try {
            Path licenseFile = Files.writeString(tempDir.resolve("broken-license.lic"), "broken-license");
            LicenseRuntimeConfig config = mock(LicenseRuntimeConfig.class);
            LicenseManagerAdapter licenseManager = mock(LicenseManagerAdapter.class);
            LicenseVerifyService verifyService = mock(LicenseVerifyService.class);

            when(config.getLicensePath()).thenReturn(licenseFile.toString());
            doThrow(new RuntimeException("Signature mismatch")).when(verifyService).install(licenseFile.toString());

            DefaultLicenseRuntime runtime = new DefaultLicenseRuntime(
                    config,
                    licenseManager,
                    verifyService,
                    new LicenseVerificationTranslator()
            );

            LicenseInstallationException exception = assertThrows(
                    LicenseInstallationException.class,
                    runtime::installIfPresent
            );

            assertEquals(
                    "License installation failed for path: " + licenseFile,
                    exception.getMessage()
            );
            assertEquals("Signature mismatch", exception.getCause().getMessage());
        } finally {
            LicenseRuntimeTestSupport.deleteRecursively(tempDir);
        }
    }
}
