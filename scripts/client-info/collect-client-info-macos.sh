#!/usr/bin/env bash

set -euo pipefail

OUTPUT_FILE="${1:-client-license-info-macos.json}"
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

unique_array() {
  if [[ $# -eq 0 ]]; then
    return 0
  fi
  printf '%s\n' "$@" | sed '/^$/d' | sort -u
}

collect_active_interfaces() {
  networksetup -listallhardwareports 2>/dev/null \
    | awk '
      /^Device: / { device = $2 }
      /^Ethernet Address: / && device != "" { print device "|" $3; device = "" }
    '
}

collect_ip_addresses() {
  local interface
  local ipv4
  local ipv6

  while IFS='|' read -r interface _; do
    [[ -z "$interface" ]] && continue
    [[ "$interface" =~ ^(lo0|awdl.*|llw.*|utun.*|bridge.*)$ ]] && continue

    ipv4="$(ipconfig getifaddr "$interface" 2>/dev/null || true)"
    if [[ -n "$ipv4" ]]; then
      IP_ADDRESSES+=("$ipv4")
    fi

    while read -r ipv6; do
      [[ -z "$ipv6" ]] && continue
      [[ "$ipv6" == fe80:* ]] && continue
      IP_ADDRESSES+=("$ipv6")
    done < <(
      ifconfig "$interface" 2>/dev/null \
        | awk '/inet6 / {print $2}' \
        | sed '/^$/d'
    )
  done < <(collect_active_interfaces)

  if [[ ${#IP_ADDRESSES[@]} -gt 0 ]]; then
    mapfile -t IP_ADDRESSES < <(unique_array "${IP_ADDRESSES[@]}")
  fi
}

collect_mac_addresses() {
  local interface
  local mac
  local normalized

  while IFS='|' read -r interface mac; do
    [[ -z "$interface" || -z "$mac" ]] && continue
    [[ "$interface" =~ ^(lo0|awdl.*|llw.*|utun.*|bridge.*)$ ]] && continue
    [[ "$mac" == "00:00:00:00:00:00" ]] && continue

    normalized="$(printf '%s' "$mac" | tr '[:lower:]' '[:upper:]' | tr ':' '-')"
    MAC_ADDRESSES+=("$normalized")
  done < <(collect_active_interfaces)

  if [[ ${#MAC_ADDRESSES[@]} -gt 0 ]]; then
    mapfile -t MAC_ADDRESSES < <(unique_array "${MAC_ADDRESSES[@]}")
  fi
}

get_system_serial() {
  system_profiler SPHardwareDataType 2>/dev/null \
    | awk -F': ' '/Serial Number \(system\)/ {print $2; exit}'
}

get_platform_uuid() {
  ioreg -rd1 -c IOPlatformExpertDevice 2>/dev/null \
    | awk -F'"' '/IOPlatformUUID/ {print $(NF-1); exit}'
}

main() {
  local cpu_serial
  local main_board_serial

  collect_ip_addresses
  collect_mac_addresses
  cpu_serial="$(get_system_serial)"
  main_board_serial="$(get_platform_uuid)"

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
  printf 'Note: macOS does not expose a stable CPU serial, so the system serial is used as cpuSerial.\n'
}

main "$@"