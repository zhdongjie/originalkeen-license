package org.eu.originalkeen.license.runtime.internal.bootstrap;

import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.NoLicenseInstalledException;
import org.eu.originalkeen.license.core.service.CoreVerificationOutcome;
import org.eu.originalkeen.license.runtime.LicenseFailureCode;
import org.eu.originalkeen.license.runtime.LicenseMismatchType;
import org.eu.originalkeen.license.runtime.LicenseVerificationResult;
import org.eu.originalkeen.license.runtime.exception.LicenseVerificationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseVerificationTranslatorTest {

    private final LicenseVerificationTranslator translator = new LicenseVerificationTranslator();

    @Test
    void shouldTranslateSuccessfulVerificationMetadata() {
        Instant checkedAt = Instant.parse("2026-04-29T08:00:00Z");
        LicenseContent content = new LicenseContent();
        content.setNotAfter(Date.from(checkedAt.plus(45, ChronoUnit.DAYS)));

        LicenseVerificationResult result = translator.toResult(
                CoreVerificationOutcome.success(content, true, false, checkedAt, false, false)
        );

        assertTrue(result.isValid());
        assertNull(result.getFailureCode());
        assertNull(result.getMismatchType());
        assertEquals(checkedAt.plus(45, ChronoUnit.DAYS), result.getExpiresAt());
        assertEquals(45L, result.getDaysRemaining());
        assertTrue(result.isFromCache());
        assertEquals("License verification succeeded using the short-lived success cache.", result.getMessage());
    }

    @Test
    void shouldTranslateConfiguredMissingLicenseFileAsDedicatedFailureCode() {
        LicenseVerificationResult result = translator.toResult(
                CoreVerificationOutcome.failure(
                        new NoLicenseInstalledException("demo-subject"),
                        Instant.parse("2026-04-29T08:00:00Z"),
                        true,
                        false
                )
        );

        assertEquals(LicenseFailureCode.LICENSE_FILE_MISSING, result.getFailureCode());
        assertNull(result.getMismatchType());
    }

    @Test
    void shouldTranslateHardwareMismatchDetails() {
        RuntimeException failure = new RuntimeException("CPU serial not authorized");

        LicenseVerificationResult result = translator.toResult(
                CoreVerificationOutcome.failure(
                        failure,
                        Instant.parse("2026-04-29T08:00:00Z"),
                        false,
                        false
                )
        );

        assertEquals(LicenseFailureCode.HARDWARE_MISMATCH, result.getFailureCode());
        assertEquals(LicenseMismatchType.CPU, result.getMismatchType());
    }

    @Test
    void shouldTranslateSignatureFailuresFromNestedRootCause() {
        RuntimeException failure = new RuntimeException(
                "wrapper",
                new IllegalStateException("Signature verification failed")
        );

        LicenseVerificationResult result = translator.toResult(
                CoreVerificationOutcome.failure(
                        failure,
                        Instant.parse("2026-04-29T08:00:00Z"),
                        false,
                        false
                )
        );

        assertEquals(LicenseFailureCode.SIGNATURE_INVALID, result.getFailureCode());
        assertNull(result.getMismatchType());
    }

    @Test
    void shouldRaiseVerificationExceptionFromStructuredFailure() {
        LicenseVerificationResult result = translator.toResult(
                CoreVerificationOutcome.failure(
                        new RuntimeException("License has expired"),
                        Instant.parse("2026-04-29T08:00:00Z"),
                        false,
                        false
                )
        );

        LicenseVerificationException exception = assertThrows(
                LicenseVerificationException.class,
                () -> {
                    throw translator.toVerificationException(result);
                }
        );

        assertEquals(LicenseFailureCode.EXPIRED, exception.getFailureCode());
        assertNull(exception.getMismatchType());
    }
}
