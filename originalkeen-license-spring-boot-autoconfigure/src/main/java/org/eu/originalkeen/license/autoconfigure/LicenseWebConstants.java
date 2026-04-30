package org.eu.originalkeen.license.autoconfigure;

import java.util.Set;

/**
 * Spring Web-specific license constants.
 */
public final class LicenseWebConstants {

    public static final Set<String> DEFAULT_EXCLUDE_PATHS = Set.of(
            "/actuator/**",
            "/error",
            "/favicon.ico",
            "/webjars/**",
            "/**/*.css",
            "/**/*.js",
            "/**/*.html",
            "/**/*.png",
            "/**/*.jpg",
            "/**/*.jpeg",
            "/**/*.gif",
            "/**/*.svg",
            "/**/*.ico"
    );

    private LicenseWebConstants() {
    }
}
