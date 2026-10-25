# 从 deploy/env/local-mysql.env 加载本机 MySQL 环境变量（文件已被 gitignore）
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root "deploy\env\local-mysql.env"

if (-not (Test-Path $envFile)) {
    Write-Error "未找到 $envFile。请复制 deploy\env\local-mysql.env.example 为 local-mysql.env 并填写本机 root 密码。"
}

Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
        $parts = $line.Split("=", 2)
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

Write-Host "MYSQL_* 已从 deploy\env\local-mysql.env 加载（$env:MYSQL_USER @ $env:MYSQL_HOST`:$env:MYSQL_PORT/$env:MYSQL_DATABASE）"
