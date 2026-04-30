# V2 Interface Finalization Draft

Status: implementation-oriented recommendation draft.

This document turns the V2 runtime idea into a more concrete interface recommendation. It focuses on two areas:

- the final public shape of `LicenseRuntime`
- the Spring compatibility strategy during the V2 transition

## 1. Finalize the Runtime Surface as Small but Complete

Recommendation:

- keep the public runtime interface intentionally small
- cover the common lifecycle directly
- avoid exposing low-level or duplicate methods in V2.0

Suggested public interface:

```java
public interface LicenseRuntime {

    static LicenseRuntimeBuilder builder() {
        throw new UnsupportedOperationException("factory method sketch");
    }

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

### Why This Set Is Enough

- `install(String licensePath)` covers manual install and admin-driven replacement flows.
- `installIfPresent()` covers the startup-friendly convenience path when a configured path may or may not exist.
- `verify()` covers diagnostics, dashboards, tests, and result-driven flows.
- `verifyOrThrow()` covers fail-fast protection paths.
- `isValid()` gives a lightweight shortcut without becoming the main contract.
- `currentHardwareInfo()` is operationally useful and aligns with existing client-info scenarios.
- `config()` is useful for observability and advanced integration code, but it should expose a sanitized config view rather than raw secrets.

## 2. Methods That Should Not Be Public in V2.0

Recommendation:

- do not expose every internal capability directly in the first runtime API

Methods or concepts that should stay internal for now:

- `reloadIfChanged()`
- `installConfiguredLicense()`
- explicit cache control methods
- uninstall operations
- direct exposure of low-level TrueLicense objects

Why:

- `verify()` should already incorporate the configured hot reload behavior
- `installIfPresent()` is easier to understand than separate configured install variants
- uninstall semantics are not part of the main protection story and can complicate compatibility guarantees
- every extra public method becomes long-term API surface to maintain

## 3. Finalize the Meaning of `installIfPresent()`

Recommendation:

- `installIfPresent()` should return `true` when an install was attempted and completed
- it should return `false` when no configured license path is present or the path is not currently usable
- it should throw `LicenseInstallationException` when a configured path exists but installation fails

Suggested semantics:

| Situation | Behavior |
| --- | --- |
| no configured `licensePath` | return `false` |
| configured path missing or unreadable during optional install flow | return `false` |
| configured path readable and install succeeds | return `true` |
| configured path readable but install fails | throw `LicenseInstallationException` |

Why:

- this keeps optional startup flows simple
- it distinguishes "nothing to do" from "real install failure"
- it avoids treating every absent file as a fatal bootstrap error

## 4. Finalize the Verification Result Shape

Recommendation:

- keep one main result type
- keep top-level failure codes stable
- keep detailed mismatch information optional

Suggested result fields:

```java
public interface LicenseVerificationResult {

    boolean isValid();

    LicenseFailureCode getFailureCode();

    LicenseMismatchType getMismatchType();

    String getMessage();

    Instant getCheckedAt();

    Instant getExpiresAt();

    Long getDaysRemaining();

    boolean isFromCache();

    boolean isReloaded();
}
```

Recommended enums:

```java
public enum LicenseFailureCode {
    NOT_INSTALLED,
    LICENSE_FILE_MISSING,
    EXPIRED,
    SIGNATURE_INVALID,
    HARDWARE_MISMATCH,
    CONFIGURATION_ERROR,
    INSTALLATION_ERROR,
    UNKNOWN_ERROR
}

public enum LicenseMismatchType {
    IP,
    MAC,
    CPU,
    MAIN_BOARD
}
```

Why:

- `LicenseFailureCode` stays stable and broad
- `LicenseMismatchType` gives hardware detail without exploding the top-level error surface
- callers can branch on a small set of top-level outcomes while still rendering useful operational messages

## 5. Finalize the Exception Strategy

Recommendation:

- keep a small exception hierarchy
- avoid one public exception class per failure code

Suggested exception model:

```java
public class LicenseRuntimeException extends RuntimeException { }

public class LicenseConfigurationException extends LicenseRuntimeException { }

public class LicenseInstallationException extends LicenseRuntimeException { }

public class LicenseVerificationException extends LicenseRuntimeException {

    private final LicenseFailureCode failureCode;

    private final LicenseMismatchType mismatchType;
}
```

Suggested behavior:

- runtime creation failures should throw `LicenseConfigurationException`
- manual or optional install failures should throw `LicenseInstallationException`
- `verifyOrThrow()` should throw `LicenseVerificationException`

Why:

- this keeps public exception handling straightforward
- the failure code carries the fine-grained reason instead of multiplying exception classes

## 6. Introduce a Runtime Customizer Extension Point

Recommendation:

- add a runtime-level customization hook rather than continuing to encourage Spring users to override low-level beans

Suggested contract:

```java
public interface LicenseRuntimeCustomizer {

    void customize(LicenseRuntimeBuilder builder);
}
```

How it helps:

- plain Java users can call the builder directly
- Spring Boot can discover and apply ordered customizers
- advanced configuration becomes possible without pushing users back down into `LicenseParam`

## 7. Spring Bean Finalization for V2.0

Recommendation:

- make `LicenseRuntime` the main documented Spring bean
- keep a small compatibility layer for existing users

Suggested bean categories:

### Primary documented beans

- `LicenseRuntime`
- `LicenseProperties`
- `HardwareDataProvider`
- `LicenseRuntimeCustomizer`

### Compatibility beans kept in V2.0

- `LicenseVerifyService`

### Advanced or no-longer-documented beans

- `LicenseParam`
- `LicenseManagerAdapter`

Why:

- existing applications may already inject `LicenseVerifyService`
- `LicenseRuntime` should become the mental model for new integrations
- leaving `HardwareDataProvider` overridable preserves the most useful extension point
- discouraging direct `LicenseParam` and `LicenseManagerAdapter` usage helps separate public API from internal assembly

## 8. Recommended Spring Transition Policy

Recommendation:

- V2.0 should preserve compatibility where practical
- documentation should move faster than code removal

Suggested transition posture:

| Version Line | Recommendation |
| --- | --- |
| first V2 release | add `LicenseRuntime`, keep compatibility bean exposure |
| later V2 minor releases | document `LicenseVerifyService` as compatibility or advanced usage |
| next major review point | decide whether any compatibility beans should be deprecated or removed |

## 9. Finalize the Spring Startup Story

Recommendation:

- Spring startup should call `installIfPresent()` and then rely on runtime verification from request or application guard paths
- missing `licensePath` should not fail the app by default
- actual install failures should still fail startup when install was attempted from a valid configured path

Why:

- this preserves the current tolerant startup model
- it avoids blocking startup in environments where the license is mounted later
- it still fails loudly when a real configured install attempt breaks

## 10. What to Implement First

Recommended implementation order:

1. add `originalkeen-license-runtime`
2. implement `LicenseRuntimeConfig`, builder, result, and exceptions
3. wrap `LicenseVerifyService` internally
4. update Spring auto-configuration to create and expose `LicenseRuntime`
5. keep `LicenseVerifyService` bean as compatibility output
6. update docs to present `runtime` and `starter` as the two primary entry points
