# OriginalKeen License

**OriginalKeen License** is a **Java license management and verification system** for enterprise applications.
It supports hardware-bound licenses, automatic installation and verification, and seamless integration with Spring Boot.

This project is modularized into four modules:

* `originalkeen-license-model` – Defines license data models and constants.
* `originalkeen-license-core` – Core library for license creation, verification, and hardware detection.
* `originalkeen-license-spring-boot-autoconfigure` – Spring Boot auto-configuration for license integration.
* `originalkeen-license-spring-boot-starter` – Starter module for zero-configuration Spring Boot integration.

---

## Features

* Hardware binding: CPU, motherboard, IP, and MAC address.
* License expiration management with pre-warning.
* Web request interception with path whitelist.
* Cross-platform support: Windows and Linux.
* Spring Boot integration via auto-configuration and starter.
* Verification caching for improved performance.

---

## Modules Overview

### 1. `originalkeen-license-model`

Provides **data models and constants** for license management:

* `LicenseCheckModel` – Contains hardware fingerprint information (CPU, motherboard, MAC, IP).
* `LicenseConstants` – Default constants like buffer size and XML charset.
* Enums or other utility classes as needed for license metadata.

This module is used by **core, configure, and starter** to unify license data structures.

---

### 2. `originalkeen-license-core`

Contains the **core functionality**:

* License installation, verification, and creation (`LicenseManagerAdapter`, `LicenseVerifyService`).
* Hardware detection (`HardwareDataProvider`, `WindowsHardwareProvider`, `LinuxHardwareProvider`).
* IP and MAC utilities (`IpAddressUtils`).
* License content handling and decoding.
* Cache for verification results (default 60 seconds).

**Programmatic usage:**

```java
LicenseVerifyService verifyService = new LicenseVerifyService(licenseManagerAdapter);
verifyService.install("/path/to/license.lic");

if (verifyService.verify()) {
    System.out.println("License is valid");
} else {
    System.out.println("License verification failed");
}
```

---

### 3. `originalkeen-license-spring-boot-autoconfigure`

Spring Boot **auto-configuration** module:

* Registers beans: `HardwareDataProvider`, `LicenseManagerAdapter`, `LicenseVerifyService`, `LicenseParam`.
* Detects OS automatically and configures the proper hardware provider.
* Supports automatic license installation on application startup (`LicenseCheckListener`).

**Enable auto-configuration via starter** or manual import:

```java
@Import(LicenseAutoConfiguration.class)
```

---

### 4. `originalkeen-license-spring-boot-starter`

Spring Boot **starter** module for quick integration:

* Registers `LicenseWebAutoConfiguration` when `web-enabled=true`.
* Automatically adds a `LicenseInterceptor` to verify all HTTP requests.
* Supports `exclude-paths` whitelist (e.g., `/login`, `/actuator/**`).
* Uses properties from `application.yml` or `application.properties`.

**Example configuration:**

```yaml
originalkeen:
  license:
    enabled: true
    web-enabled: true
    subject: "MyAppLicense"
    license-path: "classpath:license.lic"
    public-alias: "public"
    public-key-store-path: "classpath:publicKey.keystore"
    public-password: "changeit"
    exclude-paths:
      - /login
      - /actuator/**
```

---

## Quick Start

1. **Add the starter dependency**:

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. **Configure license properties** in `application.yml` (see above).

3. **Start the application**: license will be installed automatically if `license-path` is provided.

4. **Programmatic verification**:

```java
@Autowired
private LicenseVerifyService licenseVerifyService;

if (!licenseVerifyService.verify()) {
    throw new RuntimeException("License verification failed");
}
```

---

## Web Interceptor

* Activated when `web-enabled=true`.
* All HTTP requests are checked; unauthorized requests return **HTTP 403 Forbidden**.
* `exclude-paths` allow whitelisting specific endpoints.

---

## Hardware Binding

* Detects **CPU ID**, **motherboard serial**, **MAC address**, and **IP addresses**.
* Supports Windows and Linux.
* Hardware provider can be customized by implementing `HardwareDataProvider`.

---

## Logging and Cache

* Logs installation success/failure.
* Logs verification success/failure.
* Logs upcoming license expiration (15 days before).
* Verification results cached for 60 seconds to reduce repeated checks.

---

## Extending

Override default beans if needed:

```java
@Bean
@ConditionalOnMissingBean(HardwareDataProvider.class)
public HardwareDataProvider customProvider() {
    return new CustomHardwareProvider();
}

@Bean
@ConditionalOnMissingBean(LicenseParam.class)
public LicenseParam customParam(LicenseProperties properties) {
    return new CustomLicenseParam(properties);
}
```

---

## Build Guide

### 1. Start GPG Agent (Optional)

Before signing, ensure `gpg-agent` is running. You can start it manually:

```
gpg-agent --daemon
```

### 2. Local Build and GPG Signing

You can build the project, generate sources, Javadoc, and sign artifacts using:

```
mvn clean verify gpg:sign
```

Or directly sign a specific file using GPG:

```
gpg --sign path/to/file
```

> These commands **do not upload artifacts** to any remote repository. They only validate the build and create signed artifacts locally.

------

### 3. Deploy to Maven Central

To publish artifacts to Maven Central, use the **Release Profile**:

```
mvn clean deploy -P release
```

Notes:

- `-P release` activates the Release Profile in the parent POM, which includes GPG signing and Central Publishing plugin.
- Ensure your `~/.m2/settings.xml` contains Sonatype credentials:

```
<servers>
    <server>
        <id>central</id>
        <username>YourSonatypeUsername</username>
        <password>YourOSSRHToken</password>
    </server>
</servers>
```

- Make sure your local GPG key is available and unlocked to avoid signing failures.

------

### 4. Important Notes

1. **Submodule versions** are controlled by the parent POM. No changes are needed in submodules.
2. **GPG signing failures** usually happen if the key is locked or requires a passphrase. Use `gpg-agent` or `--pinentry-mode loopback` to resolve.
3. **Do not execute `mvn deploy` without `-P release`**, as it may fail due to missing `<distributionManagement>` or attempt unintended deployment.

------

### 5. References

- TrueLicense Official Documentation
- [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)
- Central Publishing Maven Plugin
- [GnuPG Manual](https://gnupg.org/documentation/manuals/gnupg/)

---

## Notes

* Public key files must be accessible (classpath or filesystem).
* Hardware detection may require root/admin privileges on Linux/Windows.
* Cached verification only stores successful verifications; failures are checked immediately.

---

## License

This project is licensed under the **MIT License**.
