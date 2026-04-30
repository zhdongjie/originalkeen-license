# V2 Builder and Config Finalization Draft

Status: implementation-oriented recommendation draft.

This document finalizes the proposed `LicenseRuntimeBuilder` and `LicenseRuntimeConfig` behavior for V2.0. The main goal is to keep runtime creation easy for plain Java users while keeping the exposed config view safe and stable.

## 1. Design Goals

- make runtime creation simple for normal applications
- keep the public config surface small
- keep secrets out of the observable config view
- preserve the current tolerant startup behavior around `licensePath`
- separate runtime config from Spring-only Web config

## 2. Final Builder Responsibilities

`LicenseRuntimeBuilder` should be responsible for:

- collecting required runtime inputs
- normalizing and validating caller input
- resolving the effective hardware provider when none is supplied
- creating the internal low-level bootstrap objects
- producing a sanitized `LicenseRuntimeConfig` snapshot for `runtime.config()`

It should not be responsible for:

- servlet filter policy
- HTTP exclude paths
- Spring bean lifecycle
- exposing low-level TrueLicense objects directly

## 3. Recommended Builder API

Suggested builder methods:

```java
public interface LicenseRuntimeBuilder {

    LicenseRuntimeBuilder subject(String subject);

    LicenseRuntimeBuilder licensePath(String licensePath);

    LicenseRuntimeBuilder publicAlias(String publicAlias);

    LicenseRuntimeBuilder publicKeyStorePath(String publicKeyStorePath);

    LicenseRuntimeBuilder publicPassword(String publicPassword);

    LicenseRuntimeBuilder publicPassword(char[] publicPassword);

    LicenseRuntimeBuilder hardwareDataProvider(HardwareDataProvider hardwareDataProvider);

    LicenseRuntimeBuilder preferencesNodeName(String preferencesNodeName);

    LicenseRuntime build();
}
```

### Why Both `String` and `char[]`

Recommendation:

- support `String` for convenience and Spring property mapping
- support `char[]` for more security-aware plain Java integrations

Suggested behavior:

- if both password setters are called, the last call wins
- builders should defensively copy incoming `char[]`
- public config objects must never expose the password value back out

## 4. Final Runtime Config Shape

`LicenseRuntimeConfig` should be a sanitized, immutable view of runtime settings. It should be safe to log carefully and safe to expose through operational diagnostics.

Suggested interface:

```java
public interface LicenseRuntimeConfig {

    String getSubject();

    String getLicensePath();

    String getPublicAlias();

    String getPublicKeyStorePath();

    boolean hasPublicPassword();

    String getRequestedPreferencesNodeName();

    String getEffectivePreferencesNodeName();

    String getRequestedHardwareProviderClassName();

    String getEffectiveHardwareProviderClassName();

    boolean isHotReloadEnabled();
}
```

### Important Rule

`LicenseRuntimeConfig` should not expose:

- raw password text
- raw password bytes or char arrays
- internal TrueLicense parameter objects

Why:

- `config()` is intended for observability and advanced integration, not secret retrieval
- exposing the password would make the convenience API unsafe by default

## 5. Final Field Set for V2.0

### Required Inputs

- `subject`
- `publicAlias`
- `publicKeyStorePath`
- `publicPassword`

### Optional Inputs

- `licensePath`
- `hardwareDataProvider`
- `preferencesNodeName`

### Explicitly Not in the Public Runtime Config for V2.0

- `webEnabled`
- `excludePaths`
- cache duration overrides
- explicit hot reload switch
- expiry warning threshold

Why:

- `webEnabled` and `excludePaths` are Spring Web policy, not runtime engine configuration
- advanced engine knobs should stay internal until there is a real compatibility need

## 6. Default Values and Derived Behavior

Recommended defaults:

| Field | Default Recommendation |
| --- | --- |
| `licensePath` | `null` when blank or missing |
| `hardwareDataProvider` | auto-resolve built-in Windows or Linux provider |
| `preferencesNodeName` | `/org/eu/originalkeen/license/runtime` |
| hot reload | enabled when `licensePath` is not null |

### Why This Preferences Default

Recommendation:

- use a stable explicit node name rather than inferring it from an implementation class

Why:

- it avoids accidental storage changes if internal class names or packages shift later
- it gives the runtime module a stable persistence identity

## 7. Normalization Rules

Before validation, the builder should normalize input with these rules:

- trim all incoming string fields
- convert blank `licensePath` to `null`
- reject blank required fields after trimming
- keep `classpath:` paths as-is after trimming
- normalize blank `preferencesNodeName` to default rather than preserving an empty value

Examples:

| Input | Normalized Result |
| --- | --- |
| `"  MyApp  "` for `subject` | `"MyApp"` |
| `"   "` for `licensePath` | `null` |
| `" classpath:publicKey.keystore "` | `"classpath:publicKey.keystore"` |
| `null` for `preferencesNodeName` | default node name |

## 8. Validation Rules

Runtime creation should validate in this order:

1. required text fields are present after trimming
2. a password has been supplied through either password setter
3. `publicKeyStorePath` is not blank
4. if `publicKeyStorePath` starts with `classpath:`, the remaining resource segment is not blank
5. if no custom `HardwareDataProvider` is supplied, the current operating system is supported by built-in providers

### Important Non-Validation Rule

Do not require `licensePath` to exist during build.

Why:

- current startup semantics intentionally allow the app to boot before the license file is mounted
- build-time validation should not break that deployment model

## 9. Recommended Secret Handling

V2.0 should follow these rules:

- builder accepts secrets, runtime internals use them, public config does not reveal them
- `toString()` on config or runtime-related classes must not print password values
- if a `char[]` password is provided, builders should copy it on input
- internal bootstrap logic should convert secrets only as needed for compatibility with existing low-level classes

Practical note:

- Spring properties still arrive as `String`, so V2.0 is not a full secret-management redesign
- the main goal here is to avoid making the new public API worse than necessary

## 10. Spring Mapping Rules

Spring should map `LicenseProperties` into the runtime builder like this:

| Spring Property or Bean | Runtime Builder Target |
| --- | --- |
| `subject` | `subject(...)` |
| `licensePath` | `licensePath(...)` |
| `publicAlias` | `publicAlias(...)` |
| `publicKeyStorePath` | `publicKeyStorePath(...)` |
| `publicPassword` | `publicPassword(...)` |
| `HardwareDataProvider` bean | `hardwareDataProvider(...)` |

These Spring-only fields should remain outside runtime config:

| Spring Field | Reason |
| --- | --- |
| `webEnabled` | servlet enforcement policy |
| `excludePaths` | servlet filter policy |

## 11. Spring Customizer Application Order

Recommended Spring creation order:

1. create builder
2. apply values from `LicenseProperties`
3. apply discovered `HardwareDataProvider` bean if user supplied one
4. apply ordered `LicenseRuntimeCustomizer` beans
5. build runtime

Why this order:

- property binding establishes the baseline
- explicit bean overrides and customizers can then refine it
- customizers become the clean advanced extension point

If multiple customizers exist:

- Spring should apply them using standard ordering semantics

## 12. Multi-Runtime Usage Guidance

V2 should support multiple runtime instances in one JVM.

Recommendation:

- default preferences node is acceptable for normal single-runtime applications
- applications with multiple distinct runtime instances may override `preferencesNodeName` for stronger isolation

Typical cases for overriding:

- one process hosts multiple protected products
- one process verifies both production and test licenses side by side
- one process needs isolated license state during migration or testing

## 13. Final Recommendation Summary

The recommended V2.0 builder and config posture is:

- small builder API
- small sanitized config view
- fail-fast runtime creation for real configuration problems
- tolerant handling of missing `licensePath`
- no Web policy fields in runtime config
- no secret exposure through `config()`

