package org.eu.originalkeen.license.runtime.exception;

/**
 * Thrown when runtime configuration is invalid or unsupported.
 */
public class LicenseConfigurationException extends LicenseRuntimeException {

    public LicenseConfigurationException(String message) {
        super(message);
    }

    public LicenseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
