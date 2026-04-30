# OriginalKeen License Spring Boot Auto-Configuration

`originalkeen-license-spring-boot-autoconfigure` provides the runtime-backed Spring Boot bean wiring behind OriginalKeen License. Use this module directly only when you want fine-grained dependency control. Most application teams should prefer the starter module.

## What This Module Does Now

This module no longer hand-assembles the primary `core` path directly. Instead, it adapts Spring Boot onto the shared runtime layer and keeps a small compatibility surface for existing applications.

## Features

- Registers a default `HardwareDataProvider` when none is supplied
- Builds and exposes `LicenseRuntime` as the primary Spring bean
- Keeps `LicenseVerifyService` available as a compatibility bean
- Exposes `LicenseManagerAdapter` as an advanced diagnostic bean
- Runs startup installation through `runtime.installIfPresent()`
- Registers a servlet filter that rejects protected requests with HTTP 403 when runtime verification fails
- Applies ordered `LicenseRuntimeCustomizer` beans before runtime creation

## Installation

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-spring-boot-autoconfigure</artifactId>
    <version>1.1.5</version>
</dependency>
```

## Configuration Properties

| Property | Default | Description |
| --- | --- | --- |
| `originalkeen.license.enabled` | `true` | Globally enables license support. When `false`, runtime beans are not created. |
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

## Exposed Beans

| Bean Type | Role |
| --- | --- |
| `LicenseRuntime` | Primary application-facing runtime bean |
| `LicenseVerifyService` | Compatibility bean for existing integrations |
| `LicenseManagerAdapter` | Advanced diagnostic or expert bean |
| `HardwareDataProvider` | Built-in or user-supplied hardware provider |
| `LicenseRuntimeCustomizer` | Builder refinement hook discovered from the Spring context |

## Runtime Behavior

- Startup installation runs when `enabled=true`.
- A blank or unreadable `license-path` logs a warning and skips optional install instead of failing startup.
- Servlet requests that are not excluded are verified before reaching application code.
- Verification failures return HTTP 403 from the servlet filter.
- If the configured license file changes and is readable, verification reloads it before reusing the short-lived success cache.

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

### Replace the Default Runtime Entirely

If you need deeper low-level control than the builder and customizer allow, provide your own `LicenseRuntime` bean and bypass the default runtime assembly path.

## Migration Notes

- New Spring code should inject `LicenseRuntime`.
- Existing code that injects `LicenseVerifyService` can keep working in the first V2 release line.
- Existing `originalkeen.license.*` property names stay unchanged.

For a fuller migration walkthrough, see [../docs/v2/spring-migration-guide.md](../docs/v2/spring-migration-guide.md).

## Platform Notes

- Built-in runtime providers support Windows and Linux.
- For macOS or other operating systems, register a custom `HardwareDataProvider` bean.
- Linux hardware collection may require elevated privileges for some serial number lookup strategies.

## License

This project is licensed under the Apache License 2.0.
