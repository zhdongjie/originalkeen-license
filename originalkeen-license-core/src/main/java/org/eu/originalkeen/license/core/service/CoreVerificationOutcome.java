package org.eu.originalkeen.license.core.service;

import de.schlichtherle.license.LicenseContent;

import java.time.Instant;

/**
 * Detailed verification outcome used by higher-level runtime adapters.
 *
 * <p>This type keeps the operational verification behavior in {@link LicenseVerifyService}
 * while exposing enough metadata for the V2 runtime result model.</p>
 */
public final class CoreVerificationOutcome {

    private final boolean valid;
    private final boolean fromCache;
    private final boolean reloaded;
    private final Throwable failure;
    private final LicenseContent content;
    private final Instant checkedAt;
    private final boolean configuredLicensePathPresent;
    private final boolean configuredLicenseFileReadable;

    private CoreVerificationOutcome(
            boolean valid,
            boolean fromCache,
            boolean reloaded,
            Throwable failure,
            LicenseContent content,
            Instant checkedAt,
            boolean configuredLicensePathPresent,
            boolean configuredLicenseFileReadable
    ) {
        this.valid = valid;
        this.fromCache = fromCache;
        this.reloaded = reloaded;
        this.failure = failure;
        this.content = content;
        this.checkedAt = checkedAt;
        this.configuredLicensePathPresent = configuredLicensePathPresent;
        this.configuredLicenseFileReadable = configuredLicenseFileReadable;
    }

    public static CoreVerificationOutcome success(
            LicenseContent content,
            boolean fromCache,
            boolean reloaded,
            Instant checkedAt,
            boolean configuredLicensePathPresent,
            boolean configuredLicenseFileReadable
    ) {
        return new CoreVerificationOutcome(
                true,
                fromCache,
                reloaded,
                null,
                content,
                checkedAt,
                configuredLicensePathPresent,
                configuredLicenseFileReadable
        );
    }

    public static CoreVerificationOutcome failure(
            Throwable failure,
            Instant checkedAt,
            boolean configuredLicensePathPresent,
            boolean configuredLicenseFileReadable
    ) {
        return new CoreVerificationOutcome(
                false,
                false,
                false,
                failure,
                null,
                checkedAt,
                configuredLicensePathPresent,
                configuredLicenseFileReadable
        );
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public boolean isReloaded() {
        return reloaded;
    }

    public Throwable getFailure() {
        return failure;
    }

    public LicenseContent getContent() {
        return content;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public boolean isConfiguredLicensePathPresent() {
        return configuredLicensePathPresent;
    }

    public boolean isConfiguredLicenseFileReadable() {
        return configuredLicenseFileReadable;
    }
}
