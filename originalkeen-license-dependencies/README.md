# OriginalKeen License Dependencies (BOM)

`originalkeen-license-dependencies` provides a **Bill of Materials (BOM)** for the OriginalKeen License ecosystem. It centralizes dependency management, ensuring that all internal modules and external libraries are version-aligned and fully compatible.

By importing this BOM, users can avoid "Version Hell" and ensure a stable integration of the license management system.

## Features

* **Centralized Versioning**: Manage all `originalkeen-license` module versions from a single point.
* **Spring Boot Alignment**: Synchronizes project dependencies with the recommended Spring Boot stack.
* **Dependency Consistency**: Guarantees that `core`, `model`, and `starter` modules work together flawlessly.
* **Simplified Maintenance**: Upgrade the entire license framework by changing just one version number in your configuration.

## Installation

### Maven

To use this BOM, add it to the `<dependencyManagement>` section of your `pom.xml`. This allows you to omit version numbers when declaring dependencies on specific OriginalKeen modules.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eu.originalkeen</groupId>
            <artifactId>originalkeen-license-dependencies</artifactId>
            <version>1.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

```

### Gradle

In Gradle 5.0+, you can use the `platform` keyword to import the BOM:

```groovy
dependencies {
    implementation platform('org.eu.originalkeen:originalkeen-license-dependencies:1.0.1')
    implementation 'org.eu.originalkeen:originalkeen-license-spring-boot-starter'
}

```

## Usage

Once the BOM is imported, you can add any OriginalKeen License module to your project without specifying a version:

```xml
<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-core</artifactId>
    </dependency>
</dependencies>

```

## Managed Dependencies

This BOM manages the following core components:

| Artifact ID | Description |
| --- | --- |
| `originalkeen-license-model` | Protocol and data model definitions. |
| `originalkeen-license-core` | Core logic for hardware detection and validation. |
| `originalkeen-license-spring-boot-autoconfigure` | Auto-configuration for Spring Boot applications. |
| `originalkeen-license-spring-boot-starter` | Rapid integration entry point. |

## Compatibility

* **Java**: 17 or higher.
* **Spring Boot**: 3.x.x ecosystem.
* **Platforms**: Linux and Windows.

## Contributing

1. Fork the repository.
2. Implement features or bug fixes.
3. Submit a pull request to the `main` branch.

## License

This project is licensed under the **MIT License**.
