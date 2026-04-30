# V2 Migration Plan

Status: phased rollout plan with active implementation already landed in the repository.

This document describes the rollout path from the earlier Spring-centered public shape to the V2 runtime-centered shape.

## Current Snapshot

- Phase 0 is complete: the detailed verification boundary was finalized around `LicenseVerifyService.verifyDetailed()`.
- Phase 1 is complete: `originalkeen-license-runtime` now exists.
- Phase 2 is largely complete: Spring Boot now consumes `runtime` and exposes `LicenseRuntime`.
- Phase 3 has started: Web-only defaults were moved into the Spring layer.
- Phase 4 is in progress: documentation, migration notes, and acceptance review are being tightened around the implemented code.

See [PLAN.md](PLAN.md) for the live execution record.

## Strategy

Implement V2 as a layered evolution, not a rewrite. The main risk to avoid is breaking existing Spring Boot users while moving the public shape toward a cleaner runtime-first architecture.

## Phase 0: Prepare the Verification Boundary

Scope:

- confirm the final public runtime API from the design set
- decide whether richer verification metadata is exposed through `LicenseVerifyService` or an adjacent helper
- keep the `core` change additive and minimal

Expected impact:

- the runtime module can be implemented without duplicating cache and reload behavior
- the later Spring switch has a stable internal verification path to consume

Repository result:

- implemented through `LicenseVerifyService.verifyDetailed()` and `CoreVerificationOutcome`

## Phase 1: Introduce `runtime`

Scope:

- add `originalkeen-license-runtime`
- add runtime config, builder, result, and exception types
- add runtime bootstrap and assembly internals
- implement `LicenseRuntime` by wrapping existing `core` classes
- update the BOM to manage the new artifact

Expected impact:

- plain Java users get a supported public entry point
- no mandatory change required for existing Spring Boot users yet

Repository result:

- implemented in the current repository state

## Phase 2: Switch Spring Boot to Consume `runtime`

Scope:

- make `originalkeen-license-spring-boot-autoconfigure` depend on `runtime`
- create the runtime builder from Spring properties
- apply `HardwareDataProvider` and `LicenseRuntimeCustomizer` integration hooks
- create a runtime-owned assembly object and expose `LicenseRuntime`
- keep `LicenseVerifyService` as a compatibility bean if needed
- keep startup installation and servlet filtering in the Spring module

Expected impact:

- Spring Boot becomes an adapter layer instead of the place where runtime assembly lives
- non-Web and Web Spring applications both share the same runtime core path

Repository result:

- implemented in the current repository state, with `LicenseVerifyService` compatibility kept in place

## Phase 3: Clean Module Boundaries

Scope:

- move Web-specific defaults such as servlet exclude path constants out of `core` if still present there
- review which `core` classes are documented as expert APIs versus internal implementation details
- de-emphasize direct `core` usage in documentation in favor of `runtime`

Expected impact:

- cleaner separation of framework concerns
- easier future support for non-Spring adapters

Repository result:

- Web-only default exclude paths now live in the Spring layer
- documentation still needs continued cleanup to keep `runtime` and `starter` as the main stories

## Phase 4: Documentation, Examples, and Acceptance Review

Scope:

- update root and module READMEs to present two primary paths:
  - plain Java via `runtime`
  - Spring Boot via `starter`
- add migration notes for existing Spring users
- add usage samples for unsupported operating systems with custom providers
- check the implementation against `acceptance-checklist.md`

Expected impact:

- the public product shape becomes obvious from the repository docs
- the implementation can be judged against a shared completion standard

Repository result:

- in progress

## Compatibility Guidance

| Audience | V2 Compatibility Expectation |
| --- | --- |
| current Spring Boot starter users | should keep working with minimal or no configuration changes |
| existing code injecting `LicenseVerifyService` | should keep working in the first V2 release line |
| advanced users wiring `core` manually | should still be supported, but documented as expert usage |
| plain Java users | gain a simpler supported integration path through `runtime` |

## Suggested Deprecation Posture

- Do not remove `core` APIs in the same release that introduces `runtime`.
- Prefer documentation-based demotion first, then deprecation annotations only after the runtime is stable.
- Keep package moves to a minimum in the first iteration.

## Risks and Mitigations

### Risk: duplicated behavior between `runtime` and Spring

Mitigation:

- Spring should delegate to `runtime` for install and verify flow
- Spring should own only property binding, lifecycle hooks, and servlet enforcement

### Risk: leaking low-level exceptions through the new API

Mitigation:

- centralize translation from core failures into runtime result codes and runtime exceptions

### Risk: unsupported operating systems create confusing defaults

Mitigation:

- fail fast during runtime creation when no built-in provider exists
- document custom provider registration as the explicit escape hatch

## Completion Criteria

V2 should be considered structurally complete when:

- a plain Java application can use the project through `runtime` without touching `core` bootstrapping details
- the Spring Boot starter internally consumes the runtime module
- Web-only defaults are owned by the Spring layer
- repository documentation presents `runtime` and `starter` as the two official entry points
- the implementation path is consistent with `implementation-blueprint.md`
- the release candidate satisfies `acceptance-checklist.md`
