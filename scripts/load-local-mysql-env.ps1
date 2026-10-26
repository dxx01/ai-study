# 兼容旧命令：从 local.env 加载 MYSQL_* 到当前 PowerShell（供 init-mysql-local.ps1）
& (Join-Path $PSScriptRoot "load-local-env.ps1")
