# V2 Plan

Status: active execution plan derived from the current V2 design set.

Last updated: 2026-04-30

This file is the working plan for V2. It does not replace the design documents in this directory. Instead, it turns them into one execution-oriented view so implementation can follow a clear sequence.

## Current Execution Snapshot

| Phase | Status | Current Record |
| --- | --- | --- |
| Phase 0 | completed | runtime public surface locked and the detailed verification path was finalized as `LicenseVerifyService.verifyDetailed()` plus `CoreVerificationOutcome` |
| Phase 1 | completed | `originalkeen-license-runtime` was added with public API, builder/config/result types, exception hierarchy, bootstrap, assembly, translator, and first test coverage |
| Phase 2 | completed | Spring auto-configuration now builds and exposes `LicenseRuntime`, startup and filter paths consume runtime, and `LicenseVerifyService` remains exposed as a compatibility bean |
| Phase 3 | completed | Web-only exclude path defaults were moved out of `core`, runtime config remains free of Spring Web policy, and docs now position `runtime` and `starter` as the default entry points |
| Phase 4 | completed | the last three release gaps were closed on 2026-04-30, `docs/v2` acceptance status was refreshed, and a full reactor validation rerun passed locally |

## 1. Goal

Deliver a V2 line where:

- plain Java applications use a first-class `originalkeen-license-runtime` module
- Spring Boot remains the recommended Spring entry point through the starter
- Spring consumes `runtime` instead of hand-assembling `core`
- `core` remains available as the expert layer
- current users are not broken during the first V2 release

## 2. Planning Assumptions

This plan follows the current converged design decisions:

- `model` stays the shared protocol contract
- `core` stays the low-level engine and keeps `LicenseVerifyService`
- `runtime` is added as the new public non-Spring facade
- Spring Boot auto-configuration becomes an adapter over `runtime`
- Web-only concerns remain in the Spring layer
- migration is incremental, not a rewrite

Supporting documents:

- [architecture.md](architecture.md)
- [runtime-api.md](runtime-api.md)
- [config-finalization.md](config-finalization.md)
- [interface-finalization.md](interface-finalization.md)
- [internal-assembly.md](internal-assembly.md)
- [implementation-blueprint.md](implementation-blueprint.md)
- [migration-plan.md](migration-plan.md)
- [acceptance-checklist.md](acceptance-checklist.md)

## 3. Plan Summary

| Phase | Objective | Main Deliverables |
| --- | --- | --- |
| Phase 0 | lock the verification boundary | final runtime API direction, chosen detailed verification path |
| Phase 1 | introduce `runtime` | new module, public API, builder, config, result, exceptions, bootstrap |
| Phase 2 | switch Spring to `runtime` | runtime-backed auto-configuration, `LicenseRuntime` bean, compatibility bean path |
| Phase 3 | clean boundaries and docs | Web-only policy isolated in Spring, `core` repositioned as expert layer |
| Phase 4 | acceptance and release prep | docs complete, scenarios checked, V2 release candidate judged against acceptance checklist |

## 4. Phase Plan

## Phase 0: Lock the Verification Boundary

Purpose:

- confirm the final runtime surface before code spreads across modules
- avoid duplicating cache and hot reload logic

Main tasks:

- confirm `LicenseRuntime`, `LicenseRuntimeBuilder`, `LicenseRuntimeConfig`, and `LicenseVerificationResult`
- confirm the small public exception hierarchy
- decide how `runtime` receives detailed verification metadata:
  - `LicenseVerifyService.verifyDetailed()`
  - or a separate internal helper near `core`

Deliverables:

- current API and config drafts treated as implementation baseline
- one chosen internal detailed verification path

Exit criteria:

- the team can begin `runtime` coding without unresolved public API churn
- there is a clear plan for structured result generation without reimplementing verification behavior

Current implementation record:

- completed on 2026-04-29 by choosing `LicenseVerifyService.verifyDetailed()` as the single detailed verification path
- completed on 2026-04-29 by adding `CoreVerificationOutcome` so runtime can translate verification metadata without duplicating cache or hot reload behavior

## Phase 1: Introduce `runtime`

Purpose:

- create the first-class non-Spring public product surface

Main tasks:

- add `originalkeen-license-runtime` to the parent build
- add runtime artifact management to the BOM
- create the runtime package structure
- implement the public types:
  - `LicenseRuntime`
  - `LicenseRuntimeBuilder`
  - `LicenseRuntimeConfig`
  - `LicenseVerificationResult`
  - `LicenseFailureCode`
  - `LicenseMismatchType`
  - runtime exception types
  - `LicenseRuntimeCustomizer`
- implement runtime internals:
  - `DefaultLicenseRuntime`
  - `DefaultLicenseRuntimeBuilder`
  - `DefaultLicenseRuntimeConfig`
  - `ResolvedRuntimeOptions`
  - `LicenseRuntimeBootstrap`
  - `LicenseRuntimeAssembly`
  - `LicenseVerificationTranslator`

Deliverables:

- new `runtime` module compiling in the multi-module build
- plain Java usage no longer requires callers to assemble `LicenseParam` directly
- runtime bootstrap owns assembly of `FileKeyStoreParam`, `DefaultLicenseParam`, `LicenseManagerAdapter`, and `LicenseVerifyService`

Exit criteria:

- a plain Java prototype can build a `LicenseRuntime`
- `install`, `installIfPresent`, `verify`, `verifyOrThrow`, and `currentHardwareInfo` are wired through the runtime facade

Current implementation record:

- completed on 2026-04-29 by adding the `originalkeen-license-runtime` module to the parent build and BOM
- completed on 2026-04-29 by implementing `LicenseRuntime`, `LicenseRuntimeBuilder`, `LicenseRuntimeConfig`, `LicenseVerificationResult`, `LicenseFailureCode`, `LicenseMismatchType`, and the runtime exception hierarchy
- completed on 2026-04-29 by implementing `DefaultLicenseRuntime`, `DefaultLicenseRuntimeBuilder`, `DefaultLicenseRuntimeConfig`, `ResolvedRuntimeOptions`, `LicenseRuntimeBootstrap`, `LicenseRuntimeAssembly`, and `LicenseVerificationTranslator`
- completed on 2026-04-29 by adding runtime tests for builder normalization, failure translation, and unsupported-OS fail-fast behavior

## Phase 2: Switch Spring to Consume `runtime`

Purpose:

- make Spring an adapter layer instead of the most complete product surface

Main tasks:

- make `originalkeen-license-spring-boot-autoconfigure` depend on `runtime`
- map `LicenseProperties` into `LicenseRuntimeBuilder`
- keep `HardwareDataProvider` overridable by Spring beans
- apply ordered `LicenseRuntimeCustomizer` beans
- create a runtime-owned assembly object
- expose `LicenseRuntime` as the primary Spring bean
- keep `LicenseVerifyService` available as a compatibility bean in the first V2 release
- switch startup installation to `runtime.installIfPresent()`
- keep servlet filter logic in Spring

Deliverables:

- runtime-backed Spring auto-configuration
- compatibility path for existing `LicenseVerifyService` injection
- thinner Spring assembly code

Exit criteria:

- starter users can upgrade with minimal or no configuration change
- Spring no longer hand-assembles the main `core` runtime path directly

Current implementation record:

- completed on 2026-04-29 by switching `originalkeen-license-spring-boot-autoconfigure` to depend on `originalkeen-license-runtime`
- completed on 2026-04-29 by mapping `LicenseProperties` into `LicenseRuntimeBuilder`, applying optional `HardwareDataProvider`, and applying ordered `LicenseRuntimeCustomizer` beans
- completed on 2026-04-29 by exposing `LicenseRuntime` as the primary bean and keeping `LicenseVerifyService` as a compatibility bean from `LicenseRuntimeAssembly`
- completed on 2026-04-29 by moving startup installation to `runtime.installIfPresent()` and servlet enforcement to runtime-backed `LicenseFilter`
- completed on 2026-04-29 by adding a Spring regression guard so `originalkeen.license.enabled=false` does not build runtime beans
- completed on 2026-04-29 by documenting the starter-facing bean model and upgrade path around `LicenseRuntime`

## Phase 3: Clean Module Boundaries and Positioning

Purpose:

- finish the architectural separation implied by V2

Main tasks:

- move any remaining Web-only defaults out of `core`
- confirm runtime config contains no Spring-only fields
- review which `core` classes remain documented as advanced or expert APIs
- keep `core` public, but demote it in default guidance

Deliverables:

- cleaner boundary between engine, runtime facade, and Spring adapter
- documentation that consistently presents `runtime` as the default non-Spring path

Exit criteria:

- no Web policy leaks into `runtime`
- module responsibilities match `architecture.md`

Current implementation record:

- completed on 2026-04-29 by moving default servlet exclude paths into Spring-local `LicenseWebConstants`
- completed on 2026-04-29 by keeping `LicenseRuntimeConfig` free of `webEnabled` and `excludePaths`
- completed on 2026-04-29 by aligning root and module READMEs so `runtime` is the default non-Spring path and `core` is documented as the expert layer

## Phase 4: Acceptance and Release Preparation

Purpose:

- verify that the implementation matches the design goals before calling V2 ready

Main tasks:

- check the implementation against [acceptance-checklist.md](acceptance-checklist.md)
- validate the suggested verification scenarios from that checklist
- finalize root and module docs for the new public story
- document compatibility guidance and upgrade notes

Deliverables:

- release-ready documentation set
- implementation validated against acceptance criteria

Exit criteria:

- the release candidate satisfies the acceptance checklist
- repository docs clearly show:
  - current stable Spring path
  - V2 runtime path
  - compatibility posture for existing users

Current implementation record:

- completed on 2026-04-29 by updating and extending tests in `core`, `runtime`, and Spring auto-configuration
- completed on 2026-04-29 by running full reactor validation with `mvn test`
- completed on 2026-04-29 by refreshing root and module READMEs and by adding `spring-migration-guide.md`
- completed on 2026-04-29 by reviewing the current repository state against `acceptance-checklist.md`
- completed on 2026-04-30 by adding runtime end-to-end coverage for a real generated license artifact using runtime-managed reload plus success-cache verification
- completed on 2026-04-30 by adding dedicated `SIGNATURE_INVALID` translator coverage, including nested root-cause classification
- completed on 2026-04-30 by adding dedicated installation-failure mapping coverage for `installIfPresent()`
- completed on 2026-04-30 by rerunning full reactor validation with JDK 17, workspace-local Maven settings, and the Surefire Windows compatibility flags required by this environment
- note: Windows `Preferences` emitted registry-access warnings in the sandbox, but the reactor test run still completed successfully

## 5. Workstreams

These workstreams can overlap, but they should still respect the main phase order.

### Workstream A: API and Type System

- finalize the public runtime surface
- keep config intentionally small
- keep failure codes stable and high-level

### Workstream B: Bootstrap and Internal Assembly

- centralize low-level assembly inside `runtime`
- avoid duplicated verification logic between `core`, `runtime`, and Spring

### Workstream C: Spring Transition

- preserve existing property names
- preserve compatibility bean exposure where needed
- keep startup and servlet enforcement behavior stable

### Workstream D: Documentation and Migration

- keep docs explicit about what is implemented now versus what is planned for V2
- keep `core` available for advanced users while changing the default recommendation

## 6. Key Dependencies

| Dependency | Why It Matters |
| --- | --- |
| final runtime API agreement | avoids churn across runtime and Spring code |
| chosen detailed verification path | enables `LicenseVerificationResult` without duplicate logic |
| runtime bootstrap ownership | lets Spring stop wiring `core` directly |
| compatibility bean strategy | protects existing Spring users during migration |

## 7. Risks and Guardrails

Main risks:

- `runtime` duplicates verification, reload, or cache behavior already owned by `core`
- Spring refactor breaks current starter users
- public API surface grows too large too early
- docs drift away from implementation order

Guardrails:

- do not remove existing `core` classes in the first V2 release
- do not break `originalkeen.license.*` property names
- do not remove `LicenseVerifyService` compatibility exposure too early
- do not move Web policy into `runtime`
- do not redesign hardware binding rules in the same iteration

## 8. Definition of Done

V2 should be considered ready for its first release when all of the following are true:

- `originalkeen-license-runtime` exists and is part of the normal build
- plain Java users can integrate through `LicenseRuntime`
- Spring Boot starter internally uses `runtime`
- `LicenseRuntime` is the primary documented Spring bean
- `LicenseVerifyService` remains available for compatibility in the first V2 release
- Web-only concerns remain in the Spring layer
- docs and examples match the actual public shape
- the release candidate passes the expectations in [acceptance-checklist.md](acceptance-checklist.md)

## 9. Immediate Next Step

After the implementation completed on 2026-04-30, the next concrete step should be:

1. decide whether this V2 line should ship as the next public release candidate
2. prepare release-facing notes, sample updates, and version promotion work
3. continue any post-V2 sample expansion, such as CLI or Docker demos, outside the core V2 acceptance baseline

This keeps the plan aligned with the current V2 document set while reflecting the code that has already landed.
