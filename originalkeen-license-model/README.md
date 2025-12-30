# originalkeen-license-model

This module defines the **license protocol** shared between:

- License runtime (Spring Boot starter)
- License platform (issuer / management system)

## Compatibility Rules

- Package name MUST NOT be changed
- Fields can be added, but MUST NOT be removed or renamed
- Breaking changes require a major version bump

## Serialization

- Current protocol uses Java Serializable
- Protocol version is defined by `protocolVersion`
