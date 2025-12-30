package org.eu.originalkeen.license.model;

import java.io.Serial;
import java.util.List;

/**
 * Hardware binding information.
 */
public class LicenseCheckModel implements LicenseProtocol {

    @Serial
    private static final long serialVersionUID = 1L;

    private String protocolVersion;

    /**
     * Bind IP addresses
     */
    private List<String> ipAddress;

    /**
     * Bind MAC addresses
     */
    private List<String> macAddress;

    /**
     * CPU serial
     */
    private String cpuSerial;

    /**
     * Main board serial
     */
    private String mainBoardSerial;

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public List<String> getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(List<String> ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<String> getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(List<String> macAddress) {
        this.macAddress = macAddress;
    }

    public String getCpuSerial() {
        return cpuSerial;
    }

    public void setCpuSerial(String cpuSerial) {
        this.cpuSerial = cpuSerial;
    }

    public String getMainBoardSerial() {
        return mainBoardSerial;
    }

    public void setMainBoardSerial(String mainBoardSerial) {
        this.mainBoardSerial = mainBoardSerial;
    }
}
