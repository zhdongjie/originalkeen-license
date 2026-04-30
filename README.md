# OriginalKeen License

OriginalKeen License is a Java license installation and verification toolkit for enterprise applications. It combines hardware-bound verification, a framework-free runtime facade, Spring Boot integration, client information collection scripts, and Maven Central release tooling in a Maven multi-module project.

## Repository Status

This repository now reflects the V2 runtime-first architecture:

- plain Java applications should start with `originalkeen-license-runtime`
- Spring Boot applications should start with `originalkeen-license-spring-boot-starter`
- `originalkeen-license-core` remains public as the expert layer
- existing Spring integrations keep the same `originalkeen.license.*` properties
- existing Spring code that injects `LicenseVerifyService` continues to work in the first V2 release line

Design and rollout documents are kept in [docs/v2/README.md](docs/v2/README.md).

## Key Features

- Hardware binding for CPU, motherboard, IP, and MAC addresses
- Framework-free `LicenseRuntime` facade for install, verify, and hardware inspection
- Spring Boot startup installation and servlet filter enforcement backed by the shared runtime path
- Verification success cache and configured license file hot reload support
- Windows and Linux built-in hardware providers, plus extension points for custom providers
- Client information collection scripts for Windows, Linux, and macOS registration workflows
- BOM, runtime, starter, and release tooling modules in one aligned reactor

## Module Overview

1. `originalkeen-license-dependencies`: BOM for internal modules and aligned third-party versions.
2. `originalkeen-license-model`: Shared protocol models and compatibility contract.
3. `originalkeen-license-core`: Verification engine, hardware providers, keystore helpers, and expert-level assembly APIs.
4. `originalkeen-license-runtime`: Preferred plain Java integration path through `LicenseRuntime`.
5. `originalkeen-license-spring-boot-autoconfigure`: Runtime-backed Spring Boot bean registration, startup installation, and servlet filter wiring.
6. `originalkeen-license-spring-boot-starter`: Recommended Spring Boot entry point for application teams.

## Quick Start

### 1. Import the BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eu.originalkeen</groupId>
            <artifactId>originalkeen-license-dependencies</artifactId>
            <version>1.1.5</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. Plain Java via `runtime`

```xml
<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-runtime</artifactId>
    </dependency>
</dependencies>
```

```java
LicenseRuntime runtime = LicenseRuntime.builder()
        .subject("MyAppLicense")
        .licensePath("/opt/licenses/myapp.lic")
        .publicAlias("public")
        .publicKeyStorePath("classpath:publicKey.keystore")
        .publicPassword("changeit1")
        .build();

runtime.installIfPresent();
runtime.verifyOrThrow();
```

Use `runtime.verify()` when you want a structured result with failure codes such as `NOT_INSTALLED`, `EXPIRED`, or `HARDWARE_MISMATCH`.

### 3. Spring Boot via `starter`

```xml
<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

```yaml
originalkeen:
  license:
    enabled: true
    web-enabled: true
    subject: "MyAppLicense"
    license-path: "/opt/licenses/myapp.lic"
    public-alias: "public"
    public-key-store-path: "classpath:publicKey.keystore"
    public-password: "changeit1"
    exclude-paths:
      - /login
      - /api/public/**
```

Runtime behavior:

- startup calls `runtime.installIfPresent()`
- a blank or unreadable `license-path` skips optional install instead of failing startup
- servlet requests that are not excluded are verified through `LicenseRuntime`
- built-in servlet exclusions always include `/actuator/**`, `/error`, `/favicon.ico`, and common static asset patterns

If you need explicit checks in application code, inject `LicenseRuntime`. If you already inject `LicenseVerifyService`, that compatibility bean is still available in the first V2 release line.

## Recommended Entry Points

- Plain Java: [originalkeen-license-runtime/README.md](originalkeen-license-runtime/README.md)
- Spring Boot: [originalkeen-license-spring-boot-starter/README.md](originalkeen-license-spring-boot-starter/README.md)
- Expert internals: [originalkeen-license-core/README.md](originalkeen-license-core/README.md)
- Spring migration notes: [docs/v2/spring-migration-guide.md](docs/v2/spring-migration-guide.md)

## Module Docs

- Root overview: [README.md](README.md)
- BOM: [originalkeen-license-dependencies/README.md](originalkeen-license-dependencies/README.md)
- Model: [originalkeen-license-model/README.md](originalkeen-license-model/README.md)
- Core: [originalkeen-license-core/README.md](originalkeen-license-core/README.md)
- Runtime: [originalkeen-license-runtime/README.md](originalkeen-license-runtime/README.md)
- Spring Boot auto-configuration: [originalkeen-license-spring-boot-autoconfigure/README.md](originalkeen-license-spring-boot-autoconfigure/README.md)
- Spring Boot starter: [originalkeen-license-spring-boot-starter/README.md](originalkeen-license-spring-boot-starter/README.md)
- V2 design and rollout set: [docs/v2/README.md](docs/v2/README.md)

## Client Information Collection

This project provides ready-to-use client information collection scripts for license issuance and registration workflows.

- Full script guide: [scripts/client-info/README.md](scripts/client-info/README.md)
- Linux script: [scripts/client-info/collect-client-info-linux.sh](scripts/client-info/collect-client-info-linux.sh)
- macOS script: [scripts/client-info/collect-client-info-macos.sh](scripts/client-info/collect-client-info-macos.sh)
- Windows script: [scripts/client-info/collect-client-info-windows.ps1](scripts/client-info/collect-client-info-windows.ps1)

These scripts generate JSON matching `LicenseCheckModel` directly:

- `protocolVersion`
- `ipAddress`
- `macAddress`
- `cpuSerial`
- `mainBoardSerial`

## Release Guide

This project publishes to Maven Central through the Sonatype Central Portal Maven plugin.

- Full release guide: [docs/release/README.md](docs/release/README.md)
- Base Maven settings example: [docs/release/settings.xml.example](docs/release/settings.xml.example)
- Maven settings with GPG: [docs/release/settings-gpg.xml.example](docs/release/settings-gpg.xml.example)
- Preflight check script: [scripts/check-release.sh](scripts/check-release.sh)
- Release script: [scripts/release.sh](scripts/release.sh)

Project-specific release rules:

- Always release from the root parent project.
- Use `mvn versions:set -DnewVersion=...` to change versions consistently.
- Child modules intentionally keep explicit `groupId` and `version` so Central can parse coordinates reliably for this project.
- `flatten-maven-plugin` is intentionally enabled for publishing compatibility; do not remove it casually.
- `.flattened-pom.xml` files are build artifacts and should not be committed.

## Platform Notes

- Built-in runtime hardware providers support Windows and Linux.
- For macOS or other operating systems, register a custom `HardwareDataProvider`.
- Linux hardware information collection may require elevated privileges depending on the deployment environment.

## License

This project is licensed under the Apache License 2.0.
