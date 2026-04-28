package org.eu.originalkeen.license.core.hardware;

import org.eu.originalkeen.license.core.utils.IpAddressUtils;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base class for hardware information providers.
 *
 * <p>The implementation caches the collected hardware information using
 * double-checked locking so the expensive operating system calls happen only once.
 * Subclasses are responsible for the CPU and main-board serial numbers, while the
 * shared base class handles IP and MAC address collection.</p>
 */
public abstract class AbstractHardwareProvider implements HardwareDataProvider {

    /**
     * Cached hardware information. The {@code volatile} modifier ensures that all
     * threads observe a fully initialized snapshot once the cache is populated.
     */
    private volatile LicenseCheckModel cachedModel = null;

    /**
     * Returns the hardware information of the current machine.
     *
     * @return {@link LicenseCheckModel} containing CPU, main-board, IP, and MAC data
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
     *
     * @return CPU serial number as a string
     */
    protected abstract String getCpuSerial();

    /**
     * Returns the main-board serial number.
     *
     * @return main-board serial number as a string
     */
    protected abstract String getMainBoardSerial();

    /**
     * Returns all local IP addresses of the machine.
     *
     * @return list of distinct IP addresses in lowercase form
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
     *
     * <p>Blank values are filtered out because some environments expose a usable IP
     * address but do not provide a hardware address for the associated interface.</p>
     *
     * @return list of distinct MAC addresses
     */
    protected List<String> getMacAddress() {
        List<InetAddress> inetAddresses = IpAddressUtils.getLocalAllInetAddress();
        return inetAddresses.stream()
                .map(IpAddressUtils::getMacByInetAddress)
                .filter(mac -> !mac.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}
