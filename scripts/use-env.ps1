# Copy deploy/env/local.env or server.env -> current.env (all services read current.env)
# Usage: .\scripts\use-env.ps1 local
#        .\scripts\use-env.ps1 server
param([string]$Target = "local")

$ErrorActionPreference = "Stop"
if ($Target -ne "local" -and $Target -ne "server") {
    throw "Target must be local or server. Example: .\scripts\use-env.ps1 server"
}

$root = Split-Path -Parent $PSScriptRoot
$envDir = Join-Path $root "deploy\env"
$source = Join-Path $envDir ($Target + ".env")
$current = Join-Path $envDir "current.env"

if (-not (Test-Path $source)) {
    throw "Missing $source - create deploy/env/local.env or server.env first."
}

Copy-Item -Path $source -Destination $current -Force

foreach ($name in @(
        "SPRING_PROFILES_ACTIVE",
        "MYSQL_HOST", "MYSQL_PORT", "MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD",
        "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD",
        "NACOS_SERVER_ADDR", "NACOS_USERNAME", "NACOS_PASSWORD"
    )) {
    Remove-Item -Path ("Env:" + $name) -ErrorAction SilentlyContinue
}

Write-Host ("Synced " + $source + " -> current.env")
if ($Target -eq "server") {
    Write-Host "Profile: server (remote MySQL/Redis from server.env)"
} else {
    Write-Host "Profile: dev (local MySQL/Redis from local.env)"
}
