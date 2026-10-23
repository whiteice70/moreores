# ============================================================
# Fabric 26.3 开发脚本
# 自动使用项目自带的 JDK 25 运行 Gradle，并自动探测本地代理。
#
# 用法:
#   .\dev.ps1 build        # 构建 jar
#   .\dev.ps1 runClient    # 启动开发客户端
#   .\dev.ps1 runServer    # 启动开发服务端
#   .\dev.ps1 genSources   # 反编译 Minecraft 源码（方便阅读）
#   .\dev.ps1 clean        # 清理
#
# 为什么需要代理：
#   Minecraft 的下载源（piston-meta / piston-data / libraries.minecraft.net /
#   resources.download.minecraft.net）在国内会被 TLS 重置，必须走代理。
# ============================================================

$ErrorActionPreference = 'Stop'

$root = $PSScriptRoot
$jdk  = Join-Path $root '.jdk\jdk-25'

if (-not (Test-Path (Join-Path $jdk 'bin\java.exe'))) {
    Write-Host "未找到 JDK 25: $jdk" -ForegroundColor Red
    Write-Host "请重新下载 JDK 25 并解压到该目录" -ForegroundColor Yellow
    exit 1
}

$env:JAVA_HOME = $jdk
$env:PATH      = "$jdk\bin;$env:PATH"

# ---------- 自动探测本地代理端口 ----------
function Test-LocalPort {
    param([int]$Port)
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $async  = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne(300)) { $client.Close(); return $false }
        $client.EndConnect($async)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

$proxyArgs = @()
foreach ($port in 7890, 7899, 7897, 10809, 10808, 1080, 2080) {
    if (Test-LocalPort $port) {
        Write-Host "检测到本地代理 127.0.0.1:$port，已注入 Gradle 代理参数" -ForegroundColor DarkGray
        $proxyArgs += "-Dhttp.proxyHost=127.0.0.1",  "-Dhttp.proxyPort=$port"
        $proxyArgs += "-Dhttps.proxyHost=127.0.0.1", "-Dhttps.proxyPort=$port"
        # 国内镜像直连，不走代理
        $proxyArgs += "-Dhttp.nonProxyHosts=localhost|127.0.0.1|maven.aliyun.com|mirrors.cloud.tencent.com|mirrors.huaweicloud.com"
        break
    }
}

Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor DarkGray

$allArgs = $proxyArgs + $args
& (Join-Path $root 'gradlew.bat') @allArgs
exit $LASTEXITCODE
