package org.eu.originalkeen.license.runtime.internal;

import org.eu.originalkeen.license.runtime.LicenseFailureCode;
import org.eu.originalkeen.license.runtime.LicenseMismatchType;
import org.eu.originalkeen.license.runtime.LicenseVerificationResult;

import java.time.Instant;

/**
 * Default immutable verification result.
 */
public final class DefaultLicenseVerificationResult implements LicenseVerificationResult {

    private final boolean valid;
    private final LicenseFailureCode failureCode;
    private final LicenseMismatchType mismatchType;
    private final String message;
    private final Instant checkedAt;
    private final Instant expiresAt;
    private final Long daysRemaining;
    private final boolean fromCache;
    private final boolean reloaded;

    public DefaultLicenseVerificationResult(
            boolean valid,
            LicenseFailureCode failureCode,
            LicenseMismatchType mismatchType,
            String message,
            Instant checkedAt,
            Instant expiresAt,
            Long daysRemaining,
            boolean fromCache,
            boolean reloaded
    ) {
        this.valid = valid;
        this.failureCode = failureCode;
        this.mismatchType = mismatchType;
        this.message = message;
        this.checkedAt = checkedAt;
        this.expiresAt = expiresAt;
        this.daysRemaining = daysRemaining;
        this.fromCache = fromCache;
        this.reloaded = reloaded;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public LicenseFailureCode getFailureCode() {
        return failureCode;
    }

    @Override
    public LicenseMismatchType getMismatchType() {
        return mismatchType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Instant getCheckedAt() {
        return checkedAt;
    }

    @Override
    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public Long getDaysRemaining() {
        return daysRemaining;
    }

    @Override
    public boolean isFromCache() {
        return fromCache;
    }

    @Override
    public boolean isReloaded() {
        return reloaded;
    }
}
