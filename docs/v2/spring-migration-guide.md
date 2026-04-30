# Spring Migration Guide

Status: implementation-aligned migration notes for the first V2 release line.

This document is for teams already using the Spring Boot starter or auto-configuration and wanting a clear path into the runtime-backed model without unnecessary churn.

## 1. What Stays the Same

- The recommended Spring entry point is still `originalkeen-license-spring-boot-starter`.
- The property namespace is still `originalkeen.license.*`.
- Startup still performs an optional install from `license-path`.
- Servlet request protection still lives in the Spring layer.
- `HardwareDataProvider` is still overridable through a user bean.

## 2. What Changed

The primary application-facing bean is now `LicenseRuntime`.

The Spring module no longer defines the main product shape by wiring the low-level `core` path directly. Instead, it adapts Spring Boot onto the shared runtime bootstrap path.

Current bean posture:

| Bean Type | Status in the First V2 Release Line |
| --- | --- |
| `LicenseRuntime` | preferred primary bean |
| `LicenseVerifyService` | compatibility bean, still available |
| `LicenseManagerAdapter` | advanced bean, not the default application API |
| `LicenseParam` | no longer the default Spring extension path |

## 3. No-Change Upgrade Path

If your application only:

- imports the starter
- sets `originalkeen.license.*` properties
- relies on startup install and servlet enforcement

then your normal upgrade path is expected to be a dependency upgrade with no property-file rewrite.

## 4. Bean Injection Migration

### Before

```java
@Service
public class LegacyLicenseService {

    private final LicenseVerifyService licenseVerifyService;

    public LegacyLicenseService(LicenseVerifyService licenseVerifyService) {
        this.licenseVerifyService = licenseVerifyService;
    }
}
```

### Preferred After

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

Compatibility posture:

- existing `LicenseVerifyService` injection can remain in place during the first V2 release line
- new application code should prefer `LicenseRuntime`

## 5. Custom Provider Migration

If you already provide a custom `HardwareDataProvider`, keep doing that. The runtime-backed Spring path still consumes it.

```java
@Bean
public HardwareDataProvider hardwareDataProvider() {
    return new CustomHardwareProvider();
}
```

## 6. Advanced Spring Customization

If you only need to refine runtime creation within the public builder surface, use `LicenseRuntimeCustomizer`.

```java
@Bean
public LicenseRuntimeCustomizer licenseRuntimeCustomizer() {
    return builder -> builder.preferencesNodeName("/my/company/license");
}
```

If you previously relied on deep custom `LicenseParam` wiring that cannot be expressed through properties, provider selection, or `LicenseRuntimeCustomizer`, provide your own `LicenseRuntime` bean and bypass the default runtime assembly path.

## 7. Operational Semantics to Keep in Mind

- `enabled=false` prevents runtime bean creation.
- A blank or unreadable `license-path` skips optional install instead of failing startup.
- A changed readable configured license file is reloaded on the next verification path before the success cache is reused.
- Servlet exclude paths remain a Spring-only concern and are not part of `LicenseRuntimeConfig`.

## 8. Suggested Team Migration Order

1. Upgrade dependencies without changing property files.
2. Leave existing `LicenseVerifyService` injection in place if that keeps the upgrade low-risk.
3. Move new application code to `LicenseRuntime`.
4. Replace old low-level Spring customizations with `HardwareDataProvider`, `LicenseRuntimeCustomizer`, or a custom `LicenseRuntime` bean as appropriate.
5. Reserve direct `core` assembly for expert or infrastructure-only use cases.
