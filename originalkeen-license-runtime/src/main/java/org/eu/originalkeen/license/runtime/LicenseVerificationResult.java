package org.eu.originalkeen.license.runtime;

import java.time.Instant;

/**
 * Structured verification result returned by {@link LicenseRuntime#verify()}.
 */
public interface LicenseVerificationResult {

    boolean isValid();

    LicenseFailureCode getFailureCode();

    LicenseMismatchType getMismatchType();

    String getMessage();

    Instant getCheckedAt();

    Instant getExpiresAt();

    Long getDaysRemaining();

    boolean isFromCache();

    boolean isReloaded();
}
