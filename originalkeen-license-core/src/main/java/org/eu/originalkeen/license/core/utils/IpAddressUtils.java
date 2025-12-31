package org.eu.originalkeen.license.core.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code IpAddressUtils} provides utility methods for retrieving
 * network-related information of the current server.
 *
 * <p>This class focuses on extracting <b>real, physical network addresses</b>
 * and intentionally filters out:</p>
 * <ul>
 *   <li>Loopback interfaces</li>
 *   <li>Virtual interfaces (Docker, Kubernetes, CNI, etc.)</li>
 *   <li>Link-local and multicast addresses</li>
 * </ul>
 *
 * <p>The primary use case is license and hardware binding, where
 * virtual or transient network interfaces must not be considered
 * as valid machine identifiers.</p>
 *
 * <p>This is a stateless utility class and all methods are thread-safe.</p>
 *
 * @author Original Keen
 */
public class IpAddressUtils {

    /**
     * Retrieves all valid {@link InetAddress} instances of the current server.
     *
     * <p>The following addresses are excluded:</p>
     * <ul>
     *   <li>Loopback interfaces</li>
     *   <li>Virtual network interfaces</li>
     *   <li>Interfaces that are not up</li>
     *   <li>Common container-related interfaces such as Docker, CNI, Flannel</li>
     *   <li>Loopback, link-local, and multicast IP addresses</li>
     * </ul>
     *
     * <p>This method is designed for environments running inside
     * containers or cloud platforms, ensuring that only meaningful
     * physical network addresses are returned.</p>
     *
     * @return a list of valid {@link InetAddress} instances
     * @throws RuntimeException if network interfaces cannot be accessed
     */
    public static List<InetAddress> getLocalAllInetAddress() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces())
                    .stream()
                    .filter(ni -> {
                        try {
                            return !ni.isLoopback()
                                    && !ni.isVirtual()
                                    && ni.isUp()
                                    // Exclude common virtual / container NICs
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
            throw new RuntimeException("Failed to retrieve local network addresses", e);
        }
    }

    /**
     * Retrieves the MAC address associated with the given {@link InetAddress}.
     *
     * <p>The returned MAC address is formatted as an uppercase hexadecimal
     * string separated by hyphens, for example:</p>
     *
     * <pre>
     * AA-BB-CC-DD-EE-FF
     * </pre>
     *
     * <p>This format is stable and suitable for license binding
     * and hardware fingerprinting.</p>
     *
     * @param inetAddr the {@link InetAddress} representing a network interface
     * @return the MAC address in format {@code XX-XX-XX-XX-XX-XX}
     * @throws RuntimeException if the MAC address cannot be obtained
     */
    public static String getMacByInetAddress(InetAddress inetAddr) {
        try {
            byte[] mac = NetworkInterface
                    .getByInetAddress(inetAddr)
                    .getHardwareAddress();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i > 0) {
                    builder.append("-");
                }
                // Convert byte to hex string
                String hex = Integer.toHexString(mac[i] & 0xff);
                if (hex.length() == 1) {
                    builder.append("0");
                }
                builder.append(hex);
            }
            return builder.toString().toUpperCase();
        } catch (SocketException e) {
            throw new RuntimeException("Failed to retrieve MAC address", e);
        }
    }

}
