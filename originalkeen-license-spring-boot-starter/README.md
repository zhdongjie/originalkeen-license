# OriginalKeen License Spring Boot Starter

`originalkeen-license-spring-boot-starter` is the recommended Spring Boot entry point for OriginalKeen License. It pulls in the runtime-backed auto-configuration and exposes the simplest application-facing integration path.

## Features

- Zero-boilerplate Spring Boot integration
- Runtime-backed startup installation through `LicenseRuntime`
- Servlet request interception with configurable exclusions
- Verification success cache and configured license file hot reload support
- `LicenseRuntime` as the primary application-facing bean
- Compatibility exposure for `LicenseVerifyService` in the first V2 release line
- Extension points for custom hardware providers and ordered `LicenseRuntimeCustomizer` beans

## Recommended Installation

Import the BOM and then add the starter dependency without repeating versions:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eu.originalkeen</groupId>
            <artifactId>originalkeen-license-dependencies</artifactId>
            <version>1.3.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

If you are not using the BOM, declare the starter version explicitly:

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Configuration Properties

| Property | Default | Description |
| --- | --- | --- |
| `originalkeen.license.enabled` | `true` | Globally enables license support. |
| `originalkeen.license.web-enabled` | `true` | Enables servlet request interception. |
| `originalkeen.license.subject` | required when enabled | License subject passed to the runtime builder. |
| `originalkeen.license.license-path` | blank | Optional startup install and hot reload path. If blank or unreadable, startup skips optional install. |
| `originalkeen.license.public-alias` | required when enabled | Public key alias in the keystore. |
| `originalkeen.license.public-key-store-path` | required when enabled | Public keystore path. Supports `classpath:` and filesystem paths. |
| `originalkeen.license.public-password` | required when enabled | Password for the public keystore and cipher parameter. |
| `originalkeen.license.exclude-paths` | custom list merged with built-in defaults | Extra servlet paths excluded from verification. |

Built-in servlet exclusions always include `/actuator/**`, `/error`, `/favicon.ico`, and common static resource patterns such as CSS, JS, HTML, and image files.

Example:

```yaml
originalkeen:
  license:
    enabled: true
    web-enabled: true
    subject: "MyAppLicense"
    license-path: "/opt/licenses/myapp.lic"
    public-alias: "public"
    public-key-store-path: "classpath:publicKey.keystore"
    public-password: "changeit1"
    exclude-paths:
      - /login
      - /api/public/**
```

## Application Usage

Inject `LicenseRuntime` when application code needs explicit checks or hardware inspection:

```java
@Service
public class LicenseGuardService {

    private final LicenseRuntime licenseRuntime;

    public LicenseGuardService(LicenseRuntime licenseRuntime) {
        this.licenseRuntime = licenseRuntime;
    }

    public void verifyNow() {
        licenseRuntime.verifyOrThrow();
    }
}
```

## Customization

### Override the Hardware Provider

```java
@Bean
public HardwareDataProvider hardwareDataProvider() {
    return new CustomHardwareProvider();
}
```

### Refine Runtime Creation

```java
@Bean
public LicenseRuntimeCustomizer licenseRuntimeCustomizer() {
    return builder -> builder.preferencesNodeName("/my/company/license");
}
```

If you already inject `LicenseVerifyService`, that compatibility bean remains available in the first V2 release line. New Spring code should prefer `LicenseRuntime`.

## Migration Notes

The starter keeps the same property namespace and the same high-level startup and filter behavior, but the primary bean model has changed:

- preferred bean now: `LicenseRuntime`
- compatibility bean still available: `LicenseVerifyService`
- low-level `LicenseParam` customization is no longer the default extension path

For detailed migration guidance, see [../docs/v2/spring-migration-guide.md](../docs/v2/spring-migration-guide.md).

## Platform Notes

- Built-in runtime providers support Windows and Linux.
- For macOS or other operating systems, register a custom `HardwareDataProvider`.
- Linux hardware collection may require elevated privileges for some serial number lookup strategies.

## License

This project is licensed under the Apache License 2.0.
