# 已废弃：请使用 use-env.ps1 -Target server，连接信息在 current.env
Write-Warning "load-server-app-env.ps1 已废弃，请运行: .\scripts\use-env.ps1 -Target server"
& (Join-Path $PSScriptRoot "use-env.ps1") -Target server
