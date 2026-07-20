#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MAVEN_BIN="${MAVEN_BIN:-/usr/local/apache-maven-3.9.6/bin/mvn}"
SETTINGS_FILE="${SETTINGS_FILE:-/usr/local/apache-maven-3.9.6/conf/settings-gpg.xml}"
SKIP_TESTS="${SKIP_TESTS:-true}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $1" >&2
    exit 1
  fi
}

require_file() {
  if [[ ! -f "$1" ]]; then
    echo "[ERROR] Missing required file: $1" >&2
    exit 1
  fi
}

require_env_if_referenced() {
  local key="$1"
  local ref="\${env.${key}}"
  if grep -Fq "$ref" "$SETTINGS_FILE"; then
    if [[ -z "${!key:-}" ]]; then
      echo "[ERROR] Environment variable $key is required by $SETTINGS_FILE" >&2
      exit 1
    fi
  fi
}

print_step() {
  printf '\n==> %s\n' "$1"
}

require_command bash
require_command git
require_command gpg
require_file "$MAVEN_BIN"
require_file "$SETTINGS_FILE"
require_file "$SCRIPT_DIR/check-dependency-boundaries.sh"
require_file "$SCRIPT_DIR/check-spring-boot-compatibility.sh"

require_env_if_referenced CENTRAL_TOKEN_USERNAME
require_env_if_referenced CENTRAL_TOKEN_PASSWORD
require_env_if_referenced GPG_KEY_ID
require_env_if_referenced GPG_PASSPHRASE

if [[ -n "${GPG_KEY_ID:-}" ]]; then
  print_step "Checking GPG secret key ${GPG_KEY_ID}"
  gpg --list-secret-keys --keyid-format LONG "$GPG_KEY_ID" >/dev/null
  print_step "Verifying GPG signing with ${GPG_KEY_ID}"
  printf 'release-check\n' | gpg --batch --yes --local-user "$GPG_KEY_ID" --clearsign >/dev/null
else
  print_step "Verifying default GPG signing"
  printf 'release-check\n' | gpg --batch --yes --clearsign >/dev/null
fi

print_step "Running Maven preflight build"
cd "$PROJECT_DIR"
"$MAVEN_BIN" \
  -s "$SETTINGS_FILE" \
  -DskipTests="$SKIP_TESTS" \
  -Dgpg.skip=true \
  clean install

print_step "Checking dependency boundaries"
MAVEN_BIN="$MAVEN_BIN" SETTINGS_FILE="$SETTINGS_FILE" \
  bash "$SCRIPT_DIR/check-dependency-boundaries.sh"

if [[ "$SKIP_TESTS" != "true" ]]; then
  print_step "Checking newer Spring Boot compatibility"
  MAVEN_BIN="$MAVEN_BIN" SETTINGS_FILE="$SETTINGS_FILE" \
    bash "$SCRIPT_DIR/check-spring-boot-compatibility.sh"
fi

print_step "Preflight checks passed"
