# Dependency Governance

The implementation plan, completed work, verification evidence, and encountered issues are tracked in [Spring Boot Dependency Decoupling Plan](dependency-decoupling-plan.md).

## Goals

OriginalKeen License is a library ecosystem, not an application platform. Its published artifacts must keep the consumer in control of framework and logging choices while keeping all OriginalKeen modules mutually compatible.

## Ownership Rules

- `originalkeen-license-dependencies` manages only `org.eu.originalkeen` artifacts.
- The neutral root reactor does not import Spring Boot dependency management.
- Only the auto-configuration and starter modules import the Spring Boot BOM, using the oldest supported version as their compile baseline.
- Consuming applications select their own Spring Boot version within the documented compatibility line.
- Library modules depend on logging APIs only. They must not publish Logback, Log4j Core, or another logging provider as a transitive dependency.
- A module declares every dependency whose types it uses directly. It does not rely on accidental transitive dependencies.
- Optional Servlet support stays optional in the auto-configuration module and activates only when the required Web classes and a `LicenseRuntime` bean are present.

## Supported Baseline

| Concern | Current baseline |
| --- | --- |
| Java | 17 or later |
| Spring Boot integration | 3.2+ within the 3.x line |
| Spring Boot compile baseline | 3.2.0 |
| Verified Spring Boot consumers | 3.2.0 and 3.5.9 |
| Logging facade | SLF4J API |
| Built-in hardware providers | Windows and Linux |

Spring Boot 4 compatibility must be tested and documented separately before it is claimed.

## Published Dependency Shape

### Plain Java

Applications normally depend on `originalkeen-license-runtime`. It brings the OriginalKeen core and model modules, TrueLicense, and the SLF4J API. The application chooses an SLF4J provider if logging output is required.

### Spring Boot

Applications normally depend on `originalkeen-license-spring-boot-starter`. The starter brings Spring Boot's core starter and the OriginalKeen auto-configuration module. Servlet filtering activates only in a Servlet Web application; adding the license starter alone does not turn a non-Web application into a Web application.

The Spring Boot versions recorded in the published integration POMs are compile baselines, not a requirement for applications to use that exact patch version. An application's Spring Boot parent or BOM remains authoritative and may select a newer compatible Boot 3.x version.

## Versioning Policy

- Patch releases may fix documentation, tests, metadata generation, and implementation defects without intentionally changing the consumer dependency graph.
- Minor releases may add modules or integrations. Any transitive dependency change must be called out in release notes.
- Removing a dependency that consumers may have used transitively is treated as a migration concern even when no OriginalKeen Java signature changes.
- Released coordinates are immutable. Development continues under the next `-SNAPSHOT` version.
- OriginalKeen versions follow OriginalKeen API and feature changes; they do not mirror Spring Boot minor or patch versions.

## Release Verification

Before publishing, verify:

1. the full reactor test suite passes;
2. flattened published POMs contain explicit dependency versions;
3. the public BOM contains only `org.eu.originalkeen` managed dependencies;
4. the model, core, and runtime dependency trees contain no Spring dependencies;
5. no logging implementation is present in the runtime dependency tree;
6. the starter contains `spring-boot-starter` and does not force a Web stack;
7. auto-configuration backs off when licensing or Web enforcement is disabled;
8. the compile baseline and at least one newer Boot 3.x consumer both pass integration verification.

Run `scripts/check-release.sh` with `SKIP_TESTS=false` for the full release gate. The normal reactor tests exercise the Boot 3.2 compile baseline, and `scripts/check-spring-boot-compatibility.sh` reruns the auto-configuration tests against the newer versions listed in `BOOT_COMPATIBILITY_VERSIONS` (currently Boot 3.5.9 by default).

## Continuous Integration

`.github/workflows/verify.yml` runs on pushes and pull requests using Java 17. It contains two independent gates:

- a full Reactor `clean verify` followed by the neutral-module dependency boundary check;
- a Spring Boot compatibility matrix covering the 3.2.0 compile baseline and the newer verified 3.5.9 line.

The boundary and compatibility scripts use the fixed WSL Maven and settings paths when those files exist. In standard CI environments they fall back to `mvn` from `PATH` and Maven's default settings. Passing `MAVEN_BIN` or `SETTINGS_FILE` still overrides that discovery explicitly.
