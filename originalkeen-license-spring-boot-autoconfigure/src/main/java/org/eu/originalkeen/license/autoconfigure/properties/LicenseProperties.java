package org.eu.originalkeen.license.autoconfigure.properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
@ConfigurationProperties(prefix = "originalkeen.license")
public class LicenseProperties implements InitializingBean {

    /**
     * Whether license verification is enabled
     */
    private boolean enabled = true;

    /**
     * Whether the web interceptor is enabled
     */
    private boolean webEnabled = true;

    /**
     * License subject
     */
    private String subject;

    /**
     * Path to the user's license file
     */
    private String licensePath;

    /**
     * Public key alias in the keystore
     */
    private String publicAlias;

    /**
     * Path to the public key keystore
     */
    private String publicKeyStorePath;

    /**
     * Password for the public key keystore
     */
    private String publicPassword;

    /**
     * Paths excluded from interception (whitelist), supporting AntPathMatcher patterns, e.g., /login, /actuator/**
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * Validate required properties after Spring injects them.
     * Throws IllegalArgumentException if any required property is missing.
     *
     */
    @Override
    public void afterPropertiesSet() {
        // Only validate if license verification is enabled
        if (!enabled) {
            return;
        }

        // Use Spring's Assert to check required properties
        Assert.hasText(subject, "License is enabled (enabled=true), but license.subject is not configured");
        Assert.hasText(publicAlias, "License is enabled (enabled=true), but publicAlias is not configured");
        Assert.hasText(publicKeyStorePath, "License is enabled (enabled=true), but publicKeyStorePath is not configured");
        Assert.hasText(publicPassword, "License is enabled (enabled=true), but publicPassword is not configured");

        if (excludePaths == null) {
            excludePaths = new ArrayList<>();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isWebEnabled() {
        return webEnabled;
    }

    public void setWebEnabled(boolean webEnabled) {
        this.webEnabled = webEnabled;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLicensePath() {
        return licensePath;
    }

    public void setLicensePath(String licensePath) {
        this.licensePath = licensePath;
    }

    public String getPublicAlias() {
        return publicAlias;
    }

    public void setPublicAlias(String publicAlias) {
        this.publicAlias = publicAlias;
    }

    public String getPublicKeyStorePath() {
        return publicKeyStorePath;
    }

    public void setPublicKeyStorePath(String publicKeyStorePath) {
        this.publicKeyStorePath = publicKeyStorePath;
    }

    public String getPublicPassword() {
        return publicPassword;
    }

    public void setPublicPassword(String publicPassword) {
        this.publicPassword = publicPassword;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

// Getters and setters omitted for brevity
}
