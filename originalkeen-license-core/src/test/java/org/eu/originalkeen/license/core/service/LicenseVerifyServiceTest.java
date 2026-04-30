package org.eu.originalkeen.license.core.service;

import de.schlichtherle.license.LicenseContent;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LicenseVerifyServiceTest {

    @Test
    void shouldUseCachedSuccessUntilCacheExpires() throws Exception {
        LicenseManagerAdapter licenseManager = mock(LicenseManagerAdapter.class);
        LicenseVerifyService service = new LicenseVerifyService(licenseManager);
        LicenseContent verifiedContent = new LicenseContent();
        verifiedContent.setNotAfter(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)));

        when(licenseManager.verify())
                .thenReturn(verifiedContent)
                .thenThrow(new IllegalStateException("license expired"));

        CoreVerificationOutcome first = service.verifyDetailed();
        assertTrue(first.isValid(), "The first verification should delegate to the license manager.");
        assertFalse(first.isFromCache());
        assertFalse(first.isReloaded());
        assertNotNull(first.getContent());
        assertNotNull(first.getCheckedAt());
        assertFalse(first.isConfiguredLicensePathPresent());
        assertFalse(first.isConfiguredLicenseFileReadable());

        CoreVerificationOutcome cached = service.verifyDetailed();
        assertTrue(cached.isValid(), "The second verification should reuse the short-lived success cache.");
        assertTrue(cached.isFromCache());
        assertFalse(cached.isReloaded());
        verify(licenseManager, times(1)).verify();

        expireVerificationCache(service);

        CoreVerificationOutcome failed = service.verifyDetailed();
        assertFalse(failed.isValid(), "Once the cache expires, the service must perform a real verification again.");
        assertNotNull(failed.getFailure());
        assertNull(failed.getContent());
        assertFalse(failed.isFromCache());
        assertFalse(failed.isReloaded());
        verify(licenseManager, times(2)).verify();
    }

    @Test
    void shouldHotReloadBeforeUsingVerificationCache() throws Exception {
        LicenseManagerAdapter licenseManager = mock(LicenseManagerAdapter.class);
        Path licenseFile = Files.createTempFile("license-verify-service", ".lic");

        try {
            Files.writeString(licenseFile, "license-v1", StandardCharsets.UTF_8);
            Files.setLastModifiedTime(licenseFile, FileTime.fromMillis(System.currentTimeMillis()));

            LicenseVerifyService fileBoundService = new LicenseVerifyService(licenseManager, licenseFile.toString());

            when(licenseManager.reloadIfNeeded(any())).thenReturn(new LicenseContent());

            CoreVerificationOutcome reloaded = fileBoundService.verifyDetailed();
            assertTrue(reloaded.isValid(), "A modified license file should trigger hot reload and verification success.");
            assertFalse(reloaded.isFromCache());
            assertTrue(reloaded.isReloaded());
            assertTrue(reloaded.isConfiguredLicensePathPresent());
            assertTrue(reloaded.isConfiguredLicenseFileReadable());
            verify(licenseManager, times(1)).reloadIfNeeded(any());
            verify(licenseManager, never()).verify();

            CoreVerificationOutcome cached = fileBoundService.verifyDetailed();
            assertTrue(cached.isValid(), "After hot reload, the short-lived cache should be used.");
            assertTrue(cached.isFromCache());
            assertFalse(cached.isReloaded());
            verify(licenseManager, times(1)).reloadIfNeeded(any());
            verify(licenseManager, never()).verify();
        } finally {
            Files.deleteIfExists(licenseFile);
        }
    }

    private void expireVerificationCache(LicenseVerifyService service) throws Exception {
        Field lastSuccessTimestampField = LicenseVerifyService.class.getDeclaredField("lastSuccessTimestamp");
        lastSuccessTimestampField.setAccessible(true);
        lastSuccessTimestampField.setLong(service, System.currentTimeMillis() - 61_000L);
    }
}
