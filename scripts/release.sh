#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CHECK_SCRIPT="$SCRIPT_DIR/check-release.sh"
MAVEN_BIN="${MAVEN_BIN:-/usr/local/apache-maven-3.9.6/bin/mvn}"
SETTINGS_FILE="${SETTINGS_FILE:-/usr/local/apache-maven-3.9.6/conf/settings-gpg.xml}"
SKIP_VERSION_SET="${SKIP_VERSION_SET:-false}"

usage() {
  cat <<'EOF'
Usage:
  bash scripts/release.sh <new-version>

Optional environment variables:
  MAVEN_BIN         Maven binary path
  SETTINGS_FILE     Maven settings file path
  SKIP_TESTS        Passed through to check-release.sh
  SKIP_VERSION_SET  Set to true if the version has already been updated manually
EOF
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $1" >&2
    exit 1
  fi
}

print_step() {
  printf '\n==> %s\n' "$1"
}

ensure_clean_worktree() {
  if [[ -n "$(git status --porcelain)" ]]; then
    echo "[ERROR] Git working tree is not clean. Commit, stash, or discard changes before release." >&2
    git status --short >&2
    exit 1
  fi
}

NEW_VERSION="${1:-}"
if [[ -z "$NEW_VERSION" ]]; then
  usage
  exit 1
fi

require_command bash
require_command git
require_file() {
  if [[ ! -f "$1" ]]; then
    echo "[ERROR] Missing required file: $1" >&2
    exit 1
  fi
}
require_file "$MAVEN_BIN"
require_file "$SETTINGS_FILE"
require_file "$CHECK_SCRIPT"

cd "$PROJECT_DIR"
print_step "Validating clean Git working tree"
ensure_clean_worktree

if [[ "$SKIP_VERSION_SET" != "true" ]]; then
  print_step "Setting Maven version to $NEW_VERSION"
  "$MAVEN_BIN" versions:set -DnewVersion="$NEW_VERSION"
else
  print_step "Skipping versions:set because SKIP_VERSION_SET=true"
fi

print_step "Running release preflight checks"
bash "$CHECK_SCRIPT"

print_step "Publishing version $NEW_VERSION to Maven Central"
"$MAVEN_BIN" \
  -s "$SETTINGS_FILE" \
  clean deploy -Prelease

print_step "Release command completed"
printf 'Check deployment status in Sonatype Central Portal: %s\n' 'https://central.sonatype.com/'