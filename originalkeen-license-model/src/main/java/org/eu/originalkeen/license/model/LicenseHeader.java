package org.eu.originalkeen.license.model;

import java.io.Serial;
import java.time.Instant;

/**
 * License metadata header.
 */
public class LicenseHeader implements LicenseProtocol {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Protocol version
     */
    private String protocolVersion;

    /**
     * License unique id
     */
    private String licenseId;

    /**
     * Issued time
     */
    private Instant issuedAt;

    /**
     * Expire time
     */
    private Instant expireAt;

    /**
     * Issuer (company / system)
     */
    private String issuer;

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
