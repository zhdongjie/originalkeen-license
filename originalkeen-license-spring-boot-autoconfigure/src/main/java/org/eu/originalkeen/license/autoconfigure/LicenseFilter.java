package org.eu.originalkeen.license.autoconfigure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.runtime.LicenseRuntime;
import org.eu.originalkeen.license.runtime.LicenseVerificationResult;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that enforces runtime-backed license validation.
 */
public class LicenseFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(LicenseFilter.class);

    private final LicenseRuntime licenseRuntime;
    private final LicenseProperties licenseProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public LicenseFilter(
            LicenseRuntime licenseRuntime,
            LicenseProperties licenseProperties
    ) {
        this.licenseRuntime = licenseRuntime;
        this.licenseProperties = licenseProperties;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!licenseProperties.isWebEnabled()) {
            return true;
        }

        String requestUri = request.getRequestURI();
        return licenseProperties.getExcludePaths() != null
                && licenseProperties.getExcludePaths()
                .stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException, ServletException {
        LicenseVerificationResult result = licenseRuntime.verify();
        if (!result.isValid()) {
            log.warn(
                    "License verification failed for request: {}, code: {}",
                    request.getRequestURI(),
                    result.getFailureCode()
            );
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "License verification failed");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
