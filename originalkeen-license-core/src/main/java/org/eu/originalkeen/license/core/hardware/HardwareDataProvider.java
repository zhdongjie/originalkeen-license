package org.eu.originalkeen.license.core.hardware;

import org.eu.originalkeen.license.model.LicenseCheckModel;

/**
 * Interface for providing hardware information of the current machine.
 * <p>
 * Implementations should return hardware-related details such as CPU serial,
 * main-board serial, IP addresses, and MAC addresses. This interface is typically
 * used for license validation or hardware fingerprinting.
 * </p>
 */
public interface HardwareDataProvider {

    /**
     * Retrieves the hardware information of the current machine.
     * Implementations may cache the results for performance.
     *
     * @return LicenseCheckModel containing CPU, main-board, IP, and MAC information
     */
    LicenseCheckModel getHardwareInfo();
}
