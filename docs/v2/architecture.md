# V2 Architecture and Module Boundaries

## Problem Statement

The current project already separates protocol objects, core verification logic, and Spring Boot integration at the code dependency level. The remaining gap is product shape:

- Plain Java users must assemble low-level classes such as `LicenseParam`, `FileKeyStoreParam`, and platform-specific providers manually.
- Spring Boot auto-configuration currently acts as the most complete and ergonomic entry point.
- Some Web-specific defaults, such as servlet exclude paths, are conceptually outside the core verification engine.

V2 resolves this by introducing a dedicated runtime layer above `core`.

## Design Principles

- Core verification logic must remain framework-agnostic.
- Public runtime APIs must be easier to use than the low-level engine classes.
- Spring Boot support must consume the runtime, not define the runtime.
- Web concerns must stay in the Spring layer.
- Migration should preserve compatibility for existing users as much as possible.

## Target Module Layout

| Module | Role in V2 | Depends On |
| --- | --- | --- |
| `originalkeen-license-dependencies` | BOM for all public artifacts and aligned third-party versions | none |
| `originalkeen-license-model` | Shared protocol objects and compatibility contract | none |
| `originalkeen-license-core` | Low-level verification engine, TrueLicense adapter, hardware providers, keystore helpers, advanced service layer | `model` |
| `originalkeen-license-runtime` | Public framework-free runtime facade, configuration model, result types, exceptions, default provider resolution | `core`, `model` |
| `originalkeen-license-spring-boot-autoconfigure` | Spring properties, bean wiring, startup install, servlet filter registration | `runtime`, Spring Boot |
| `originalkeen-license-spring-boot-starter` | Convenience dependency for Spring Boot users | `autoconfigure` |

## Dependency Rules

- `model` must not depend on any project module.
- `core` must not depend on Spring or servlet APIs.
- `runtime` may depend on `core`, but `core` must not depend on `runtime`.
- `autoconfigure` must depend on `runtime`, not on `core` directly for its primary workflow.
- `starter` should remain a thin dependency aggregator.

## Public Surface by Layer

### `model`

Public responsibility:

- shared protocol objects such as `LicenseCheckModel`
- compatibility and serialization contract

### `core`

Public responsibility:

- expert-level primitives and extension points
- hardware providers
- TrueLicense integration
- low-level verification workflow

Examples of classes that remain here:

- `HardwareDataProvider`
- `AbstractHardwareProvider`
- `WindowsHardwareProvider`
- `LinuxHardwareProvider`
- `FileKeyStoreParam`
- `LicenseManagerAdapter`

Likely treatment for `LicenseVerifyService`:

- keep it in `core` initially to minimize churn
- treat it as an advanced or expert API
- let `runtime` wrap it instead of exposing it as the preferred first-touch API

### `runtime`

Public responsibility:

- the main non-Spring API that application teams use directly
- runtime configuration builder or options object
- simplified install and verify flow
- result and exception semantics owned by OriginalKeen License rather than leaked from low-level libraries
- default OS provider selection and runtime bootstrap logic

### `spring-boot-autoconfigure`

Public responsibility:

- mapping Spring configuration properties into runtime configuration
- creating the runtime bean
- running startup installation
- registering servlet filter enforcement
- holding Web-only defaults such as servlet exclude paths

## Ownership Mapping from V1 to V2

| Current Class or Concern | V2 Ownership |
| --- | --- |
| `LicenseCheckModel` | stays in `model` |
| `LicenseHeader` | stays in `model` |
| `HardwareDataProvider` and OS providers | stay in `core` |
| `LicenseManagerAdapter` | stays in `core` |
| `LicenseVerifyService` | remains in `core` initially, used by `runtime` internally |
| `LicenseProperties` | stays in Spring layer, maps to runtime config |
| `LicenseFilter` | stays in Spring layer |
| startup installation | driven by Spring in Spring apps, callable through `runtime` in plain Java apps |
| servlet exclude path defaults | should move to Spring layer if they are still stored in `core` |

## Why the New Module Is Named `runtime`

`runtime` is preferred over `client` because:

- the repository already has client-info scripts, so `client` would be overloaded
- the new module is the runtime that lives inside the protected application
- the name maps well to both plain Java usage and framework adapters

## Out of Scope

- Issuer-side tool redesign
- Reactive or non-servlet framework adapters
- Replacing hardware matching rules or license storage format

