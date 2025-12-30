# OriginalKeen License Spring Boot Starter

**OriginalKeen License Spring Boot Starter** is a Spring Boot starter module that **automatically integrates the OriginalKeen License verification system** into your Spring Boot applications. It provides out-of-the-box **license installation, verification, and web request interception** with minimal configuration.

---

## Features

* **Auto-Configuration**: Automatically sets up license verification beans.
* **Hardware Binding**: License verification supports CPU, motherboard, IP, and MAC addresses.
* **Web Interceptor**: Supports HTTP request interception to enforce license checks.
* **Cross-Platform**: Works on Windows and Linux.
* **Caching**: Verification results are cached for improved performance.
* **Customizable**: Override beans like `HardwareDataProvider` or `LicenseParam` as needed.

---

## Installation

Add the starter dependency to your Spring Boot project:

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> This starter automatically pulls in the `originalkeen-license-spring-boot-autoconfigure` module.

---

## Configuration Properties

The starter supports configuration via `application.yml` or `application.properties`.

| Property                                     | Default | Description                                  |
| -------------------------------------------- | ------- | -------------------------------------------- |
| `originalkeen.license.enabled`               | `true`  | Enable license verification                  |
| `originalkeen.license.web-enabled`           | `true`  | Enable web interceptor                       |
| `originalkeen.license.subject`               | -       | License subject (required if enabled)        |
| `originalkeen.license.license-path`          | -       | Path to the license file                     |
| `originalkeen.license.public-alias`          | -       | Public key alias                             |
| `originalkeen.license.public-key-store-path` | -       | Public key file path                         |
| `originalkeen.license.public-password`       | -       | Public key password                          |
| `originalkeen.license.exclude-paths`         | `[]`    | Web paths excluded from license interception |

**Example (application.yml):**

```yaml
originalkeen:
  license:
    enabled: true
    web-enabled: true
    subject: "MyAppLicense"
    license-path: "/opt/licenses/myapp.lic"
    public-alias: "public"
    public-key-store-path: "classpath:publicKey.keystore"
    public-password: "changeit"
    exclude-paths:
      - /login
      - /actuator/**
```

---

## Usage

### Automatic License Installation

The starter automatically installs the license on application startup if:

* `enabled=true`
* `license-path` is configured

### Programmatic License Installation

```java
@Autowired
private LicenseVerifyService licenseVerifyService;

public void installLicense() {
    licenseVerifyService.install("/path/to/license.lic");
}
```

### License Verification

Programmatically check if license is valid:

```java
boolean valid = licenseVerifyService.verify();
if (!valid) {
    throw new RuntimeException("License verification failed");
}
```

### Web Interceptor

If `web-enabled=true`, all HTTP requests are intercepted for license validation.
Excluded paths can be configured using `exclude-paths`. Requests failing license verification will return **HTTP 403 Forbidden**.

---

## Extending and Customization

You can override default beans if needed:

```java
@Bean
@ConditionalOnMissingBean(HardwareDataProvider.class)
public HardwareDataProvider customHardwareProvider() {
    return new CustomHardwareProvider();
}

@Bean
@ConditionalOnMissingBean(LicenseParam.class)
public LicenseParam customLicenseParam(LicenseProperties properties) {
    return new CustomLicenseParam(properties);
}
```

---

## Logging

The starter logs important events:

* License installation success/failure
* License verification success/failure
* License expiration warnings (15 days before expiry)

---

## Notes

* Ensure public key files are accessible via classpath or filesystem.
* Hardware detection may require elevated permissions on Linux.
* Successful verification results are cached for 60 seconds to improve performance.

---

## License

This project is licensed under the MIT License.
