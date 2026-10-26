# 从 deploy/env/local.env 加载 MYSQL_* 到当前 PowerShell（供 init-mysql-local.ps1）
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root "deploy\env\local.env"
if (-not (Test-Path $envFile)) {
    Write-Error "未找到 $envFile，请创建 deploy/env/local.env（见 deploy/env/README.md）"
}
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) { return }
    $eq = $line.IndexOf("=")
    if ($eq -lt 1) { return }
    $name = $line.Substring(0, $eq).Trim()
    $value = $line.Substring($eq + 1).Trim()
    Set-Item -Path "Env:$name" -Value $value
}
Write-Host "MYSQL_* 已从 deploy\env\local.env 加载（$env:MYSQL_USER @ $env:MYSQL_HOST`:$env:MYSQL_PORT/$env:MYSQL_DATABASE）"
