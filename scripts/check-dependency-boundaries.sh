#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEFAULT_MAVEN_BIN="/usr/local/apache-maven-3.9.6/bin/mvn"
DEFAULT_SETTINGS_FILE="/usr/local/apache-maven-3.9.6/conf/settings-gpg.xml"

NEUTRAL_MODULES=(
  originalkeen-license-model
  originalkeen-license-core
  originalkeen-license-runtime
)

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

if grep -Fq '<artifactId>spring-boot-dependencies</artifactId>' "$PROJECT_DIR/pom.xml"; then
  echo "[ERROR] The neutral parent must not import the Spring Boot BOM" >&2
  exit 1
fi

dependency_report="$(mktemp)"
trap 'rm -f "$dependency_report"' EXIT

cd "$PROJECT_DIR"
for module in "${NEUTRAL_MODULES[@]}"; do
  : >"$dependency_report"
  "$MAVEN_BIN" \
    "${MAVEN_SETTINGS_ARGS[@]}" \
    -q \
    -am \
    -pl "$module" \
    dependency:tree \
    '-Dincludes=org.springframework:*,org.springframework.boot:*' \
    -DoutputFile="$dependency_report"

  if grep -Eq 'org\.springframework(\.boot)?:' "$dependency_report"; then
    echo "[ERROR] Spring dependency detected in neutral module: $module" >&2
    cat "$dependency_report" >&2
    exit 1
  fi
done

echo "Dependency boundary checks passed"
