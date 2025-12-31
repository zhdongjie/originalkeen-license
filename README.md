# OriginalKeen License

**OriginalKeen License** is a sophisticated **Java license management and verification system** designed for enterprise-grade applications. It provides a robust framework for hardware-bound licensing, automated installation, and seamless integration with the Spring Boot ecosystem.

This project follows a modular architecture and utilizes the **BOM (Bill of Materials)** pattern to ensure dependency consistency and simplified version management.

---

## Key Features

* **Hardware Fingerprinting**: Bind licenses to specific hardware identifiers, including CPU ID, motherboard serial number, IP addresses, and MAC addresses.
* **Expiration Management**: Integrated lifecycle monitoring with automated warnings 15 days prior to license expiration.
* **Web Request Enforcement**: High-performance Web Interceptor support with configurable Ant-style path whitelisting.
* **Cross-Platform Compatibility**: Native support for both Windows and Linux environments.
* **Verification Caching**: Optimized performance through an internal cache (default 60s) for successful verification results.

---

## Module Overview

The project is organized into five specialized modules:

1. **`originalkeen-license-dependencies` (BOM)**: The **Single Source of Truth**. It centralizes version definitions for all internal modules and third-party dependencies to prevent version conflicts.
2. **`originalkeen-license-model`**: Defines the core data models and constants for the license protocol.
3. **`originalkeen-license-core`**: The engine of the system, providing hardware detection, license installation, and verification logic.
4. **`originalkeen-license-spring-boot-autoconfigure`**: Handles the automated registration of Spring beans based on the application environment.
5. **`originalkeen-license-spring-boot-starter`**: The primary entry point for users, offering zero-configuration integration for Spring Boot applications.

---

## Quick Start

### 1. Import the BOM

To ensure version alignment, it is highly recommended to import the BOM in your project's `dependencyManagement` section:

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

```xml
<dependencies>
    <dependency>
        <groupId>org.eu.originalkeen</groupId>
        <artifactId>originalkeen-license-spring-boot-starter</artifactId>
    </dependency>
</dependencies>

```

### 2. Configuration

Configure the license properties in your `application.yml`:

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

---

## Development & Release Workflow

We adhere to a standardized Maven multi-module release process. **Directly modifying version numbers in sub-module `pom.xml` files is strictly prohibited.**

### Standard Release Steps
1. **Develop**: Implement features and tests.
2. **Version Update**: One command to sync all modules (including BOM):
```bash
   mvn versions:set -DnewVersion=1.0.2

```


3. **Confirmation**: Verify the changes across all modules and commit the new versions:
```bash
mvn versions:commit

```


4. **Deployment**: Execute the release profile to trigger GPG signing and deploy artifacts to the Central Repository:
```bash
mvn clean deploy -P release

```



---

## Important Notes

* **System Privileges**: Extracting CPU or motherboard serial numbers on Linux may require `root` or elevated administrative privileges.
* **GPG Signing**: Ensure your `gpg-agent` is running and the signing key is unlocked before initiating a release.
* **Inheritance**: Sub-module versions are managed by the Parent POM and the internal BOM. No manual version declarations are needed within individual modules.

---

## License

This project is licensed under the **MIT License**.
