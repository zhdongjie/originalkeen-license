package org.eu.originalkeen.license.runtime.exception;

/**
 * Thrown when runtime-managed license installation fails.
 */
public class LicenseInstallationException extends LicenseRuntimeException {

    public LicenseInstallationException(String message) {
        super(message);
    }

    public LicenseInstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
