# OriginalKeen License Spring Boot Auto-Configuration

**OriginalKeen License Spring Boot Auto-Configuration** is a Spring Boot starter module that provides **automatic license verification** for Java applications. It supports **hardware-bound licenses**, including CPU, motherboard, IP, and MAC addresses, and integrates seamlessly with Spring Boot applications.

---

## Features

* **Automatic License Verification**: Validate license files during application startup or on demand.
* **Hardware Binding**: License can be bound to specific hardware (CPU, motherboard, IP, MAC).
* **Web Interceptor Support**: Integrate license verification for HTTP requests.
* **Cross-platform Support**: Works on Windows and Linux.
* **Configurable**: Enable/disable verification, customize license path, and configure web interceptor paths.
* **Caching**: License verification results are cached for a short duration to improve performance.

---

## Installation

Add the module dependency to your Spring Boot project:

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-spring-boot-autoconfigure</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Configuration Properties

The module can be configured via `application.yml` or `application.properties`.

| Property                                     | Default | Description                                         |
| -------------------------------------------- | ------- | --------------------------------------------------- |
| `originalkeen.license.enabled`               | `true`  | Enable license verification                         |
| `originalkeen.license.web-enabled`           | `true`  | Enable web request interception                     |
| `originalkeen.license.subject`               | -       | License subject (required if enabled)               |
| `originalkeen.license.license-path`          | -       | Path to the license file                            |
| `originalkeen.license.public-alias`          | -       | Public key alias                                    |
| `originalkeen.license.public-key-store-path` | -       | Public key file path                                |
| `originalkeen.license.public-password`       | -       | Public key password                                 |
| `originalkeen.license.exclude-paths`         | `[]`    | List of URL patterns excluded from web interception |

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

The module automatically installs the license file on application startup if `originalkeen.license.enabled=true` and `license-path` is configured.

### Programmatic License Installation

You can manually install a license at runtime:

```java
@Autowired
private LicenseVerifyService licenseVerifyService;

public void installLicense() {
    licenseVerifyService.install("/path/to/license.lic");
}
```

### License Verification

Verify license programmatically:

```java
boolean valid = licenseVerifyService.verify();
if (!valid) {
    throw new RuntimeException("License verification failed");
}
```

### Web Request Interceptor

When `web-enabled` is true, all HTTP requests are intercepted and license is verified.
You can configure excluded paths via `exclude-paths` property. Requests failing license verification will return **HTTP 403 Forbidden**.

---

## Hardware Binding

The license system supports hardware binding:

* **CPU Serial**
* **Motherboard Serial**
* **IP Addresses**
* **MAC Addresses**

If the license does not match the current hardware, verification fails.

---

## Extending

You can provide a custom `HardwareDataProvider` bean to override hardware detection logic:

```java
@Bean
@ConditionalOnMissingBean(HardwareDataProvider.class)
public HardwareDataProvider hardwareDataProvider() {
    return new CustomHardwareProvider();
}
```

---

## Logging

All license events and warnings are logged using **Log4j2**.

* License installation success/failure
* License verification success/failure
* License expiration warnings (15 days before expiry)

---

## Notes

* Ensure your public key file is accessible (classpath or file path).
* The module caches successful verification results for **60 seconds** to improve performance.
* Hardware detection may require elevated permissions on Linux for CPU or motherboard serial access.

---

## License

This project is licensed under the MIT License.
