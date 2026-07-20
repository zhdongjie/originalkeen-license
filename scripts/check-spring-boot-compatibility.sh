#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEFAULT_MAVEN_BIN="/usr/local/apache-maven-3.9.6/bin/mvn"
DEFAULT_SETTINGS_FILE="/usr/local/apache-maven-3.9.6/conf/settings-gpg.xml"
BOOT_COMPATIBILITY_VERSIONS="${BOOT_COMPATIBILITY_VERSIONS:-3.5.9}"

if [[ -z "${MAVEN_BIN:-}" ]]; then
  if [[ -x "$DEFAULT_MAVEN_BIN" ]]; then
    MAVEN_BIN="$DEFAULT_MAVEN_BIN"
  else
    MAVEN_BIN="$(command -v mvn || true)"
  fi
fi

if [[ -z "$MAVEN_BIN" || ! -x "$MAVEN_BIN" ]]; then
  echo "[ERROR] Missing Maven executable: $MAVEN_BIN" >&2
  exit 1
fi

if [[ -z "${SETTINGS_FILE:-}" && -f "$DEFAULT_SETTINGS_FILE" ]]; then
  SETTINGS_FILE="$DEFAULT_SETTINGS_FILE"
fi

MAVEN_SETTINGS_ARGS=()
if [[ -n "${SETTINGS_FILE:-}" ]]; then
  if [[ ! -f "$SETTINGS_FILE" ]]; then
    echo "[ERROR] Missing Maven settings: $SETTINGS_FILE" >&2
    exit 1
  fi
  MAVEN_SETTINGS_ARGS=(-s "$SETTINGS_FILE")
fi

cd "$PROJECT_DIR"
for boot_version in $BOOT_COMPATIBILITY_VERSIONS; do
  printf '\n==> Testing Spring Boot %s compatibility\n' "$boot_version"
  "$MAVEN_BIN" \
    "${MAVEN_SETTINGS_ARGS[@]}" \
    -B \
    -am \
    -pl originalkeen-license-spring-boot-autoconfigure \
    "-Dspring-boot.version=$boot_version" \
    test
done

echo "Spring Boot compatibility checks passed"
