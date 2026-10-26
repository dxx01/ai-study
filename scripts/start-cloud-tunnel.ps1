# Local dev with server DB: forward Redis + Nacos from 47.120.50.85 when ports are not public.
# Keep this window open. In server.env set:
#   REDIS_HOST=127.0.0.1
#   NACOS_SERVER_ADDR=127.0.0.1:8848
# Then: .\scripts\use-env.ps1 server
param(
    [string]$ServerHost = "47.120.50.85",
    [string]$SshUser = "root"
)

$ErrorActionPreference = "Stop"
Write-Host "Tunnel 127.0.0.1:6379 (Redis), :8848/:9848 (Nacos) -> $ServerHost"
Write-Host "Enter SSH password; leave window open."
ssh -N `
  -L 6379:127.0.0.1:6379 `
  -L 8848:127.0.0.1:8848 `
  -L 9848:127.0.0.1:9848 `
  "$SshUser@$ServerHost"
