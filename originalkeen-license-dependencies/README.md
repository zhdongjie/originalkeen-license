# OriginalKeen License Dependencies (BOM)

`originalkeen-license-dependencies` provides the Bill of Materials for the OriginalKeen License ecosystem. Importing it keeps public OriginalKeen modules aligned without changing dependency versions owned by the consuming application.

## What It Manages

- OriginalKeen module versions across `model`, `core`, `runtime`, `autoconfigure`, and `starter`
- It intentionally does not import `spring-boot-dependencies` or manage logging implementations
- Spring Boot applications retain control of their Spring, Servlet, and logging dependency lines

## Installation

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eu.originalkeen</groupId>
            <artifactId>originalkeen-license-dependencies</artifactId>
            <version>1.3.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Gradle

```groovy
dependencies {
    implementation platform('org.eu.originalkeen:originalkeen-license-dependencies:1.3.0')
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
- Spring Boot version selected by the consuming application
- Spring Boot 3.5.x is the current tested integration baseline
- Runtime hardware providers built in for Windows and Linux

## License

This project is licensed under the Apache License 2.0.
