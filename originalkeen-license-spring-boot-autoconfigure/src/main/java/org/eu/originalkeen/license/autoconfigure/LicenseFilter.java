package org.eu.originalkeen.license.autoconfigure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LicenseFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(LicenseFilter.class);

    private final LicenseVerifyService licenseVerifyService;
    private final LicenseProperties licenseProperties;

    public LicenseFilter(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties licenseProperties
    ) {
        this.licenseVerifyService = licenseVerifyService;
        this.licenseProperties = licenseProperties;
    }

    /**
     * Determine whether the current request should bypass license verification.
     *
     * <p>This method is invoked by {@link OncePerRequestFilter} before
     * {@link #doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)}.
     * If it returns {@code true}, the filter logic will be skipped entirely.</p>
     *
     * <p>Bypass conditions:</p>
     * <ul>
     *   <li>Web license checking is disabled via configuration</li>
     *   <li>The request URI matches one of the configured exclude paths</li>
     * </ul>
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!licenseProperties.isWebEnabled()) {
            return true;
        }

        String requestUri = request.getRequestURI();
        return licenseProperties.getExcludePaths() != null
                && licenseProperties.getExcludePaths()
                .stream()
                .anyMatch(requestUri::startsWith);
    }

    /**
     * Perform license verification for each incoming HTTP request.
     *
     * <p>If the license verification succeeds, the request is forwarded to the
     * next filter in the chain.</p>
     *
     * <p>If the verification fails, the request is immediately rejected with
     * HTTP 403 (Forbidden), and the filter chain will not continue.</p>
     *
     * <p>This method is guaranteed to be executed at most once per request
     * by {@link OncePerRequestFilter}.</p>
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException, ServletException {
        // Perform license verification before processing the request
        boolean valid = licenseVerifyService.verify();
        if (!valid) {
            log.warn("License verification failed for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "License verification failed");
            return;
        }
        filterChain.doFilter(request, response);
    }

}
