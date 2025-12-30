package org.eu.originalkeen.license.core.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public class IpAddressUtils {

    /**
     * Get all valid InetAddress of the current server.
     * Filters out loopback, virtual, link-local, and multicast addresses.
     *
     * @return list of InetAddress
     */
    public static List<InetAddress> getLocalAllInetAddress() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces())
                    .stream()
                    // Core improvement: filter by network interface name
                    .filter(ni -> {
                        try {
                            return !ni.isLoopback() && !ni.isVirtual() // exclude virtual interfaces
                                    && ni.isUp() // must be up
                                    // exclude common virtual NIC name prefixes
                                    && !ni.getName().startsWith("docker")
                                    && !ni.getName().startsWith("veth")
                                    && !ni.getName().startsWith("flannel")
                                    && !ni.getName().startsWith("cni");
                        } catch (SocketException e) {
                            return false;
                        }
                    })
                    .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                    .filter(inetAddr -> !inetAddr.isLoopbackAddress()
                            && !inetAddr.isLinkLocalAddress()
                            && !inetAddr.isMulticastAddress())
                    .collect(Collectors.toList());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get MAC address of a given network interface.
     *
     * @param inetAddr the InetAddress of the network interface
     * @return MAC address in format XX-XX-XX-XX-XX-XX
     */
    public static String getMacByInetAddress(InetAddress inetAddr) {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(inetAddr).getHardwareAddress();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    stringBuilder.append("-");
                }
                // Convert byte to hex string
                String temp = Integer.toHexString(mac[i] & 0xff);
                if (temp.length() == 1) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(temp);
            }
            return stringBuilder.toString().toUpperCase();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
