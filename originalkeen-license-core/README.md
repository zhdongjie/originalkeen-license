# OriginalKeen License Core

`originalkeen-license-core` contains the low-level verification engine behind OriginalKeen License. It remains public, but it is now the expert layer rather than the default plain Java entry point.

## Position in the Repository

- Preferred plain Java path: use [../originalkeen-license-runtime/README.md](../originalkeen-license-runtime/README.md)
- Preferred Spring Boot path: use [../originalkeen-license-spring-boot-starter/README.md](../originalkeen-license-spring-boot-starter/README.md)
- Expert path: use `core` when you need direct control over keystore wiring, provider selection, or verification assembly

## Features

- Windows and Linux hardware providers for CPU, motherboard, IP, and MAC collection
- Shared base provider with cached IP and MAC discovery
- `FileKeyStoreParam` for classpath or filesystem keystore loading
- `LicenseManagerAdapter` for signature, expiry, and hardware-binding verification
- `LicenseVerifyService` for installation, 60-second success caching, optional hot reload, and detailed verification outcomes
- Extension points for custom `HardwareDataProvider` implementations

## Installation

### Maven

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>originalkeen-license-core</artifactId>
    <version>1.1.5</version>
</dependency>
```

### Gradle

```groovy
implementation 'org.eu.originalkeen:originalkeen-license-core:1.1.5'
```

## Advanced Manual Assembly

Use this path only when `runtime` is not sufficient for your integration.

```java
Preferences preferences = Preferences.userNodeForPackage(LicenseVerifyService.class);

FileKeyStoreParam publicStoreParam = new FileKeyStoreParam(
        LicenseVerifyService.class,
        "classpath:publicKey.keystore",
        "public",
        "changeit1",
        null
);

LicenseParam licenseParam = new DefaultLicenseParam(
        "MyAppLicense",
        preferences,
        publicStoreParam,
        new DefaultCipherParam("changeit1")
);

String osName = System.getProperty("os.name").toLowerCase();

HardwareDataProvider provider;
if (osName.startsWith("windows")) {
    provider = new WindowsHardwareProvider();
} else if (osName.startsWith("linux")) {
    provider = new LinuxHardwareProvider();
} else {
    throw new IllegalStateException("Register a custom HardwareDataProvider for " + osName);
}

LicenseManagerAdapter manager = new LicenseManagerAdapter(licenseParam, provider);
LicenseVerifyService service = new LicenseVerifyService(manager, "/opt/licenses/myapp.lic");

service.install("/opt/licenses/myapp.lic");

if (!service.verify()) {
    throw new IllegalStateException("License verification failed");
}
```

Notes:

- Pass the configured license path into `LicenseVerifyService` when you want hot reload checks.
- Use `new LicenseVerifyService(manager)` if you only want manual install and verify operations.
- `verifyDetailed()` exists for runtime adapters and infrastructure code that need structured verification metadata.
- If you only need a supported non-Spring application API, prefer `LicenseRuntime` instead of extending this assembly story further.

## Hardware Binding Model

License hardware requirements are stored in `LicenseCheckModel`, which is read from `LicenseContent#getExtra()`.

```text
LicenseCheckModel:
  - protocolVersion
  - ipAddress (List<String>)
  - macAddress (List<String>)
  - cpuSerial
  - mainBoardSerial
```

## Matching Rules

- CPU serial: exact match when configured
- Main-board serial: exact match when configured
- MAC address: verification passes when at least one runtime MAC matches
- IP address: verification passes when at least one runtime IP matches
- Empty or missing fields in the license mean that binding rule is skipped

## Logging and Operations

- Successful verification results are cached for 60 seconds by default.
- License reload checks can pick up a changed license file before the cache is reused.
- Expiry warnings are logged when less than 15 days remain.
- Installation and verification failures log diagnostic details, including current hardware info when installation fails.

## Supported Platforms

- Windows: built-in runtime provider
- Linux: built-in runtime provider
- macOS and other operating systems: provide a custom `HardwareDataProvider`

## License

This project is licensed under the Apache License 2.0.
