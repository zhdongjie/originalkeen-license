package org.eu.originalkeen.license.core.constant;

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
     * Private constructor to prevent instantiation.
     */
    private LicenseConstants() {
        // Prevent instantiation.
    }
}
