# License Core

`license-core` is a lightweight Java license management library designed for generating, installing, and validating software licenses based on hardware fingerprints and certificate verification.  
It supports both **Linux** and **Windows** environments and allows flexible hardware binding (CPU, mainboard, MAC, IP).

## Features

- Generate and install license certificates (XML + digital signature).
- Bind license to server hardware (CPU, motherboard, MAC, IP).
- Validate license at runtime with caching to improve performance.
- Multi-platform support: Linux, Windows.
- Provides hooks for custom hardware info providers.

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.eu.originalkeen</groupId>
    <artifactId>license-core</artifactId>
    <version>1.0.0</version>
</dependency>
````

### Gradle

```groovy
implementation 'org.eu.originalkeen:license-core:1.0.0'
```

## Usage

### 1. Implement Hardware Provider

You can extend `AbstractHardwareProvider` for your platform:

```java
HardwareDataProvider provider = new LinuxHardwareProvider();
// or for Windows
// HardwareDataProvider provider = new WindowsHardwareProvider();
```

### 2. Create License Manager

```java
LicenseParam param = new LicenseParam(); // configure keyStore, subject, etc.
LicenseManagerAdapter manager = new LicenseManagerAdapter(param, provider);
```

### 3. Install License

```java
LicenseVerifyService service = new LicenseVerifyService(manager);
service.install("/path/to/license.lic");
```

### 4. Verify License at Runtime

```java
boolean valid = service.verify();
if (!valid) {
    throw new RuntimeException("License verification failed");
}
```

## License File Structure

* Uses **XML** encoding.
* Contains `LicenseCheckModel` for hardware binding:

```text
LicenseCheckModel:
  - cpuSerial
  - mainBoardSerial
  - macAddress (List<String>)
  - ipAddress (List<String>)
```

* Supports validity period: `notBefore` and `notAfter`.

## Hardware Binding Rules

* CPU Serial: exact match
* Mainboard Serial: exact match
* MAC Address: at least one matches
* IP Address: at least one matches
* Rules are optional: empty list means no binding.

## Caching

License verification caches success results for **60 seconds** by default to reduce repeated hardware checks.

## Logging

* Uses `Log4j2` for logs.
* Logs license installation, verification, and expiry warnings.

## Examples

```java
public class LicenseDemo {
    public static void main(String[] args) {
        HardwareDataProvider provider = new LinuxHardwareProvider();
        LicenseParam param = new LicenseParam();
        LicenseManagerAdapter manager = new LicenseManagerAdapter(param, provider);
        LicenseVerifyService service = new LicenseVerifyService(manager);

        // Install license
        service.install("/opt/license/license.lic");

        // Verify license
        if (service.verify()) {
            System.out.println("License is valid!");
        } else {
            System.out.println("License is invalid or expired.");
        }
    }
}
```

## Supported Platforms

* Linux (tested on Ubuntu, CentOS)
* Windows (tested on Windows 10 / 11 / Server)

## Contributing

1. Fork the repository
2. Implement features or bug fixes
3. Submit a pull request

## License

This project is **proprietary** and intended for internal enterprise usage.
