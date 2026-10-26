# 本地 MySQL：创建 ai_platform 库、表结构与种子数据（手工 DDL）
param(
    [string]$DbHost = "127.0.0.1",
    [int]$Port = 3306,
    [string]$User = "root",
    [string]$Password = $env:MYSQL_PASSWORD,
    [switch]$Recreate,
    [string]$SqlDir = (Join-Path (Split-Path -Parent $PSScriptRoot) "deploy\sql")
)

$ErrorActionPreference = "Stop"
# 保证通过管道传给 mysql 的是 UTF-8 字节，避免中文注释被按 GBK 解释
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($Password)) {
    throw "未读取到 MYSQL_PASSWORD。请先运行 .\scripts\load-local-mysql-env.ps1，或用 -Password 显式传入。"
}

$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysql)) {
    throw "mysql.exe not found. Install MySQL 8 or edit path in this script."
}

function Invoke-Sql([string]$sql) {
    $sql | & $mysql --default-character-set=utf8mb4 --host=$DbHost --port=$Port "-u$User" "-p$Password"
    if ($LASTEXITCODE -ne 0) {
        throw "SQL 执行失败（exit=$LASTEXITCODE）：$sql"
    }
}

function Invoke-SqlFile([string]$path) {
    Write-Host ">> $path"
    Get-Content $path -Encoding UTF8 -Raw | & $mysql --default-character-set=utf8mb4 --host=$DbHost --port=$Port "-u$User" "-p$Password"
    if ($LASTEXITCODE -ne 0) {
        throw "SQL 文件执行失败（exit=$LASTEXITCODE）：$path"
    }
}

if ($Recreate) {
    Write-Host ">> DROP DATABASE ai_platform (Recreate)"
    Invoke-Sql "DROP DATABASE IF EXISTS ai_platform;"
}

Invoke-SqlFile (Join-Path $SqlDir "01-init-database.sql")
Invoke-SqlFile (Join-Path $SqlDir "02-schema.sql")
Invoke-SqlFile (Join-Path $SqlDir "03-seed-data.sql")

Write-Host ""
Write-Host "库 ai_platform 已就绪（表结构 + 演示账号）。可直接启动微服务。"
Write-Host "若曾用旧版结构，请加 -Recreate 重建库后再启动。"
