package org.eu.originalkeen.license.autoconfigure.properties;

import org.eu.originalkeen.license.core.constant.LicenseConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code LicenseProperties} defines all configurable properties
 * for the Original Keen License system.
 *
 * <p>This class is bound to external configuration using
 * {@link ConfigurationProperties} with the prefix
 * {@code originalkeen.license}.</p>
 *
 * <p>It controls:</p>
 * <ul>
 *   <li>Whether license verification is globally enabled</li>
 *   <li>Whether web-layer license enforcement is enabled</li>
 *   <li>License metadata and public key configuration</li>
 *   <li>Web request exclusion paths</li>
 * </ul>
 *
 * <p>The class implements {@link InitializingBean} to perform
 * <b>fail-fast validation</b> after property binding, ensuring
 * that required license configuration is present when the
 * license feature is enabled.</p>
 *
 * <p>If license verification is disabled via configuration,
 * validation will be skipped entirely.</p>
 *
 * <p>This design ensures that misconfiguration is detected
 * early during application startup rather than at runtime.</p>
 *
 * @author Original Keen
 */
@ConfigurationProperties(prefix = "originalkeen.license")
public class LicenseProperties implements InitializingBean {

    /**
     * Whether license verification is globally enabled.
     *
     * <p>If set to {@code false}, the license system will be
     * completely disabled and no validation will be performed.</p>
     *
     * <p>Default value: {@code true}</p>
     */
    private boolean enabled = true;

    /**
     * Whether web-layer license verification is enabled.
     *
     * <p>This flag controls whether HTTP requests will be
     * intercepted by the license filter.</p>
     *
     * <p>Only effective in servlet-based web applications.</p>
     *
     * <p>Default value: {@code true}</p>
     */
    private boolean webEnabled = true;

    /**
     * License subject used to identify the license.
     *
     * <p>This value must match the subject used when
     * generating the license.</p>
     */
    private String subject;

    /**
     * Absolute or relative path to the license file provided
     * by the user.
     */
    private String licensePath;

    /**
     * Alias of the public key stored in the keystore.
     */
    private String publicAlias;

    /**
     * Path to the public key keystore file.
     */
    private String publicKeyStorePath;

    /**
     * Password used to access the public key keystore.
     */
    private String publicPassword;

    /**
     * URL path patterns excluded from license interception.
     *
     * <p>Supports Ant-style path patterns, for example:</p>
     * <ul>
     *   <li>{@code /login}</li>
     *   <li>{@code /actuator/**}</li>
     * </ul>
     *
     * <p>Typically used for health checks, login endpoints,
     * or public APIs.</p>
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * Validates required license properties after Spring
     * finishes property binding.
     *
     * <p>This method performs fail-fast validation to ensure
     * that all mandatory license configuration is present
     * when license verification is enabled.</p>
     *
     * <p>If {@link #enabled} is set to {@code false},
     * validation is skipped entirely.</p>
     *
     * @throws IllegalArgumentException if required properties
     *                                  are missing or empty
     */
    @Override
    public void afterPropertiesSet() {
        // Skip validation if license verification is disabled
        if (!enabled) {
            return;
        }

        // Validate required properties
        Assert.hasText(
                subject,
                "License is enabled (enabled=true), but license.subject is not configured"
        );
        Assert.hasText(
                publicAlias,
                "License is enabled (enabled=true), but publicAlias is not configured"
        );
        Assert.hasText(
                publicKeyStorePath,
                "License is enabled (enabled=true), but publicKeyStorePath is not configured"
        );
        Assert.hasText(
                publicPassword,
                "License is enabled (enabled=true), but publicPassword is not configured"
        );
        Assert.hasText(
                licensePath,
                "License is enabled (enabled=true), but license path is not configured"
        );
        Assert.isTrue(
                Files.exists(Paths.get(licensePath)),
                "License is enabled (enabled=true), but license file does not exist"
        );
        // Ensure excludePaths is never null
        this.setDefaultExcludePaths();
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

    public void setDefaultExcludePaths() {
        if (excludePaths == null) {
            excludePaths = new ArrayList<>();
        }
        this.excludePaths.addAll(LicenseConstants.DEFAULT_EXCLUDE_PATHS);
    }
}
