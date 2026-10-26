$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$configDir = Join-Path $root "deploy\nacos\configs"

$envFile = Join-Path $root "deploy\env\current.env"
if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $root "deploy\env\local.env"
}

$NacosAddr = "http://47.120.50.85:8848"
$Username = "nacos"
$Password = "nacos"

if (Test-Path $envFile) {
    foreach ($line in Get-Content $envFile) {
        if ($line -match '^\s*NACOS_SERVER_ADDR=(.+)$') {
            $addr = $Matches[1].Trim()
            if ($addr -notmatch '^https?://') { $addr = "http://$addr" }
            $NacosAddr = $addr
        }
        if ($line -match '^\s*NACOS_USERNAME=(.+)$') { $Username = $Matches[1].Trim() }
        if ($line -match '^\s*NACOS_PASSWORD=(.+)$') { $Password = $Matches[1].Trim() }
    }
}

Write-Host "Nacos: $NacosAddr (user $Username)"

$login = Invoke-RestMethod -Method Post -Uri ($NacosAddr + "/nacos/v1/auth/login") -Body @{
    username = $Username
    password = $Password
}
$token = $login.accessToken
if (-not $token) { throw "Nacos login failed - check deploy/env/current.env NACOS_USERNAME / NACOS_PASSWORD" }

Get-ChildItem (Join-Path $configDir "*.yaml") | ForEach-Object {
    $dataId = $_.Name
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    Write-Host ("publish " + $dataId)
    $uri = $NacosAddr + "/nacos/v1/cs/configs?accessToken=" + $token
    Invoke-RestMethod -Method Post -Uri $uri -Body @{
        dataId  = $dataId
        group   = "DEFAULT_GROUP"
        type    = "yaml"
        content = $content
    } | Out-Null
}

Write-Host "done."
