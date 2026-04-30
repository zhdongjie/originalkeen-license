package org.eu.originalkeen.license.runtime.internal.bootstrap;

import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.NoLicenseInstalledException;
import org.eu.originalkeen.license.core.service.CoreVerificationOutcome;
import org.eu.originalkeen.license.runtime.LicenseFailureCode;
import org.eu.originalkeen.license.runtime.LicenseMismatchType;
import org.eu.originalkeen.license.runtime.LicenseVerificationResult;
import org.eu.originalkeen.license.runtime.exception.LicenseInstallationException;
import org.eu.originalkeen.license.runtime.exception.LicenseVerificationException;
import org.eu.originalkeen.license.runtime.internal.DefaultLicenseVerificationResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

/**
 * Translates low-level verification outcomes into V2 runtime results and exceptions.
 */
public final class LicenseVerificationTranslator {

    public LicenseVerificationResult toResult(CoreVerificationOutcome outcome) {
        if (outcome.isValid()) {
            return toSuccessfulResult(outcome);
        }

        FailureDetails details = classifyFailure(outcome);
        return new DefaultLicenseVerificationResult(
                false,
                details.failureCode(),
                details.mismatchType(),
                details.message(),
                outcome.getCheckedAt(),
                null,
                null,
                false,
                false
        );
    }

    public LicenseInstallationException toInstallationException(String licensePath, RuntimeException ex) {
        return new LicenseInstallationException(
                "License installation failed for path: " + licensePath,
                ex
        );
    }

    public LicenseVerificationException toVerificationException(LicenseVerificationResult result) {
        return new LicenseVerificationException(
                result.getMessage(),
                result.getFailureCode(),
                result.getMismatchType()
        );
    }

    private LicenseVerificationResult toSuccessfulResult(CoreVerificationOutcome outcome) {
        Instant expiresAt = toInstant(outcome.getContent());
        Long daysRemaining = computeDaysRemaining(outcome.getCheckedAt(), expiresAt);

        String message = "License verification succeeded.";
        if (outcome.isReloaded()) {
            message = "License verification succeeded after reloading the configured license file.";
        } else if (outcome.isFromCache()) {
            message = "License verification succeeded using the short-lived success cache.";
        }

        return new DefaultLicenseVerificationResult(
                true,
                null,
                null,
                message,
                outcome.getCheckedAt(),
                expiresAt,
                daysRemaining,
                outcome.isFromCache(),
                outcome.isReloaded()
        );
    }

    private FailureDetails classifyFailure(CoreVerificationOutcome outcome) {
        Throwable failure = rootCause(outcome.getFailure());
        if (failure instanceof NoLicenseInstalledException) {
            if (outcome.isConfiguredLicensePathPresent() && !outcome.isConfiguredLicenseFileReadable()) {
                return new FailureDetails(
                        LicenseFailureCode.LICENSE_FILE_MISSING,
                        null,
                        "Configured license file is missing or unreadable."
                );
            }
            return new FailureDetails(
                    LicenseFailureCode.NOT_INSTALLED,
                    null,
                    "No license is currently installed."
            );
        }

        String message = normalizeMessage(failure);
        if (message.contains("ip address not authorized")) {
            return new FailureDetails(
                    LicenseFailureCode.HARDWARE_MISMATCH,
                    LicenseMismatchType.IP,
                    "License verification failed because the current IP address is not authorized."
            );
        }
        if (message.contains("mac address not authorized")) {
            return new FailureDetails(
                    LicenseFailureCode.HARDWARE_MISMATCH,
                    LicenseMismatchType.MAC,
                    "License verification failed because the current MAC address is not authorized."
            );
        }
        if (message.contains("cpu serial not authorized")) {
            return new FailureDetails(
                    LicenseFailureCode.HARDWARE_MISMATCH,
                    LicenseMismatchType.CPU,
                    "License verification failed because the current CPU serial is not authorized."
            );
        }
        if (message.contains("main-board serial not authorized")) {
            return new FailureDetails(
                    LicenseFailureCode.HARDWARE_MISMATCH,
                    LicenseMismatchType.MAIN_BOARD,
                    "License verification failed because the current main-board serial is not authorized."
            );
        }
        if (message.contains("expired")) {
            return new FailureDetails(
                    LicenseFailureCode.EXPIRED,
                    null,
                    "License verification failed because the license has expired."
            );
        }
        if (message.contains("keystore")
                || message.contains("classpath")
                || message.contains("unsupported operating system")) {
            return new FailureDetails(
                    LicenseFailureCode.CONFIGURATION_ERROR,
                    null,
                    "License verification failed because the runtime configuration is invalid."
            );
        }
        if (message.contains("certificate")
                || message.contains("signature")
                || message.contains("notary")) {
            return new FailureDetails(
                    LicenseFailureCode.SIGNATURE_INVALID,
                    null,
                    "License verification failed because the license signature is invalid."
            );
        }
        if (message.contains("hardware info")) {
            return new FailureDetails(
                    LicenseFailureCode.HARDWARE_MISMATCH,
                    null,
                    "License verification failed because the current hardware information could not satisfy the license requirements."
            );
        }

        return new FailureDetails(
                LicenseFailureCode.UNKNOWN_ERROR,
                null,
                failure == null || failure.getMessage() == null
                        ? "License verification failed for an unknown reason."
                        : failure.getMessage()
        );
    }

    private Throwable rootCause(Throwable failure) {
        if (failure == null) {
            return null;
        }

        Throwable current = failure;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String normalizeMessage(Throwable failure) {
        if (failure == null || failure.getMessage() == null) {
            return "";
        }
        return failure.getMessage().toLowerCase(Locale.ROOT);
    }

    private Instant toInstant(LicenseContent content) {
        if (content == null) {
            return null;
        }

        Date notAfter = content.getNotAfter();
        return notAfter == null ? null : notAfter.toInstant();
    }

    private Long computeDaysRemaining(Instant checkedAt, Instant expiresAt) {
        if (checkedAt == null || expiresAt == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(checkedAt, expiresAt);
    }

    private record FailureDetails(
            LicenseFailureCode failureCode,
            LicenseMismatchType mismatchType,
            String message
    ) {
    }
}
