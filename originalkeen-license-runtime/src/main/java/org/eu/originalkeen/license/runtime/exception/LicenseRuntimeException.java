package org.eu.originalkeen.license.runtime.exception;

/**
 * Base runtime exception for V2 runtime operations.
 */
public class LicenseRuntimeException extends RuntimeException {

    public LicenseRuntimeException(String message) {
        super(message);
    }

    public LicenseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
