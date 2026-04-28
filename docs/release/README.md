# OriginalKeen License Release Guide

This document is the standard release template for this project. Follow it in order and you can publish to Maven Central without reconstructing the steps from memory.

## 1. Scope

Use this guide when:

- You are publishing a new version under `org.eu.originalkeen`
- You are releasing from WSL or Linux
- You want one fixed sequence that covers GPG, Maven, Central Portal, validation, and publish

Validated release environment for this project:

- Project path: `/usr/local/project/originalkeen-license`
- Maven binary: `/usr/local/apache-maven-3.9.6/bin/mvn`
- Maven settings: `/usr/local/apache-maven-3.9.6/conf/settings-gpg.xml`
- Release profile: `release`

## 2. Project-Specific Release Rules

This repository has a few rules that are based on real Central publishing results, not theory.

### 2.1 Versioning Rules

- Release only from the root parent project
- Change versions with `versions:set`
- Keep explicit `groupId` and `version` in child module POMs

Why this matters:

This project previously failed Central validation with errors such as:

- `Failed to get coordinates from pom file`
- `Failed to associate file with coordinates`

In practice, Central validated successfully only after child module POMs explicitly declared `groupId` and `version`. Do not revert that pattern unless you want to re-verify the entire publishing chain.

### 2.2 Flattened POM Rules

This project intentionally uses `flatten-maven-plugin`, which generates `.flattened-pom.xml` files during build and release.

What it does:

- Produces repository-friendly published POMs
- Resolves inherited metadata into a more stable final form
- Helps Sonatype Central parse module coordinates reliably

Conclusion:

- `.flattened-pom.xml` files are expected build artifacts
- Do not commit them
- Do not remove `flatten-maven-plugin` casually

## 3. One-Time Setup

If your machine has already published successfully, you can jump to section 4.

### 3.1 Sonatype Central Account

Confirm the following:

- You can sign in to [Sonatype Central Portal](https://central.sonatype.com/)
- Your current account can manage the `org.eu.originalkeen` namespace
- You have created a Central User Token for publishing

Store the token in Maven settings, not in the repository.

Official references:

- [Central Portal](https://central.sonatype.com/)
- [Publish Portal Docs](https://central.sonatype.org/publish/publish-portal/)
- [Namespace Docs](https://central.sonatype.org/register/namespace/)

### 3.2 GPG Setup

Maven Central requires signed artifacts, so you need a usable GPG secret key.

List existing secret keys:

```bash
gpg --list-secret-keys --keyid-format LONG
```

If you do not have one yet, generate a key:

```bash
gpg --full-generate-key
```

Suggested options:

- Type: `RSA and RSA`
- Size: `4096`
- Expiry: choose according to your security policy
- Name and email: keep them aligned with your publishing identity

Check the key id again:

```bash
gpg --list-secret-keys --keyid-format LONG
```

Example output:

```text
sec   rsa4096/ABCD1234EF567890 2026-04-27 [SC]
```

In this example, `ABCD1234EF567890` is the key id used by Maven.

If you need the public key:

```bash
gpg --armor --export ABCD1234EF567890
```

Official reference:

- [Publishing Requirements](https://central.sonatype.org/publish/requirements/)

### 3.3 Maven Settings

This repository now provides two standard Maven settings templates:

- `docs/release/settings.xml.example`: base Central publishing settings
- `docs/release/settings-gpg.xml.example`: Central publishing settings plus GPG properties

Recommended usage:

- Use `settings.xml.example` when you only want the minimal Central server configuration reference
- Use `settings-gpg.xml.example` for real signed releases in this project

Copy the base template if you want a plain settings file:

```bash
cp docs/release/settings.xml.example /usr/local/apache-maven-3.9.6/conf/settings.xml
```

Copy the release template if you want the ready-to-use signed release settings file:

```bash
cp docs/release/settings-gpg.xml.example /usr/local/apache-maven-3.9.6/conf/settings-gpg.xml
```

Recommended approach:

- Use the `central` server id for the Central User Token
- Use `gpg.keyname` for your signing key id in the GPG template
- Prefer environment variables for all secrets

Example WSL environment variables:

```bash
export CENTRAL_TOKEN_USERNAME="your-central-token-username"
export CENTRAL_TOKEN_PASSWORD="your-central-token-password"
export GPG_KEY_ID="ABCD1234EF567890"
export GPG_PASSPHRASE="your-passphrase"
```

If you use `gpg-agent`, you may omit `gpg.passphrase`, but make sure the key is unlocked before release.

### 3.4 Preflight Check

Run the standard preflight script:

```bash
bash scripts/check-release.sh
```

What it checks:

- Required commands exist
- Maven settings file exists
- Required environment variables are present when the settings file depends on them
- Your GPG key can sign
- A local `clean install` succeeds with `-Dgpg.skip=true`

## 4. Standard Release Flow

This is the sequence to use every time.

### 4.1 Enter the Project Root

```bash
cd /usr/local/project/originalkeen-license
```

### 4.2 Check the Working Tree

```bash
git status
```

Recommended release conditions:

- No unrelated changes
- Version changes are the only intentional release edits

### 4.3 Update the Version

Example for `1.1.3`:

```bash
/usr/local/apache-maven-3.9.6/bin/mvn versions:set -DnewVersion=1.1.3
```

Notes:

- The repository is configured with `processAllModules=true`
- Parent and child module versions are updated together
- `generateBackupPoms=false`, so do not expect `pom.xml.versionsBackup`
- If you want to cancel the version bump, prefer Git-based rollback over `versions:revert`

### 4.4 Verify the Version Change

```bash
git diff -- pom.xml */pom.xml
```

Confirm that:

- The parent POM uses the target version
- Child modules use the same target version
- Child modules still contain explicit `groupId` and `version`

### 4.5 Run the Preflight Script

```bash
bash scripts/check-release.sh
```

If this passes, your project is ready for a signed release.

### 4.6 Publish

Use the standard release script:

```bash
bash scripts/release.sh 1.1.3
```

What the script does:

- Verifies required tools and files exist
- Verifies the Git working tree is clean before release
- Runs `mvn versions:set -DnewVersion=...`
- Runs the preflight check script
- Runs `mvn clean deploy -Prelease`

If you already updated the version manually and only want the deploy step, use:

```bash
SKIP_VERSION_SET=true bash scripts/release.sh 1.1.3
```

### 4.7 Check Portal Status

In Central Portal, focus on two states:

- `VALIDATED`: uploaded content and coordinates passed validation
- `PUBLISHED`: the release is complete

If you see all components validated and the final status is `PUBLISHED`, the release succeeded.

### 4.8 Verify Search Visibility

Indexing can take a few minutes. Check:

- [Central Search](https://central.sonatype.com/)

Search for:

- `org.eu.originalkeen`
- `originalkeen-license`
- The target version

## 5. Standard Commands

### 5.1 Manual Version Update

```bash
cd /usr/local/project/originalkeen-license
/usr/local/apache-maven-3.9.6/bin/mvn versions:set -DnewVersion=1.1.3
```

### 5.2 Manual Preflight Build

```bash
cd /usr/local/project/originalkeen-license
/usr/local/apache-maven-3.9.6/bin/mvn \
  -s /usr/local/apache-maven-3.9.6/conf/settings-gpg.xml \
  -DskipTests -Dgpg.skip=true clean install
```

### 5.3 Scripted Preflight

```bash
bash scripts/check-release.sh
```

### 5.4 Scripted Release

```bash
bash scripts/release.sh 1.1.3
```

## 6. Environment Variables Used by the Scripts

You can override defaults with environment variables.

- `MAVEN_BIN`: Maven binary path. Default: `/usr/local/apache-maven-3.9.6/bin/mvn`
- `SETTINGS_FILE`: Maven settings path. Default: `/usr/local/apache-maven-3.9.6/conf/settings-gpg.xml`
- `SKIP_TESTS`: Default: `true`
- `GPG_KEY_ID`: Your GPG key id if your settings file references it
- `GPG_PASSPHRASE`: Optional if you rely on `gpg-agent`
- `CENTRAL_TOKEN_USERNAME`: Central token username if your settings file references it
- `CENTRAL_TOKEN_PASSWORD`: Central token password if your settings file references it
- `SKIP_VERSION_SET`: Set to `true` if the version has already been updated manually

## 7. Common Questions

### 7.1 IntelliJ Shows `central-publishing-maven-plugin:0.9.0 not found`

If WSL Maven can build and publish successfully but IntelliJ still marks the plugin as unresolved, the issue is usually IDE-side Maven resolution rather than an invalid POM.

Check:

- Whether IntelliJ uses the correct Maven installation
- Whether IntelliJ is in offline mode
- Whether IntelliJ uses the same settings file strategy as WSL
- Whether a proxy or mirror is blocking plugin resolution

### 7.2 Why Do `.flattened-pom.xml` Files Appear?

Because `flatten-maven-plugin` is enabled. This is expected and important for this project's publishing compatibility.

### 7.3 Why Do Child Modules Explicitly Declare `groupId` and `version`?

Because this project published successfully only after those coordinates were made explicit in child POMs. This is a project compatibility rule, not a general Maven rule.

### 7.4 How Do I Roll Back a Version Change?

If the version bump is not committed yet, use Git to roll it back. Do not depend on `versions:revert` in this repository.

## 8. Post-Release Checklist

After a successful release, it is still worth doing the following:

- Commit the version update
- Create a Git tag
- Record release notes or a changelog entry
- Save the Central deployment id for later troubleshooting

## 9. Official References

- [Sonatype Central Portal](https://central.sonatype.com/)
- [Publish by Portal API](https://central.sonatype.org/publish/publish-portal-api/)
- [Publishing Requirements](https://central.sonatype.org/publish/requirements/)
- [Namespace Registration](https://central.sonatype.org/register/namespace/)
- [MojoHaus Flatten Maven Plugin](https://www.mojohaus.org/flatten-maven-plugin/)