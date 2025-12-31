package org.eu.originalkeen.license.core.constant;

import java.util.Set;

/**
 * License module constants.
 * <p>
 * This class holds global constants used in the License module to avoid hard-coded values
 * and improve maintainability.
 * All fields are public, static, and final, and this class cannot be instantiated.
 * </p>
 */
public class LicenseConstants {

    /**
     * Default buffer size in bytes (8 KB)
     */
    public static final Integer DEFAULT_BUFF_SIZE = 8 * 1024;

    /**
     * Default character set for XML files
     */
    public static final String XML_CHARSET = "UTF-8";

    /**
     * Default exclude paths set for filter
     */
    public static final Set<String> DEFAULT_EXCLUDE_PATHS = Set.of(
        "/actuator/",
        "/static/",
        "/favicon.ico"
    );

    /**
     * Private constructor to prevent instantiation.
     */
    private LicenseConstants() {
        // Prevent instantiation
    }
}
