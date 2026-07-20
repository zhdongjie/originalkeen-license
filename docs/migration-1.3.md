# Migrating from 1.2.x to 1.3.0

Version 1.3.0 keeps the public license runtime and Spring configuration APIs intact while changing dependency ownership to behave like a conventional reusable library.

## Spring Boot Applications

Applications using `originalkeen-license-spring-boot-starter` normally require no source changes. The starter now includes Spring Boot's core starter directly and continues to activate Servlet filtering only when the application already has a Servlet Web stack.

The OriginalKeen BOM no longer imports `spring-boot-dependencies`. Make sure the application selects its Spring Boot version through its own parent or BOM, for example:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.9</version>
</parent>
```

Alternatively, import the Spring Boot BOM before importing the OriginalKeen BOM. The two BOMs now have separate responsibilities: Spring Boot manages its ecosystem and OriginalKeen manages only OriginalKeen modules.

## Plain Java Applications

The runtime no longer brings Log4j Core. If the application needs log output and does not already have an SLF4J provider, choose one explicitly. For example:

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.17</version>
    <scope>runtime</scope>
</dependency>
```

Applications that already provide Logback, Log4j through an SLF4J provider, or another SLF4J provider need no additional logging dependency.

## Dependency Exclusions

Review exclusions that were added to work around the 1.2.x Log4j dependencies. Exclusions for `log4j-core` or `log4j-api` on OriginalKeen artifacts are normally no longer needed.

## Verification Checklist

1. Confirm the application owns its Spring Boot version.
2. Run `mvn dependency:tree` and verify that only the intended logging provider is present.
3. Start the application with `originalkeen.license.enabled=false` and confirm all license auto-configuration backs off.
4. Re-enable licensing and verify startup installation and protected Servlet requests.
