#!/usr/bin/env bash

set -euo pipefail

OUTPUT_FILE="${1:-client-license-info-linux.json}"
PROTOCOL_VERSION="${PROTOCOL_VERSION:-1.0}"
IP_ADDRESSES=()
MAC_ADDRESSES=()

json_escape() {
  local value="${1:-}"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  value="${value//$'\n'/\\n}"
  value="${value//$'\r'/\\r}"
  value="${value//$'\t'/\\t}"
  printf '%s' "$value"
}

join_by_comma() {
  local first=1
  local item
  for item in "$@"; do
    if [[ $first -eq 0 ]]; then
      printf ','
    fi
    first=0
    printf '"%s"' "$(json_escape "$item")"
  done
}

read_first_non_empty_line() {
  local path="$1"
  if [[ -r "$path" ]]; then
    while IFS= read -r line; do
      line="$(printf '%s' "$line" | xargs)"
      if [[ -n "$line" ]]; then
        printf '%s' "$line"
        return 0
      fi
    done < "$path"
  fi
  return 1
}

run_quiet_command() {
  local output
  if output="$("$@" 2>/dev/null)"; then
    printf '%s' "$output"
  fi
}

unique_array() {
  if [[ $# -eq 0 ]]; then
    return 0
  fi
  printf '%s\n' "$@" | sed '/^$/d' | sort -u
}

collect_ip_addresses() {
  local command_output
  local address

  command_output="$(run_quiet_command ip -o -4 addr show up scope global || true)"
  while read -r address; do
    [[ -z "$address" ]] && continue
    IP_ADDRESSES+=("$address")
  done < <(printf '%s\n' "$command_output" | awk '{print $4}' | cut -d/ -f1 | sed '/^$/d' | sort -u)

  command_output="$(run_quiet_command ip -o -6 addr show up scope global || true)"
  while read -r address; do
    [[ -z "$address" ]] && continue
    [[ "$address" == fe80:* ]] && continue
    IP_ADDRESSES+=("$address")
  done < <(printf '%s\n' "$command_output" | awk '{print $4}' | cut -d/ -f1 | sed '/^$/d' | sort -u)

  if [[ ${#IP_ADDRESSES[@]} -gt 0 ]]; then
    mapfile -t IP_ADDRESSES < <(unique_array "${IP_ADDRESSES[@]}")
  fi
}

collect_mac_addresses() {
  local interface
  local mac
  local normalized

  while read -r interface; do
    [[ -z "$interface" ]] && continue
    if [[ "$interface" =~ ^(lo|docker.*|veth.*|br-.*|virbr.*|flannel.*|cni.*|tun.*|tap.*)$ ]]; then
      continue
    fi

    mac="$(read_first_non_empty_line "/sys/class/net/${interface}/address" || true)"
    [[ -z "$mac" ]] && continue
    [[ "$mac" == "00:00:00:00:00:00" ]] && continue

    normalized="$(printf '%s' "$mac" | tr '[:lower:]' '[:upper:]' | tr ':' '-')"
    MAC_ADDRESSES+=("$normalized")
  done < <(ls /sys/class/net 2>/dev/null || true)

  if [[ ${#MAC_ADDRESSES[@]} -gt 0 ]]; then
    mapfile -t MAC_ADDRESSES < <(unique_array "${MAC_ADDRESSES[@]}")
  fi
}

get_cpu_serial() {
  local serial=""

  serial="$(bash -lc "dmidecode -t processor | grep 'ID' | awk -F ':' '{print \$2}' | head -n 1" 2>/dev/null | xargs || true)"
  if [[ -z "$serial" ]]; then
    serial="$(bash -lc "grep 'Serial' /proc/cpuinfo | awk -F ':' '{print \$2}' | head -n 1" 2>/dev/null | xargs || true)"
  fi

  printf '%s' "$serial"
}

get_main_board_serial() {
  local serial=""

  serial="$(read_first_non_empty_line /sys/class/dmi/id/board_serial || true)"
  if [[ -z "$serial" ]]; then
    serial="$(bash -lc "dmidecode -t baseboard | grep 'Serial Number' | awk -F ':' '{print \$2}' | head -n 1" 2>/dev/null | xargs || true)"
  fi
  if [[ -z "$serial" ]]; then
    serial="$(read_first_non_empty_line /sys/class/dmi/id/product_serial || true)"
  fi

  printf '%s' "$serial"
}

main() {
  local cpu_serial
  local main_board_serial

  collect_ip_addresses
  collect_mac_addresses
  cpu_serial="$(get_cpu_serial)"
  main_board_serial="$(get_main_board_serial)"

  cat > "$OUTPUT_FILE" <<EOF
{
  "protocolVersion": "$(json_escape "$PROTOCOL_VERSION")",
  "ipAddress": [$(join_by_comma "${IP_ADDRESSES[@]}")],
  "macAddress": [$(join_by_comma "${MAC_ADDRESSES[@]}")],
  "cpuSerial": "$(json_escape "$cpu_serial")",
  "mainBoardSerial": "$(json_escape "$main_board_serial")"
}
EOF

  printf 'Client information written to %s\n' "$OUTPUT_FILE"
  if [[ -z "$cpu_serial" || -z "$main_board_serial" ]]; then
    printf 'Warning: Some hardware serial fields are empty. Try running with sudo if dmidecode requires elevated permissions.\n'
  fi
}

main "$@"