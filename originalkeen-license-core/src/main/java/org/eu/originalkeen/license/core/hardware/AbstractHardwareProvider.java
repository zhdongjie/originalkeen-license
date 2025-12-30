package org.eu.originalkeen.license.core.hardware;

import org.eu.originalkeen.license.core.utils.IpAddressUtils;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base class for hardware information providers.
 * <p>
 * Implements caching for hardware info using double-checked locking to avoid repeated
 * system calls. Subclasses must provide CPU and main-board serial numbers. IP and MAC
 * addresses can be overridden if needed.
 * </p>
 */
public abstract class AbstractHardwareProvider implements HardwareDataProvider {

    /**
     * Cached hardware information. Volatile ensures visibility across threads.
     */
    private volatile LicenseCheckModel cachedModel = null;

    /**
     * Returns the hardware information of the current machine.
     * Uses double-checked locking to initialize the cache only once.
     *
     * @return LicenseCheckModel containing CPU, mainboard, IPs, and MAC addresses
     */
    @Override
    public LicenseCheckModel getHardwareInfo() {
        if (cachedModel == null) {
            synchronized (this) {
                if (cachedModel == null) {
                    LicenseCheckModel model = new LicenseCheckModel();
                    model.setIpAddress(this.getIpAddress());
                    model.setMacAddress(this.getMacAddress());
                    model.setCpuSerial(this.getCpuSerial());
                    model.setMainBoardSerial(this.getMainBoardSerial());
                    cachedModel = model;
                }
            }
        }
        return cachedModel;
    }

    /**
     * Returns the CPU serial number.
     * Subclasses must implement this method.
     *
     * @return CPU serial number as a string
     */
    protected abstract String getCpuSerial();

    /**
     * Returns the main-board serial number.
     * Subclasses must implement this method.
     *
     * @return main-board serial number as a string
     */
    protected abstract String getMainBoardSerial();

    /**
     * Returns all local IP addresses of the machine.
     * Can be overridden by subclasses if custom behavior is needed.
     *
     * @return list of IP addresses in lowercase
     */
    protected List<String> getIpAddress() {
        List<InetAddress> inetAddresses = IpAddressUtils.getLocalAllInetAddress();
        return inetAddresses.stream()
                .map(InetAddress::getHostAddress)
                .distinct()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /**
     * Returns all MAC addresses of the machine.
     * Can be overridden by subclasses if custom behavior is needed.
     *
     * @return list of MAC addresses
     */
    protected List<String> getMacAddress() {
        List<InetAddress> inetAddresses = IpAddressUtils.getLocalAllInetAddress();
        return inetAddresses.stream()
                .map(IpAddressUtils::getMacByInetAddress)
                .distinct()
                .collect(Collectors.toList());
    }
}
