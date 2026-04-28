package org.eu.originalkeen.license.core.constant;

import java.util.Set;

/**
 * License module constants.
 *
 * <p>This class holds shared constants used by the license module to avoid
 * scattering hard-coded values across the implementation.</p>
 */
public class LicenseConstants {

    /**
     * Default buffer size in bytes (8 KB).
     */
    public static final Integer DEFAULT_BUFF_SIZE = 8 * 1024;

    /**
     * Default character set for XML files.
     */
    public static final String XML_CHARSET = "UTF-8";

    /**
     * Default exclude paths for the servlet filter.
     *
     * <p>The values are expressed as Ant-style patterns because
     * {@code LicenseFilter} uses {@code AntPathMatcher} to decide whether a
     * request should bypass license validation.</p>
     */
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

    /**
     * Private constructor to prevent instantiation.
     */
    private LicenseConstants() {
        // Prevent instantiation.
    }
}
