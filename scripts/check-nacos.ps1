# 检查 Nacos（默认 current.env / local.env 中的 NACOS_SERVER_ADDR，未设置则用 47.120.50.85:8848）
param(
    [string]$Addr = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

function Read-NacosAddrFromEnv($path) {
    if (-not (Test-Path $path)) { return $null }
    foreach ($line in Get-Content $path) {
        if ($line -match '^\s*NACOS_SERVER_ADDR=(.+)$') { return $Matches[1].Trim() }
    }
    return $null
}

if (-not $Addr) {
    $Addr = Read-NacosAddrFromEnv (Join-Path $root "deploy\env\current.env")
    if (-not $Addr) { $Addr = Read-NacosAddrFromEnv (Join-Path $root "deploy\env\local.env") }
}
if (-not $Addr) { $Addr = "47.120.50.85:8848" }

$base = if ($Addr -match '^https?://') { $Addr } else { "http://$Addr" }
Write-Host "探测 $base ..."

try {
    $health = Invoke-RestMethod -Uri "$base/nacos/v1/console/health/readiness" -TimeoutSec 15
    Write-Host "readiness: $health"
} catch {
    Write-Warning "无法访问 Nacos: $($_.Exception.Message)"
    exit 1
}

$user = "nacos"
$pass = "nacos"
$envFile = Join-Path $root "deploy\env\current.env"
if (-not (Test-Path $envFile)) { $envFile = Join-Path $root "deploy\env\local.env" }
if (Test-Path $envFile) {
    foreach ($line in Get-Content $envFile) {
        if ($line -match '^\s*NACOS_USERNAME=(.+)$') { $user = $Matches[1].Trim() }
        if ($line -match '^\s*NACOS_PASSWORD=(.+)$') { $pass = $Matches[1].Trim() }
    }
}

try {
    $login = Invoke-RestMethod -Method Post -Uri "$base/nacos/v1/auth/login" -Body @{ username = $user; password = $pass } -TimeoutSec 15
    if ($login.accessToken) {
        Write-Host "登录成功（用户 $user）"
        $token = $login.accessToken
        $list = Invoke-RestMethod -Uri "$base/nacos/v1/cs/configs?search=blur&dataId=&group=&pageNo=1&pageSize=30&accessToken=$token" -TimeoutSec 15
        if ($list.pageItems) {
            Write-Host "已发布配置:"
            $list.pageItems | ForEach-Object { Write-Host "  - $($_.dataId)" }
        } else {
            Write-Warning "无配置项，请运行 .\scripts\publish-nacos-configs.ps1"
        }
    }
} catch {
    Write-Warning "Nacos 登录失败: $($_.Exception.Message)"
    exit 1
}
