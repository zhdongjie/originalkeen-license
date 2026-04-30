# OriginalKeen License Dependencies (BOM)

`originalkeen-license-dependencies` provides the Bill of Materials for the OriginalKeen License ecosystem. Importing this BOM keeps internal module versions aligned and centralizes the third-party versions used by the project.

## What It Manages

- OriginalKeen module versions across `model`, `core`, `runtime`, `autoconfigure`, and `starter`
- Spring Boot dependency alignment through `spring-boot-dependencies`
- Shared third-party versions such as TrueLicense and Log4j2

## Installation

### Maven

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

### Gradle

```groovy
dependencies {
    implementation platform('org.eu.originalkeen:originalkeen-license-dependencies:1.1.5')
    implementation 'org.eu.originalkeen:originalkeen-license-spring-boot-starter'
}
```

## Usage

Once the BOM is imported, you can declare OriginalKeen modules without repeating their versions:

```xml
<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-runtime</artifactId>
    </dependency>

    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

## Managed Modules

| Artifact ID | Description |
| --- | --- |
| `originalkeen-license-model` | Shared protocol models and compatibility contract. |
| `originalkeen-license-core` | Verification engine, hardware providers, and keystore helpers. |
| `originalkeen-license-runtime` | Preferred plain Java integration path through `LicenseRuntime`. |
| `originalkeen-license-spring-boot-autoconfigure` | Runtime-backed Spring Boot bean registration, startup installer, and servlet filter registration. |
| `originalkeen-license-spring-boot-starter` | End-user Spring Boot entry point. |

## Recommended Pairings

- Plain Java applications: BOM + `originalkeen-license-runtime`
- Spring Boot applications: BOM + `originalkeen-license-spring-boot-starter`
- Expert integrations: BOM + `originalkeen-license-core`

## Compatibility

- Java 17 or higher
- Spring Boot dependency line managed by the BOM
- Runtime hardware providers built in for Windows and Linux

## License

This project is licensed under the Apache License 2.0.
