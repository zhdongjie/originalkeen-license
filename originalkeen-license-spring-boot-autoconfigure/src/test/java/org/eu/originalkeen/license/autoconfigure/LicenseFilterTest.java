package org.eu.originalkeen.license.autoconfigure;

import jakarta.servlet.FilterChain;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LicenseFilterTest {

    @Test
    void shouldSkipActuatorAndStaticRequestsUsingDefaultPatterns() {
        LicenseVerifyService verifyService = mock(LicenseVerifyService.class);
        LicenseProperties properties = createEnabledProperties();
        LicenseFilter filter = new LicenseFilter(verifyService, properties);

        MockHttpServletRequest actuatorRequest = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletRequest staticRequest = new MockHttpServletRequest("GET", "/assets/app.css");

        assertTrue(filter.shouldNotFilter(actuatorRequest));
        assertTrue(filter.shouldNotFilter(staticRequest));
        verify(verifyService, never()).verify();
    }

    @Test
    void shouldRejectProtectedRequestsWhenLicenseVerificationFails() throws Exception {
        LicenseVerifyService verifyService = mock(LicenseVerifyService.class);
        when(verifyService.verify()).thenReturn(false);

        LicenseProperties properties = createEnabledProperties();
        LicenseFilter filter = new LicenseFilter(verifyService, properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        assertFalse(filter.shouldNotFilter(request));
        filter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    private LicenseProperties createEnabledProperties() {
        LicenseProperties properties = new LicenseProperties();
        properties.setEnabled(true);
        properties.setSubject("demo-subject");
        properties.setPublicAlias("publiccert");
        properties.setPublicKeyStorePath("classpath:publicCerts.keystore");
        properties.setPublicPassword("changeit");
        properties.afterPropertiesSet();
        return properties;
    }
}
