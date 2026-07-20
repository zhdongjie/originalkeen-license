# OriginalKeen License Runtime

`originalkeen-license-runtime` is the preferred plain Java integration path for OriginalKeen License. It wraps the existing verification engine in `core` and exposes a smaller public facade through `LicenseRuntime`.

## Position in the Repository

- Plain Java default path: use `runtime`
- Spring Boot path: use the starter, which internally consumes `runtime`
- Expert layer: use `core` only when you need low-level assembly control

## Features

- `LicenseRuntime` facade for install, verify, and hardware inspection
- Fluent builder that hides `Preferences`, `DefaultLicenseParam`, and keystore bootstrapping
- Structured verification results with stable failure codes
- Reuse of `core` success caching and configured license file hot reload
- Built-in Windows and Linux provider resolution with fail-fast behavior for unsupported operating systems

## Installation

### Maven

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-runtime</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'org.eu.originalkeen:originalkeen-license-runtime:1.3.0'
```

## Common Usage

```java
LicenseRuntime runtime = LicenseRuntime.builder()
        .subject("MyAppLicense")
        .licensePath("/opt/licenses/myapp.lic")
        .publicAlias("public")
        .publicKeyStorePath("classpath:publicKey.keystore")
        .publicPassword("changeit1")
        .build();

runtime.installIfPresent();
runtime.verifyOrThrow();
```

## Result-Oriented Usage

```java
LicenseVerificationResult result = runtime.verify();
if (!result.isValid()) {
    System.out.println(result.getFailureCode());
    System.out.println(result.getMismatchType());
    System.out.println(result.getMessage());
}
```

Important result fields:

- `getFailureCode()`: top-level reason such as `NOT_INSTALLED`, `EXPIRED`, or `HARDWARE_MISMATCH`
- `getMismatchType()`: optional detail for hardware mismatches
- `isFromCache()`: whether success came from the short-lived success cache
- `isReloaded()`: whether a changed configured license file was reloaded before verification

## Custom Provider Usage

If the runtime is used on macOS or another unsupported operating system, register a custom provider explicitly:

```java
LicenseRuntime runtime = LicenseRuntime.builder()
        .subject("MyAppLicense")
        .publicAlias("public")
        .publicKeyStorePath("/data/keys/publicKey.keystore")
        .publicPassword("changeit1")
        .hardwareDataProvider(new CustomHardwareProvider())
        .build();
```

## Runtime Semantics

- `install(String licensePath)` installs the specified license path immediately.
- `installIfPresent()` returns `false` when no configured `licensePath` is available or readable.
- `verify()` returns a structured result and does not force callers into low-level exception handling.
- `verifyOrThrow()` throws `LicenseVerificationException` when verification fails.
- `config()` returns a sanitized configuration snapshot and does not expose the public password.

## When to Use `core` Instead

Stay with `runtime` unless you need one of these expert scenarios:

- direct manual assembly of `LicenseManagerAdapter` or `LicenseVerifyService`
- custom low-level integration around TrueLicense types
- infrastructure code that intentionally depends on internal verification assembly details

For those cases, see [../originalkeen-license-core/README.md](../originalkeen-license-core/README.md).

## License

This project is licensed under the Apache License 2.0.
