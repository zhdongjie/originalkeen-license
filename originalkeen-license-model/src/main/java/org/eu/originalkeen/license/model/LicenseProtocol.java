package org.eu.originalkeen.license.model;

import java.io.Serializable;

/**
 * Marker interface for license protocol objects.
 * All protocol models must be backward compatible.
 */
public interface LicenseProtocol extends Serializable {

    /**
     * License protocol version.
     */
    String getProtocolVersion();
}
