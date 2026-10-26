# 将 deploy/（SQL + docker-compose.server.yml + server.env）同步到服务器并 docker compose up -d
param(
    [Parameter(Mandatory = $true)]
    [string]$ServerHost,
    [string]$SshUser = "root",
    [string]$RemoteDir = "/opt/ai-study/deploy"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$deployLocal = Join-Path $repoRoot "deploy"
$serverEnv = Join-Path $deployLocal "env\server.env"

if (-not (Test-Path $serverEnv)) {
    throw "缺少 deploy\env\server.env，请先在本机创建并填写密码"
}

$staging = Join-Path $env:TEMP "ai-study-deploy-$(Get-Random)"
New-Item -ItemType Directory -Force -Path $staging | Out-Null
Copy-Item -Recurse (Join-Path $deployLocal "sql") (Join-Path $staging "sql")
Copy-Item (Join-Path $deployLocal "docker-compose.server.yml") $staging
New-Item -ItemType Directory -Force -Path (Join-Path $staging "env") | Out-Null
Copy-Item $serverEnv (Join-Path $staging "env\server.env")

$remote = "${SshUser}@${ServerHost}"
Write-Host ">>> 创建远程目录 $RemoteDir"
ssh $remote "mkdir -p $RemoteDir"

Write-Host ">>> 上传 deploy 包（scp）"
scp -r "$staging\*" "${remote}:${RemoteDir}/"

Write-Host ">>> 安装 Docker（若未安装）并启动 compose"
$remoteCmd = @"
set -e
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
fi
cd $RemoteDir
docker compose --env-file env/server.env -f docker-compose.server.yml up -d
docker compose --env-file env/server.env -f docker-compose.server.yml ps
"@
ssh $remote $remoteCmd

Remove-Item -Recurse -Force $staging
Write-Host ">>> 完成。MySQL/Redis/Nacos 密码见 deploy\env\server.env"
