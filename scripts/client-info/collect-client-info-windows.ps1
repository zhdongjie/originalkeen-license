param(
    [string]$OutputFile = "client-license-info-windows.json",
    [string]$ProtocolVersion = "1.0"
)

$ErrorActionPreference = "Stop"

function Get-UniqueValues {
    param(
        [Parameter(ValueFromPipeline = $true)]
        [string[]]$Values
    )

    process {
        $Values `
            | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } `
            | ForEach-Object { $_.Trim() } `
            | Sort-Object -Unique
    }
}

function Normalize-MacAddress {
    param([string]$MacAddress)

    if ([string]::IsNullOrWhiteSpace($MacAddress)) {
        return $null
    }

    $normalized = $MacAddress.Trim().ToUpperInvariant().Replace(":", "-")
    if ($normalized -eq "00-00-00-00-00-00") {
        return $null
    }

    return $normalized
}

function Get-IpAddresses {
    $results = @()

    try {
        $results += Get-NetIPAddress -AddressFamily IPv4 -ErrorAction Stop `
            | Where-Object {
                $_.IPAddress -and
                $_.IPAddress -ne "127.0.0.1" -and
                $_.InterfaceAlias -notmatch "Loopback|vEthernet|Docker|Hyper-V|WSL"
            } `
            | Select-Object -ExpandProperty IPAddress
    } catch {
    }

    try {
        $results += Get-NetIPAddress -AddressFamily IPv6 -ErrorAction Stop `
            | Where-Object {
                $_.IPAddress -and
                $_.IPAddress -ne "::1" -and
                $_.IPAddress -notlike "fe80:*" -and
                $_.InterfaceAlias -notmatch "Loopback|vEthernet|Docker|Hyper-V|WSL"
            } `
            | Select-Object -ExpandProperty IPAddress
    } catch {
    }

    return @($results | Get-UniqueValues)
}

function Get-MacAddresses {
    $results = @()

    try {
        $results += Get-NetAdapter -Physical -ErrorAction Stop `
            | Where-Object {
                $_.MacAddress -and
                $_.Status -ne "Disabled"
            } `
            | ForEach-Object { Normalize-MacAddress $_.MacAddress }
    } catch {
        try {
            $results += Get-CimInstance Win32_NetworkAdapter -ErrorAction Stop `
                | Where-Object {
                    $_.MACAddress -and
                    $_.PhysicalAdapter -eq $true
                } `
                | ForEach-Object { Normalize-MacAddress $_.MACAddress }
        } catch {
        }
    }

    return @($results | Get-UniqueValues)
}

function Get-CpuSerial {
    try {
        $value = Get-CimInstance Win32_Processor -ErrorAction Stop `
            | Select-Object -First 1 -ExpandProperty ProcessorId
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value.Trim()
        }
    } catch {
    }

    try {
        $wmicOutput = & wmic cpu get processorid 2>$null
        $value = $wmicOutput `
            | Where-Object { $_ -and $_ -notmatch "ProcessorId" } `
            | Select-Object -First 1
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value.Trim()
        }
    } catch {
    }

    return ""
}

function Get-MainBoardSerial {
    try {
        $value = Get-CimInstance Win32_BaseBoard -ErrorAction Stop `
            | Select-Object -First 1 -ExpandProperty SerialNumber
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value.Trim()
        }
    } catch {
    }

    try {
        $wmicOutput = & wmic baseboard get serialnumber 2>$null
        $value = $wmicOutput `
            | Where-Object { $_ -and $_ -notmatch "SerialNumber" } `
            | Select-Object -First 1
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            return $value.Trim()
        }
    } catch {
    }

    return ""
}

$payload = [ordered]@{
    protocolVersion = $ProtocolVersion
    ipAddress       = @(Get-IpAddresses)
    macAddress      = @(Get-MacAddresses)
    cpuSerial       = Get-CpuSerial
    mainBoardSerial = Get-MainBoardSerial
}

$payload | ConvertTo-Json -Depth 4 | Set-Content -Path $OutputFile -Encoding UTF8
Write-Host "Client information written to $OutputFile"