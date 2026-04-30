package org.eu.originalkeen.license.runtime;

/**
 * Optional hardware mismatch detail when verification fails with
 * {@link LicenseFailureCode#HARDWARE_MISMATCH}.
 */
public enum LicenseMismatchType {
    IP,
    MAC,
    CPU,
    MAIN_BOARD
}
