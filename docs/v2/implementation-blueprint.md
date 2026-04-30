# V2 Implementation Blueprint

Status: implementation planning draft.

This document turns the V2 design into a concrete implementation blueprint. It is intentionally closer to engineering execution than the higher-level architecture documents.

## 1. Purpose

V2 already has a clear product direction:

- plain Java should get a first-class public runtime
- Spring Boot should become an adapter layer on top of that runtime
- `core` should remain the verification engine, not disappear overnight

What remains is implementation choreography:

- which modules need to change
- which classes should be added first
- how to keep compatibility while moving responsibilities

## 2. Target Change Map by Module

| Module | Change Type | Main Work |
| --- | --- | --- |
| `originalkeen-license-model` | little or none | keep protocol stable |
| `originalkeen-license-core` | additive, minimal | keep engine, possibly add richer internal verification outcome support |
| `originalkeen-license-runtime` | new module | add public framework-free runtime and bootstrap layer |
| `originalkeen-license-spring-boot-autoconfigure` | refactor | stop hand-assembling `core`, consume runtime assembly instead |
| `originalkeen-license-spring-boot-starter` | small dependency change | pull in runtime-backed auto-configuration |
| `originalkeen-license-dependencies` | additive | manage the new runtime artifact |

## 3. Runtime Module Package Layout

Suggested base package:

```text
org.eu.originalkeen.license.runtime
```

Suggested internal structure:

```text
org.eu.originalkeen.license.runtime
  LicenseRuntime
  LicenseRuntimeBuilder
  LicenseRuntimeConfig
  LicenseVerificationResult
  LicenseFailureCode
  LicenseMismatchType

org.eu.originalkeen.license.runtime.exception
  LicenseRuntimeException
  LicenseConfigurationException
  LicenseInstallationException
  LicenseVerificationException

org.eu.originalkeen.license.runtime.internal
  DefaultLicenseRuntime
  DefaultLicenseRuntimeBuilder
  DefaultLicenseRuntimeConfig
  ResolvedRuntimeOptions

org.eu.originalkeen.license.runtime.internal.bootstrap
  LicenseRuntimeBootstrap
  LicenseRuntimeAssembly
  LicenseVerificationTranslator

org.eu.originalkeen.license.runtime.spi
  LicenseRuntimeCustomizer
```

Recommended package posture:

- top-level package contains the public API only
- `exception` and `spi` are public but intentionally small
- `internal` and `internal.bootstrap` hold implementation details

## 4. Public Types to Implement First

Recommended first-wave public types:

- `LicenseRuntime`
- `LicenseRuntimeBuilder`
- `LicenseRuntimeConfig`
- `LicenseVerificationResult`
- `LicenseFailureCode`
- `LicenseMismatchType`
- `LicenseRuntimeException`
- `LicenseConfigurationException`
- `LicenseInstallationException`
- `LicenseVerificationException`
- `LicenseRuntimeCustomizer`

Why this order:

- it locks the public contract before Spring refactoring starts
- it lets the team discuss API stability with a smaller surface first

## 5. Runtime Internals to Implement First

Recommended first-wave internal types:

- `DefaultLicenseRuntime`
- `DefaultLicenseRuntimeBuilder`
- `DefaultLicenseRuntimeConfig`
- `ResolvedRuntimeOptions`
- `LicenseRuntimeBootstrap`
- `LicenseRuntimeAssembly`
- `LicenseVerificationTranslator`

Suggested responsibilities:

| Type | Responsibility |
| --- | --- |
| `ResolvedRuntimeOptions` | normalized and validated builder state |
| `LicenseRuntimeBootstrap` | builds `FileKeyStoreParam`, `DefaultLicenseParam`, `LicenseManagerAdapter`, and `LicenseVerifyService` |
| `LicenseRuntimeAssembly` | returns aligned runtime plus compatibility objects |
| `LicenseVerificationTranslator` | maps internal errors and outcomes to V2 result and exception types |
| `DefaultLicenseRuntime` | delegates install and verify behavior to the assembled core stack |

## 6. Core Changes to Keep Small

The whole V2 strategy depends on not rewriting `core`.

Recommended `core` posture:

- keep `LicenseManagerAdapter` where it is
- keep `LicenseVerifyService` where it is
- do not move providers or keystore helpers
- only add the minimum richer verification metadata needed by `runtime`

Preferred additive change:

- add a richer internal verification outcome path near `LicenseVerifyService`

Not recommended:

- moving `LicenseVerifyService` into `runtime` in the same iteration
- duplicating cache and reload logic in another layer
- redesigning hardware verification rules at the same time

## 7. Spring Refactoring Shape

The Spring module should gradually stop exposing its own assembly logic.

Recommended end state:

- `LicenseAutoConfiguration` builds a runtime builder from `LicenseProperties`
- the builder receives any user-supplied `HardwareDataProvider`
- ordered `LicenseRuntimeCustomizer` beans refine the builder
- Spring asks runtime bootstrap to create a `LicenseRuntimeAssembly`
- Spring exposes `LicenseRuntime` as the main bean
- Spring optionally exposes `LicenseVerifyService` from that assembly as a compatibility bean
- `LicenseStartupAutoConfiguration` switches to `runtime.installIfPresent()`

## 8. Suggested Code Touch Order

Recommended sequence:

1. add the new runtime module to the parent build and BOM
2. add public runtime API and exception types
3. add builder normalization and config snapshot support
4. add runtime bootstrap and assembly internals
5. add minimal richer verification outcome support in `core`
6. connect `DefaultLicenseRuntime.verify()` and `verifyOrThrow()` to the translator
7. switch Spring auto-configuration to runtime assembly
8. keep compatibility bean exposure where needed
9. update examples and docs to present the new public path

Why this sequence works:

- it locks the public contract before the Spring refactor
- it keeps compatibility visible at every step
- it avoids a period where both Spring and runtime own conflicting assembly logic

## 9. Minimal File-Level Work Map

Suggested first implementation slice:

| Module | Likely Work |
| --- | --- |
| parent `pom.xml` | register `originalkeen-license-runtime` |
| BOM module | add managed runtime artifact |
| new runtime module `pom.xml` | depend on `core` and `model` |
| runtime source tree | add public API, config, result, exceptions, bootstrap |
| `LicenseVerifyService` | add richer internal verification outcome support if needed |
| Spring auto-configuration | replace direct `LicenseParam` and `LicenseManagerAdapter` bean creation with runtime assembly usage |
| Spring startup runner | switch to `runtime.installIfPresent()` |

## 10. Compatibility Guardrails During Implementation

These guardrails should stay in place while coding:

- do not remove existing `core` classes
- do not break existing property names
- do not remove `LicenseVerifyService` bean exposure in the first V2 release
- do not change the current tolerant startup behavior around missing `licensePath`
- do not move Web-specific policy into runtime

## 11. Naming Decisions Worth Locking Early

These names are worth deciding before code lands:

- module name: `originalkeen-license-runtime`
- main public type: `LicenseRuntime`
- builder type: `LicenseRuntimeBuilder`
- config type: `LicenseRuntimeConfig`
- compatibility hook: `LicenseRuntimeCustomizer`
- internal bootstrap bundle: `LicenseRuntimeAssembly`

Locking them early reduces later rename churn across docs, Spring wiring, and examples.

## 12. First Release Exit Criteria

The first implementation pass should be considered complete when:

- the new runtime module exists and is published through the normal multi-module build
- plain Java callers can build and use `LicenseRuntime` without touching `LicenseParam`
- Spring Boot auto-configuration consumes runtime assembly rather than directly wiring the primary `core` path
- existing Spring users can still inject `LicenseVerifyService`
- the V2 docs no longer describe any major design gap without a concrete implementation path
