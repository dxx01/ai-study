# 生成本地 embedding 模型配置：模型已存在时，knowledge 启动会自动读取，无需 profile
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$dir = Join-Path $root "backend\ai-service-knowledge\data\model-cache\all-MiniLM-L6-v2"
$model = Join-Path $dir "model.onnx"
$tokenizer = Join-Path $dir "tokenizer.json"

if (-not (Test-Path $model) -or -not (Test-Path $tokenizer)) {
    throw "未找到模型文件，请先把 model.onnx 与 tokenizer.json 放到 $dir"
}

$modelUri = "file:///" + ($model -replace '\\', '/')
$tokenizerUri = "file:///" + ($tokenizer -replace '\\', '/')
$out = Join-Path $root "backend\ai-service-knowledge\data\model-cache\local-model.properties"

@(
    "spring.ai.embedding.transformer.onnx.model-uri=$modelUri",
    "spring.ai.embedding.transformer.tokenizer.uri=$tokenizerUri"
) | Set-Content $out -Encoding utf8

Write-Host "已生成本地模型配置: $out"
Write-Host "knowledge 启动时会自动读取，无需再传 profile。"
