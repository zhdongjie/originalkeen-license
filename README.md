# OriginalKeen License

**OriginalKeen License** is a Java license management and verification system for enterprise applications. It provides hardware-bound licensing, automated installation, and Spring Boot integration through a Maven multi-module architecture.

## Key Features

- Hardware fingerprinting for CPU, motherboard, IP, and MAC address binding
- License expiration monitoring with advanced warning support
- Web request interception with configurable path exclusions
- Runtime verification support for Windows and Linux
- Client information collection scripts for Windows, Linux, and macOS
- Internal verification cache for improved runtime performance

## Module Overview

1. `originalkeen-license-dependencies`: Internal BOM used to align dependency versions.
2. `originalkeen-license-model`: Shared protocol models and constants.
3. `originalkeen-license-core`: Core licensing engine and hardware detection logic.
4. `originalkeen-license-spring-boot-autoconfigure`: Spring Boot auto-configuration module.
5. `originalkeen-license-spring-boot-starter`: End-user starter dependency.

## Quick Start

### 1. Import the BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eu.originalkeen</groupId>
            <artifactId>originalkeen-license-dependencies</artifactId>
            <version>1.1.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. Add the Starter Dependency

```xml
<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 3. Configure License Properties

```yaml
originalkeen:
  license:
    enabled: true
    web-enabled: true
    subject: "YourApplicationSubject"
    license-path: "/path/to/license.lic"
    public-alias: "public"
    public-key-store-path: "classpath:publicKey.keystore"
    public-password: "your_password"
    exclude-paths:
      - /login
      - /actuator/**
```

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
- Child modules currently keep explicit `groupId` and `version` so Central can parse coordinates reliably for this project.
- `flatten-maven-plugin` is intentionally enabled for publishing compatibility; do not remove it casually.
- `.flattened-pom.xml` files are build artifacts and should not be committed.

## Client Information Collection

This project provides ready-to-use client information collection scripts for license issuance.

- Full script guide: [scripts/client-info/README.md](scripts/client-info/README.md)
- Linux script: [scripts/client-info/collect-client-info-linux.sh](scripts/client-info/collect-client-info-linux.sh)
- macOS script: [scripts/client-info/collect-client-info-macos.sh](scripts/client-info/collect-client-info-macos.sh)
- Windows script: [scripts/client-info/collect-client-info-windows.ps1](scripts/client-info/collect-client-info-windows.ps1)

These scripts generate JSON that matches the current `LicenseCheckModel` fields directly:

- `protocolVersion`
- `ipAddress`
- `macAddress`
- `cpuSerial`
- `mainBoardSerial`

Quick examples:

```bash
bash scripts/client-info/collect-client-info-linux.sh
bash scripts/client-info/collect-client-info-macos.sh
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\client-info\collect-client-info-windows.ps1
```

## Important Notes

- Linux hardware information collection may require elevated privileges depending on the deployment environment.
- Maven Central releases require sources, javadocs, and GPG signatures.
- If IntelliJ shows `central-publishing-maven-plugin` in red but WSL Maven can build and deploy, the usual cause is IDE-side Maven resolution rather than an invalid POM.

## License

This project is licensed under the Apache License 2.0.