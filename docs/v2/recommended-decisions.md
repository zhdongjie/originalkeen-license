# V2 Recommended Decisions

Status: recommended defaults for implementation planning.

This document narrows the V2 design into a concrete set of recommendations. It is intended to reduce ambiguity before implementation starts.

## Summary

| Topic | Recommendation |
| --- | --- |
| Primary public entry point | Add `originalkeen-license-runtime` and make `LicenseRuntime` the default public API |
| `core` positioning | Keep public, but document as advanced or expert usage rather than the default path |
| `LicenseVerifyService` | Keep in `core` for V2 and let `runtime` wrap it |
| Main verification style | Prefer `verifyOrThrow()` in primary examples, keep `verify()` and `isValid()` alongside it |
| Failure taxonomy | Use stable high-level failure codes plus optional hardware mismatch details |
| Unsupported OS behavior | Fail fast by default unless a custom `HardwareDataProvider` is supplied |
| Spring bean strategy | Expose `LicenseRuntime` as the primary bean and keep compatibility beans during the transition |
| Framework adapter assembly | Let `runtime` own low-level bootstrap and export an infrastructure-facing assembly bridge for Spring |
| Detailed verification metadata | Keep simple boolean verify for expert usage, but add a richer internal outcome path for `runtime` |
| V2.0 config scope | Keep the public runtime config intentionally small in the first release |

## 1. Position `runtime` as the Primary Product Surface

Recommendation:

- plain Java applications should integrate through `LicenseRuntime`
- Spring Boot applications should integrate through the starter, which internally uses `LicenseRuntime`
- direct use of `core` should still be supported, but documented as advanced usage

Why:

- this gives the project one clean non-Spring API instead of making users assemble low-level pieces
- it avoids rewriting the engine while still changing the public product shape

## 2. Keep `core` Public, but Demote It in Documentation

Recommendation:

- do not deprecate `core` immediately
- do not remove current `core` APIs in the same iteration that introduces `runtime`
- update documentation so `runtime` is the default recommendation and `core` is described as an expert layer

Why:

- existing advanced users may already wire `core` manually
- immediate deprecation would create noise before the new runtime proves itself

Suggested review point:

- after one or two stable releases with `runtime`, reassess whether any `core` APIs should be formally deprecated

## 3. Keep `LicenseVerifyService` in `core` for V2

Recommendation:

- keep `LicenseVerifyService` where it is for the first V2 implementation
- let `runtime` use it internally for installation, caching, and hot reload
- treat it as an advanced API, not the main entry point for new users

Why:

- this avoids unnecessary churn
- the service already encapsulates useful concurrency and caching behavior
- the real product shift comes from adding a better facade, not from moving classes around immediately

## 4. Make `verifyOrThrow()` the Main Story

Recommendation:

- support all three styles:
  - `verifyOrThrow()`
  - `verify()`
  - `isValid()`
- present `verifyOrThrow()` as the primary pattern in most application-facing examples

Why:

- most protected applications want fail-fast behavior during startup or before sensitive operations
- `verify()` remains valuable for dashboards, admin pages, diagnostics, and tests
- `isValid()` is still a useful convenience for very simple integrations

Suggested meaning:

- `verifyOrThrow()`: application guard path
- `verify()`: structured result path
- `isValid()`: lightweight boolean shortcut

## 5. Use High-Level Failure Codes, Not Over-Specialized Public Exceptions

Recommendation:

- keep public failure codes stable and relatively high-level
- expose optional details for hardware mismatch specifics

Suggested top-level failure codes:

- `NOT_INSTALLED`
- `LICENSE_FILE_MISSING`
- `EXPIRED`
- `SIGNATURE_INVALID`
- `HARDWARE_MISMATCH`
- `CONFIGURATION_ERROR`
- `INSTALLATION_ERROR`
- `UNKNOWN_ERROR`

Suggested optional mismatch details:

- `IP`
- `MAC`
- `CPU`
- `MAIN_BOARD`

Why:

- hardware-specific detail is useful
- a single top-level `HARDWARE_MISMATCH` code is easier to keep stable than many narrowly coupled public codes
- this leaves room for future validation signals without constantly expanding the public enum surface

## 6. Keep the V2.0 Runtime Config Small

Recommendation for the initial public config:

Required:

- `subject`
- `publicAlias`
- `publicKeyStorePath`
- `publicPassword`

Optional:

- `licensePath`
- `hardwareDataProvider`
- `preferencesNodeName`

Keep internal in V2.0 unless a real use case forces exposure:

- verification cache duration
- expiry warning threshold
- explicit hot reload toggle

Why:

- a smaller public config is easier to keep stable
- the first V2 goal is product simplification, not exposing every engine knob
- once options become public, they are much harder to retract or reshape

Default behavior recommendation:

- if `licensePath` is configured, hot reload behavior is enabled automatically
- if `licensePath` is missing, install-related convenience methods should degrade gracefully

## 7. Fail Fast on Unsupported Operating Systems

Recommendation:

- if no custom `HardwareDataProvider` is supplied, the runtime should only auto-resolve built-in Windows and Linux providers
- for unsupported operating systems, runtime creation should fail fast with a clear configuration error

Why:

- this is safer than pretending support exists
- it makes platform boundaries explicit
- it avoids confusing partially initialized runtimes

Not recommended for V2.0:

- a lazy initialization mode that hides platform problems until a later verify call

## 8. Spring Should Expose `LicenseRuntime`, but Keep Compatibility Beans

Recommendation:

- expose `LicenseRuntime` as the main Spring bean
- keep startup install and servlet filtering in auto-configuration
- keep compatibility-level beans for existing users during the V2 transition, especially where current applications may autowire them

Suggested bean posture:

- `LicenseRuntime`: primary documented bean
- `LicenseVerifyService`: compatibility or advanced bean, still available during migration
- `HardwareDataProvider`: still overridable by the user

Why:

- existing Spring users may already inject `LicenseVerifyService`
- removing compatibility beans too early would create avoidable breakage
- the runtime bean gives new users a clearer mental model

## 9. Move Web-Only Defaults Out of `core`

Recommendation:

- servlet filter exclude path defaults should belong to the Spring layer, not `core`

Why:

- those defaults are not part of the license verification engine
- they are request-filter policy, not runtime verification policy
- this change makes future non-Spring adapters easier

## 10. Minimum Success Criteria for V2.0

V2.0 should be considered successful if all of the following are true:

- a plain Java application can integrate through `LicenseRuntime` without assembling `LicenseParam` manually
- the Spring Boot starter internally uses `runtime`
- `runtime` becomes the main documented entry point for non-Spring usage
- servlet defaults and Web enforcement remain in the Spring layer
- existing advanced `core` users are not broken by the first V2 release

## 11. Let `runtime` Own the Bootstrap Boundary

Recommendation:

- `runtime` should own the assembly of `FileKeyStoreParam`, `DefaultLicenseParam`, `LicenseManagerAdapter`, and `LicenseVerifyService`
- Spring should consume that bootstrap result instead of continuing to wire `core` directly
- if needed, `runtime` should export an infrastructure-facing assembly object for framework adapters

Why:

- this removes the last major reason Spring remains the most complete integration path
- it prevents low-level assembly rules from drifting between plain Java and Spring
- it makes compatibility bean exposure possible without duplicating bootstrap logic

## 12. Add a Richer Internal Verification Outcome Instead of Duplicating Logic

Recommendation:

- keep `LicenseVerifyService.verify()` as the simple boolean expert API
- add one richer internal verification outcome path for `runtime`
- do not reimplement cache and hot reload logic in a second place

Why:

- the planned V2 result model needs more than a boolean
- duplicating verify behavior in `runtime` would create long-term drift risk
- a richer internal outcome keeps the public V2 API clean while preserving one operational verification path
