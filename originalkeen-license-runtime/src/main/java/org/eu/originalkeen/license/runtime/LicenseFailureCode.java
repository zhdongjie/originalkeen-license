package org.eu.originalkeen.license.runtime;

/**
 * Stable public verification failure taxonomy for V2 runtime callers.
 */
public enum LicenseFailureCode {
    NOT_INSTALLED,
    LICENSE_FILE_MISSING,
    EXPIRED,
    SIGNATURE_INVALID,
    HARDWARE_MISMATCH,
    CONFIGURATION_ERROR,
    INSTALLATION_ERROR,
    UNKNOWN_ERROR
}
