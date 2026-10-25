# 启动本地 MySQL + Redis（需已安装 Docker）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
docker compose -f deploy/docker-compose.yml up -d
Write-Host "基础设施已启动。MySQL: localhost:3306  Redis: localhost:6379"
Write-Host "首次空数据卷会自动建库、建表并写入演示账号；存量卷不会重复初始化。"
