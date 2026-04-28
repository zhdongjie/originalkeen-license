package org.eu.originalkeen.license.core.service;

import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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

    private void expireVerificationCache(LicenseVerifyService service) throws Exception {
        Field lastSuccessTimestampField = LicenseVerifyService.class.getDeclaredField("lastSuccessTimestamp");
        lastSuccessTimestampField.setAccessible(true);
        lastSuccessTimestampField.setLong(service, System.currentTimeMillis() - 61_000L);
    }
}