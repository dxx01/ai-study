# Alias for use-env.ps1 (backward compatible)
param([string]$Target = "local")

if ($PSBoundParameters.ContainsKey("Target")) {
    & (Join-Path $PSScriptRoot "use-env.ps1") -Target $Target
} else {
    & (Join-Path $PSScriptRoot "use-env.ps1") -Target "local"
}
