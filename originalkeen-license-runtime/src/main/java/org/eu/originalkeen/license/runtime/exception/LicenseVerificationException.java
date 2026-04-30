package org.eu.originalkeen.license.runtime.exception;

import org.eu.originalkeen.license.runtime.LicenseFailureCode;
import org.eu.originalkeen.license.runtime.LicenseMismatchType;

/**
 * Thrown when {@code verifyOrThrow()} is used and verification fails.
 */
public class LicenseVerificationException extends LicenseRuntimeException {

    private final LicenseFailureCode failureCode;
    private final LicenseMismatchType mismatchType;

    public LicenseVerificationException(
            String message,
            LicenseFailureCode failureCode,
            LicenseMismatchType mismatchType
    ) {
        super(message);
        this.failureCode = failureCode;
        this.mismatchType = mismatchType;
    }

    public LicenseVerificationException(
            String message,
            LicenseFailureCode failureCode,
            LicenseMismatchType mismatchType,
            Throwable cause
    ) {
        super(message, cause);
        this.failureCode = failureCode;
        this.mismatchType = mismatchType;
    }

    public LicenseFailureCode getFailureCode() {
        return failureCode;
    }

    public LicenseMismatchType getMismatchType() {
        return mismatchType;
    }
}
