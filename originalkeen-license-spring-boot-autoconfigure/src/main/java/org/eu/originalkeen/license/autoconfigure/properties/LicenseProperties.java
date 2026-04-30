package org.eu.originalkeen.license.autoconfigure.properties;

import org.eu.originalkeen.license.autoconfigure.LicenseWebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
 * fail-fast validation after property binding, ensuring that
 * the public keystore settings required for runtime verification
 * are present when the license feature is enabled.</p>
 *
 * <p>Startup installation intentionally handles {@code licensePath}
 * more leniently so an application can still boot and log a warning
 * when the license file is mounted later or installed by another process.</p>
 *
 * @author Original Keen
 */
@ConfigurationProperties(prefix = "originalkeen.license")
public class LicenseProperties implements InitializingBean {

    /**
     * Whether license verification is globally enabled.
     */
    private boolean enabled = true;

    /**
     * Whether web-layer license verification is enabled.
     */
    private boolean webEnabled = true;

    /**
     * License subject used to identify the license.
     */
    private String subject;

    /**
     * Absolute or relative path to the license file provided by the user.
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
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * Validates required license properties after Spring finishes property binding.
     */
    @Override
    public void afterPropertiesSet() {
        if (!enabled) {
            setDefaultExcludePaths();
            return;
        }

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

        /*
         * Do not fail fast on licensePath here.
         * Startup installation is allowed to degrade into a warning-and-skip flow
         * so applications can still boot in environments where the license is
         * mounted later or installation is handled by another process.
         */
        setDefaultExcludePaths();
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
        LinkedHashSet<String> mergedPaths = new LinkedHashSet<>();
        if (excludePaths != null) {
            excludePaths.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(mergedPaths::add);
        }
        mergedPaths.addAll(LicenseWebConstants.DEFAULT_EXCLUDE_PATHS);
        this.excludePaths = new ArrayList<>(mergedPaths);
    }
}
