package org.eu.originalkeen.license.core.service;

import de.schlichtherle.license.LicenseContent;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

        when(licenseManager.verify())
                .thenReturn(null)
                .thenThrow(new IllegalStateException("license expired"));

        assertTrue(service.verify(), "The first verification should delegate to the license manager.");
        assertTrue(service.verify(), "The second verification should reuse the short-lived success cache.");
        verify(licenseManager, times(1)).verify();

        expireVerificationCache(service);

        assertFalse(service.verify(), "Once the cache expires, the service must perform a real verification again.");
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

            assertTrue(fileBoundService.verify(), "A modified license file should trigger hot reload and verification success.");
            verify(licenseManager, times(1)).reloadIfNeeded(any());
            verify(licenseManager, never()).verify();

            assertTrue(fileBoundService.verify(), "After hot reload, the short-lived cache should be used.");
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