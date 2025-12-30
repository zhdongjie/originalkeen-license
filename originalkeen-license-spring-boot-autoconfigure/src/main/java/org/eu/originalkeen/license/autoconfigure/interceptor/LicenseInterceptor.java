package org.eu.originalkeen.license.autoconfigure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.autoconfigure.properties.LicenseProperties;
import org.eu.originalkeen.license.core.service.LicenseVerifyService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
public class LicenseInterceptor implements HandlerInterceptor {

    private static final Logger log = LogManager.getLogger(LicenseInterceptor.class);

    private final LicenseVerifyService licenseVerifyService;
    private final LicenseProperties licenseProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Constructor for LicenseInterceptor.
     *
     * @param licenseVerifyService the license verification service
     * @param licenseProperties    the license-related configuration properties
     */
    public LicenseInterceptor(
            LicenseVerifyService licenseVerifyService,
            LicenseProperties licenseProperties
    ) {
        this.licenseVerifyService = licenseVerifyService;
        this.licenseProperties = licenseProperties;
    }

    /**
     * Intercept the HTTP request before it reaches the controller.
     * <p>
     * 1. Requests matching the whitelist (excludePaths) are allowed directly.
     * 2. Other requests are verified against the installed license.
     * 3. If license verification fails, the request is rejected with 403.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param handler  the handler object
     * @return true if the request should proceed, false if it is blocked
     * @throws Exception if any error occurs during license verification
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @Nullable Object handler
    ) throws Exception {
        String requestUri = request.getRequestURI();

        // 1. Allow requests matching whitelist patterns
        for (String pattern : licenseProperties.getExcludePaths()) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }

        // 2. Perform license verification
        boolean verified = licenseVerifyService.verify();
        if (verified) {
            return true;
        }

        // 3. Verification failed: log warning and return 403 Forbidden
        log.warn("License verification failed, access denied: {}", requestUri);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "License verification failed");
        return false;
    }
}
