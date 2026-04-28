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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * {@code LicenseFilter} is a standard servlet filter that enforces license validation
 * for incoming HTTP requests in a Spring Web application.
 *
 * <p>This filter extends {@link OncePerRequestFilter} to ensure a single execution per
 * request dispatch. It uses {@link AntPathMatcher} to support flexible exclusion patterns
 * such as {@code /actuator/**} and wildcard static-resource patterns defined in the
 * application configuration.</p>
 *
 * <p>The filtering logic follows these rules:</p>
 * <ul>
 *   <li>If {@code originalkeen.license.web-enabled} is false, the filter is bypassed.</li>
 *   <li>If the request URI matches any configured exclusion pattern, the filter is bypassed.</li>
 *   <li>Otherwise, {@link LicenseVerifyService} is invoked to validate the current license.</li>
 * </ul>
 *
 * <p>If verification fails, the request is rejected with an <b>HTTP 403 Forbidden</b> error,
 * preventing further processing by the application.</p>
 *
 * @author Original Keen
 * @see LicenseVerifyService
 * @see LicenseProperties
 * @see AntPathMatcher
 */
public class LicenseFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(LicenseFilter.class);

    private final LicenseVerifyService licenseVerifyService;
    private final LicenseProperties licenseProperties;

    /**
     * Standard Ant-style path matcher for wildcard pattern support.
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public LicenseFilter(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties licenseProperties
    ) {
        this.licenseVerifyService = licenseVerifyService;
        this.licenseProperties = licenseProperties;
    }

    /**
     * Determines whether the current request should bypass license verification.
     *
     * @param request current HTTP request
     * @return {@code true} when the filter should be skipped, otherwise {@code false}
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
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    /**
     * Performs license verification for each incoming HTTP request.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain servlet filter chain
     * @throws IOException if the response cannot be written
     * @throws ServletException if the filter chain fails
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException, ServletException {
        boolean valid = licenseVerifyService.verify();
        if (!valid) {
            log.warn("License verification failed for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "License verification failed");
            return;
        }
        filterChain.doFilter(request, response);
    }
}