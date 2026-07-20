# Dependency Governance

## Goals

OriginalKeen License is a library ecosystem, not an application platform. Its published artifacts must keep the consumer in control of framework and logging choices while keeping all OriginalKeen modules mutually compatible.

## Ownership Rules

- `originalkeen-license-dependencies` manages only `org.eu.originalkeen` artifacts.
- The root reactor may import Spring Boot dependency management for repeatable builds, but the public BOM must not export it.
- Consuming applications select their own Spring Boot patch version within the documented compatibility line.
- Library modules depend on logging APIs only. They must not publish Logback, Log4j Core, or another logging provider as a transitive dependency.
- A module declares every dependency whose types it uses directly. It does not rely on accidental transitive dependencies.
- Optional Servlet support stays optional in the auto-configuration module and activates only when the required Web classes and a `LicenseRuntime` bean are present.

## Supported Baseline

| Concern | Current baseline |
| --- | --- |
| Java | 17 or later |
| Spring Boot integration | 3.5.x |
| Logging facade | SLF4J API |
| Built-in hardware providers | Windows and Linux |

Spring Boot 4 compatibility must be tested and documented separately before it is claimed.

## Published Dependency Shape

### Plain Java

Applications normally depend on `originalkeen-license-runtime`. It brings the OriginalKeen core and model modules, TrueLicense, and the SLF4J API. The application chooses an SLF4J provider if logging output is required.

### Spring Boot

Applications normally depend on `originalkeen-license-spring-boot-starter`. The starter brings Spring Boot's core starter and the OriginalKeen auto-configuration module. Servlet filtering activates only in a Servlet Web application; adding the license starter alone does not turn a non-Web application into a Web application.

## Versioning Policy

- Patch releases may fix documentation, tests, metadata generation, and implementation defects without intentionally changing the consumer dependency graph.
- Minor releases may add modules or integrations. Any transitive dependency change must be called out in release notes.
- Removing a dependency that consumers may have used transitively is treated as a migration concern even when no OriginalKeen Java signature changes.
- Released coordinates are immutable. Development continues under the next `-SNAPSHOT` version.

## Release Verification

Before publishing, verify:

1. the full reactor test suite passes;
2. flattened published POMs contain explicit dependency versions;
3. the public BOM contains only `org.eu.originalkeen` managed dependencies;
4. no logging implementation is present in the runtime dependency tree;
5. the starter contains `spring-boot-starter` and does not force a Web stack;
6. auto-configuration backs off when licensing or Web enforcement is disabled.
