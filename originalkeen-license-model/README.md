# OriginalKeen License Model

`originalkeen-license-model` defines the protocol objects shared between the runtime verification side and the license issuing side.

The stability of this module matters to both the current release line and the planned V2 runtime layer because it is the shared contract between scripts, issuing workflows, and runtime verification.

## Included Types

- `LicenseCheckModel`: hardware binding payload for CPU, motherboard, IP, and MAC matching
- `LicenseHeader`: optional metadata object for issuer-side or integration-side protocol extensions
- `LicenseProtocol`: marker interface for serializable, versioned protocol objects

## Compatibility Rules

- Package names must remain stable.
- Fields may be added, but they must not be removed or renamed.
- Breaking protocol changes require a major version bump.

## Serialization

- All protocol objects implement Java `Serializable`.
- Producers should set `protocolVersion`, and consumers should preserve it during transport or storage.

## Typical Role in the Project

- `originalkeen-license-core` reads `LicenseCheckModel` during runtime verification.
- Client information collection scripts emit JSON using the same field names as `LicenseCheckModel`.
- Issuing-side tooling can reuse the same model classes to avoid field-name drift across systems.

## License

This project is licensed under the Apache License 2.0.
