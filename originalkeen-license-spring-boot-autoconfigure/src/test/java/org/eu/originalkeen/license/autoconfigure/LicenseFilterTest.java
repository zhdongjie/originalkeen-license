package org.eu.originalkeen.license.autoconfigure;

import jakarta.servlet.FilterChain;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.runtime.LicenseFailureCode;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseVerificationResult;
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
        LicenseRuntime licenseRuntime = mock(LicenseRuntime.class);
        LicenseProperties properties = createEnabledProperties();
        LicenseFilter filter = new LicenseFilter(licenseRuntime, properties);

        MockHttpServletRequest actuatorRequest = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletRequest staticRequest = new MockHttpServletRequest("GET", "/assets/app.css");

        assertTrue(filter.shouldNotFilter(actuatorRequest));
        assertTrue(filter.shouldNotFilter(staticRequest));
        verify(licenseRuntime, never()).verify();
    }

    @Test
    void shouldRejectProtectedRequestsWhenLicenseVerificationFails() throws Exception {
        LicenseRuntime licenseRuntime = mock(LicenseRuntime.class);
        LicenseVerificationResult result = mock(LicenseVerificationResult.class);
        when(result.isValid()).thenReturn(false);
        when(result.getFailureCode()).thenReturn(LicenseFailureCode.EXPIRED);
        when(licenseRuntime.verify()).thenReturn(result);

        LicenseProperties properties = createEnabledProperties();
        LicenseFilter filter = new LicenseFilter(licenseRuntime, properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        assertFalse(filter.shouldNotFilter(request));
        filter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowProtectedRequestsWhenLicenseVerificationSucceeds() throws Exception {
        LicenseRuntime licenseRuntime = mock(LicenseRuntime.class);
        LicenseVerificationResult result = mock(LicenseVerificationResult.class);
        when(result.isValid()).thenReturn(true);
        when(licenseRuntime.verify()).thenReturn(result);

        LicenseProperties properties = createEnabledProperties();
        LicenseFilter filter = new LicenseFilter(licenseRuntime, properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    private LicenseProperties createEnabledProperties() {
        LicenseProperties properties = new LicenseProperties();
        properties.setEnabled(true);
        properties.setSubject("demo-subject");
        properties.setPublicAlias("publiccert");
        properties.setPublicKeyStorePath("classpath:publicCerts.keystore");
        properties.setPublicPassword("changeit1");
        properties.afterPropertiesSet();
        return properties;
    }
}
