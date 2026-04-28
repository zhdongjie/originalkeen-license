# Client Information Collection Scripts

These scripts collect client hardware information for the current `LicenseCheckModel` structure.

Generated JSON fields:

- `protocolVersion`
- `ipAddress`
- `macAddress`
- `cpuSerial`
- `mainBoardSerial`

The output file can be returned directly to the license issuing side and mapped to the existing model without renaming fields.

## Linux

Run:

```bash
bash scripts/client-info/collect-client-info-linux.sh
```

Custom output file:

```bash
bash scripts/client-info/collect-client-info-linux.sh /tmp/client-license-info.json
```

Notes:

- The script tries to read CPU and main-board serials using the same general strategy as the current Linux provider in the Java runtime.
- Some Linux distributions require `sudo` for `dmidecode`.
- If `cpuSerial` or `mainBoardSerial` is empty, run the script again with elevated privileges when allowed.

## macOS

Run:

```bash
bash scripts/client-info/collect-client-info-macos.sh
```

Custom output file:

```bash
bash scripts/client-info/collect-client-info-macos.sh ~/Desktop/client-license-info.json
```

Notes:

- macOS does not expose a stable CPU serial for normal scripting access.
- This script uses the machine system serial as `cpuSerial`.
- This script uses `IOPlatformUUID` as `mainBoardSerial`.
- Your current Java runtime does not yet include a dedicated macOS hardware provider, so the collected values are mainly for issuing-side registration unless you add macOS verification support later.

## Windows

Run in PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\client-info\collect-client-info-windows.ps1
```

Custom output file:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\client-info\collect-client-info-windows.ps1 -OutputFile C:\temp\client-license-info.json
```

Notes:

- The script prefers `Get-NetIPAddress`, `Get-NetAdapter`, and CIM.
- It falls back to `wmic` when needed.
- Physical adapters are preferred to avoid collecting virtual network interfaces whenever possible.

## Optional Protocol Version Override

If you want to set a different protocol version:

Linux or macOS:

```bash
PROTOCOL_VERSION=1.0 bash scripts/client-info/collect-client-info-linux.sh
```

Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\client-info\collect-client-info-windows.ps1 -ProtocolVersion 1.0
```

## Example Output

```json
{
  "protocolVersion": "1.0",
  "ipAddress": [
    "192.168.1.10"
  ],
  "macAddress": [
    "00-1A-2B-3C-4D-5E"
  ],
  "cpuSerial": "BFEBFBFF000906EA",
  "mainBoardSerial": "PF2ABC123456789"
}
```