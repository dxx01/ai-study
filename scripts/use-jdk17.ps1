# 当前终端会话使用 JDK 17（Maven 构建要求 Java 17+）
$jdk = "C:\Program Files\Java\jdk-17"
if (-not (Test-Path $jdk)) {
    Write-Error "未找到 $jdk，请安装 JDK 17 或修改本脚本路径。"
}
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;" + $env:Path
java -version
