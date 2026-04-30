# V2 Acceptance Checklist

Status: review and implementation acceptance checklist. Use [PLAN.md](PLAN.md) for the current execution snapshot.

This document defines what "done enough" should mean for the first V2 release line. It is intended to keep the team aligned when design starts turning into code.

## 1. Plain Java Acceptance

- A plain Java application can create `LicenseRuntime` through a small builder API.
- Plain Java callers do not need to assemble `Preferences`, `LicenseParam`, `DefaultCipherParam`, or `FileKeyStoreParam` manually.
- `verifyOrThrow()` works as the primary fail-fast entry point.
- `verify()` returns a structured result with stable failure codes.
- `currentHardwareInfo()` returns the same hardware model contract already used by the project.

## 2. Spring Acceptance

- The starter remains the recommended Spring Boot entry point.
- Spring auto-configuration builds and exposes `LicenseRuntime`.
- Startup installation uses `runtime.installIfPresent()`.
- Servlet filtering stays in the Spring layer.
- `HardwareDataProvider` remains overridable by user beans.
- `LicenseRuntimeCustomizer` beans can refine runtime creation.

## 3. Compatibility Acceptance

- Existing property names under `originalkeen.license.*` remain unchanged.
- Existing Spring applications can upgrade without rewriting their property files.
- Existing users that inject `LicenseVerifyService` still work in the first V2 release.
- Direct `core` users are not broken, even if documentation starts steering new users toward `runtime`.

## 4. Operational Acceptance

- Missing configured `licensePath` does not fail startup by default.
- Readable configured `licensePath` still triggers startup installation when requested by the runtime flow.
- Verification success caching remains effective.
- Configured license file hot reload remains effective.
- Unsupported operating systems fail fast unless a custom `HardwareDataProvider` is supplied.

## 5. Failure Model Acceptance

- Callers can reliably distinguish at least the following cases:
  - `NOT_INSTALLED`
  - `LICENSE_FILE_MISSING`
  - `EXPIRED`
  - `SIGNATURE_INVALID`
  - `HARDWARE_MISMATCH`
  - `CONFIGURATION_ERROR`
  - `INSTALLATION_ERROR`
  - `UNKNOWN_ERROR`
- Hardware mismatch details can distinguish `IP`, `MAC`, `CPU`, and `MAIN_BOARD`.
- Runtime exceptions do not leak raw TrueLicense types as the main public contract.

## 6. Module Boundary Acceptance

- `core` still has no Spring dependency.
- `runtime` depends on `core`, but `core` does not depend on `runtime`.
- Spring auto-configuration depends on `runtime` for its primary flow.
- Web-only defaults remain outside `runtime`.

## 7. Documentation Acceptance

- Root docs clearly explain the current runtime-first repository story and any compatibility posture for existing users.
- `docs/v2` contains a readable path from architecture to implementation blueprint.
- Module READMEs stay accurate for the current released modules.
- The non-Spring story is no longer implied only through expert-level `core` assembly docs.

## 8. Suggested Verification Matrix

Recommended implementation validation scenarios:

| Scenario | Expected Result |
| --- | --- |
| plain Java with valid installed license | verify succeeds |
| plain Java with no installed license | result shows `NOT_INSTALLED` |
| Spring Boot with blank `licensePath` | startup continues, optional install is skipped |
| Spring Boot with valid `licensePath` | startup install succeeds |
| Spring Boot with changed license file | next verification reloads the file |
| unsupported OS with no custom provider | runtime creation fails fast |
| unsupported OS with custom provider | runtime creation succeeds |

## 9. Release Readiness Questions

Before calling V2 ready, the team should be able to answer "yes" to all of these:

- Can a new plain Java user start from `LicenseRuntime` without consulting `core` internals?
- Can an existing Spring Boot user upgrade without major configuration churn?
- Is the Spring module thinner than before instead of merely rearranged?
- Is the public runtime surface small enough to support long-term compatibility?
- Are the docs aligned on one primary story instead of mixing several competing entry points?

## 10. Current Repository Review on 2026-04-30

| Area | Status | Notes |
| --- | --- | --- |
| Plain Java acceptance | satisfied | `LicenseRuntime` exists, the builder/config/result model is implemented, and runtime tests now cover a real valid license artifact flowing through runtime-first verification and cache behavior. |
| Spring acceptance | satisfied | Spring auto-configuration now exposes `LicenseRuntime`, startup delegates to `runtime.installIfPresent()`, the servlet filter stays in Spring, `HardwareDataProvider` remains overridable, and `LicenseRuntimeCustomizer` is supported. |
| Compatibility acceptance | satisfied for the first V2 release line | Property names remain unchanged, `LicenseVerifyService` is still exposed as a compatibility bean, and direct `core` usage remains available. |
| Operational acceptance | satisfied | Cache and hot reload behavior are covered at the `core` service level, unsupported-OS fail-fast behavior is covered in runtime tests, and runtime end-to-end coverage now exercises a real license artifact through reload and verification. |
| Failure model acceptance | satisfied | Runtime translation tests cover missing-file, expiry, hardware mismatch, and signature-invalid paths, while runtime installation tests now cover installation-failure mapping. |
| Module boundary acceptance | satisfied | `core` remains Spring-free, `runtime` depends on `core`, Spring depends on `runtime`, and Web-only defaults now live in the Spring layer. |
| Documentation acceptance | satisfied | Root, runtime, core, starter, auto-configuration, migration, and V2 status docs now tell one runtime-first story. |

## 11. Release Gap Closure Record on 2026-04-30

- Closed: added a real-license runtime end-to-end test path that verifies a generated license artifact through runtime-managed file reload and success caching.
- Closed: added dedicated translator coverage for `SIGNATURE_INVALID`, including nested root-cause classification.
- Closed: added dedicated runtime coverage for installation-failure mapping through `installIfPresent()`.
- Closed: reran full reactor validation on 2026-04-30 with JDK 17, workspace-local Maven settings, and the Surefire Windows compatibility flags required by this environment.
