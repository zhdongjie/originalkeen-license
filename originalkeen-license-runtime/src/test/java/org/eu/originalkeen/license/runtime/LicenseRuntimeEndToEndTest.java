package org.eu.originalkeen.license.runtime;

import org.eu.originalkeen.license.model.LicenseCheckModel;
import org.eu.originalkeen.license.runtime.support.LicenseRuntimeTestSupport;
import org.eu.originalkeen.license.runtime.support.LicenseRuntimeTestSupport.LicenseRuntimeFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseRuntimeEndToEndTest {

    @Test
    void shouldInstallAndVerifyARealLicenseArtifact() throws Exception {
        Path tempDir = LicenseRuntimeTestSupport.createWorkspaceTempDirectory("runtime-end-to-end-");
        try {
            try (LicenseRuntimeFixture fixture = LicenseRuntimeTestSupport.createFixture(tempDir)) {
                LicenseRuntime runtime = fixture.createRuntime();

                LicenseVerificationResult first = runtime.verify();
                assertTrue(first.isValid(), () -> "first verification failed: " + first.getFailureCode() + " / " + first.getMessage());
                assertNull(first.getFailureCode());
                assertNull(first.getMismatchType());
                assertFalse(first.isFromCache());
                assertTrue(first.isReloaded());
                assertNotNull(first.getCheckedAt());
                assertNotNull(first.getExpiresAt());
                assertNotNull(first.getDaysRemaining());

                runtime.verifyOrThrow();

                LicenseVerificationResult cached = runtime.verify();
                assertTrue(cached.isValid());
                assertTrue(cached.isFromCache());

                LicenseCheckModel currentHardware = runtime.currentHardwareInfo();
                assertNotNull(currentHardware);
                assertEquals(fixture.getExpectedHardware().getCpuSerial(), currentHardware.getCpuSerial());
                assertEquals(fixture.getExpectedHardware().getMainBoardSerial(), currentHardware.getMainBoardSerial());
            }
        } finally {
            LicenseRuntimeTestSupport.deleteRecursively(tempDir);
        }
    }
}
