# V2 Runtime API

Status: implementation-aligned baseline.

## Goals

- Provide a first-class Java API with no Spring dependency.
- Hide low-level assembly details such as `Preferences`, `DefaultLicenseParam`, and keystore bootstrapping.
- Support both simple use cases and advanced extension points.
- Preserve the existing verification behavior in `core`, including caching and hot reload, while exposing a clearer public contract.

## Artifact and Package

- Artifact: `originalkeen-license-runtime`
- Base package: `org.eu.originalkeen.license.runtime`

## Public Types

| Type | Responsibility |
| --- | --- |
| `LicenseRuntime` | Main entry point for install, verify, and hardware inspection operations |
| `LicenseRuntimeBuilder` | Fluent builder for runtime creation |
| `LicenseRuntimeConfig` | Immutable runtime configuration snapshot |
| `LicenseVerificationResult` | Structured verification result |
| `LicenseFailureCode` | Stable failure taxonomy for callers |
| `LicenseMismatchType` | Optional hardware mismatch detail for callers |
| `LicenseRuntimeException` | Base runtime exception |
| `LicenseConfigurationException` | Configuration or bootstrap problem |
| `LicenseInstallationException` | Installation failure |
| `LicenseVerificationException` | Verification failure when exception flow is preferred |

## Core API

```java
public interface LicenseRuntime {

    LicenseRuntimeConfig config();

    LicenseCheckModel currentHardwareInfo();

    void install(String licensePath);

    boolean installIfPresent();

    LicenseVerificationResult verify();

    void verifyOrThrow();

    default boolean isValid() {
        return verify().isValid();
    }
}
```

Builder example:

```java
LicenseRuntime runtime = LicenseRuntime.builder()
        .subject("MyAppLicense")
        .licensePath("/opt/licenses/myapp.lic")
        .publicAlias("public")
        .publicKeyStorePath("classpath:publicKey.keystore")
        .publicPassword("changeit1")
        .build();
```

## Configuration Model

Configuration inputs:

| Field | Required | Notes |
| --- | --- | --- |
| `subject` | yes | license subject passed into the low-level license parameter |
| `licensePath` | no | optional configured install and hot reload path |
| `publicAlias` | yes | public key alias |
| `publicKeyStorePath` | yes | classpath or filesystem location |
| `publicPassword` | yes | public keystore password |
| `hardwareDataProvider` | no | overrides default OS provider resolution |
| `preferencesNodeName` | no | advanced override for storage namespace |

Builder behavior:

- auto-select Windows or Linux providers when no provider is supplied
- fail fast on unsupported operating systems unless a custom provider is registered
- normalize blank `licensePath` values to `null`
- keep hot reload as derived behavior when `licensePath` is present
- expose a sanitized config view rather than returning secrets through `config()`

## Result Model

Result fields:

| Field | Meaning |
| --- | --- |
| `valid` | whether verification succeeded |
| `failureCode` | stable failure category when invalid |
| `mismatchType` | optional hardware mismatch detail when `failureCode=HARDWARE_MISMATCH` |
| `message` | caller-facing summary |
| `checkedAt` | verification timestamp |
| `expiresAt` | license expiration time when available |
| `daysRemaining` | convenience field for UI or logging |
| `fromCache` | whether success came from the short-lived verification cache |
| `reloaded` | whether a configured license file was reloaded before verification |

Failure codes:

- `NOT_INSTALLED`
- `LICENSE_FILE_MISSING`
- `EXPIRED`
- `SIGNATURE_INVALID`
- `HARDWARE_MISMATCH`
- `CONFIGURATION_ERROR`
- `INSTALLATION_ERROR`
- `UNKNOWN_ERROR`

Mismatch detail values:

- `IP`
- `MAC`
- `CPU`
- `MAIN_BOARD`

## Usage Examples

### Simple Plain Java Usage

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

### Result-Oriented Usage

```java
LicenseVerificationResult result = runtime.verify();
if (!result.isValid()) {
    System.out.println(result.getFailureCode());
    System.out.println(result.getMessage());
}
```

### Custom Provider Usage

```java
LicenseRuntime runtime = LicenseRuntime.builder()
        .subject("MyAppLicense")
        .publicAlias("public")
        .publicKeyStorePath("/data/keys/publicKey.keystore")
        .publicPassword("changeit1")
        .hardwareDataProvider(new CustomHardwareProvider())
        .build();
```

## Error Model

Public behavior:

- `verify()` returns a structured result and should not force callers to understand TrueLicense exceptions.
- `verifyOrThrow()` converts invalid states into OriginalKeen runtime exceptions.
- install-related errors throw `LicenseInstallationException`.
- invalid configuration throws `LicenseConfigurationException` during runtime creation rather than much later.
- `config()` returns a sanitized observable view, not the raw password value.

## Thread Safety and Lifecycle

- `LicenseRuntime` is thread-safe and reusable as an application singleton.
- The runtime internally reuses the existing concurrency protections already present in `core`.
- Spring Boot exposes it as a singleton bean.

## Relationship to `core`

V2 does not rewrite the verification engine. `runtime` wraps existing `core` classes:

- it uses `FileKeyStoreParam` and `LicenseManagerAdapter` to build the low-level verification stack
- it uses `LicenseVerifyService` internally for installation, caching, and hot reload
- it translates low-level success and failure details into runtime result objects and exceptions
