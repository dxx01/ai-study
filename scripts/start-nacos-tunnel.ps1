# 可选：安全组未放行 Nacos 时用 SSH 隧道。已放行 47.120.50.85:8848 时不必运行。
param(
    [string]$ServerHost = "47.120.50.85",
    [string]$SshUser = "root"
)

$ErrorActionPreference = "Stop"
Write-Host "建立 Nacos 隧道：127.0.0.1:8848 / 127.0.0.1:9848 -> $ServerHost"
Write-Host "按提示输入服务器密码；保持本窗口打开。"
ssh -N -L 8848:127.0.0.1:8848 -L 9848:127.0.0.1:9848 "$SshUser@$ServerHost"
