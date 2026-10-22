# Demarrage local sans build Java dans Docker (plus fiable sur connexion instable).
# Usage : .\scripts\dev-local.ps1
# Prerequis : Java 21, Maven, Node.js 20+, Docker Desktop

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

function Test-BackendRunning {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui/index.html" -UseBasicParsing -TimeoutSec 3
        return $r.StatusCode -eq 200
    } catch {
        return $false
    }
}

function Stop-BackendOnPort8080 {
    $conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        $processId = $conn.OwningProcess
        Write-Host "Arret du backend existant (PID $processId)..." -ForegroundColor Yellow
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
}

Set-Location $Root

Write-Host "=== Infra Docker (postgres, redis, minio) ===" -ForegroundColor Cyan
docker compose up -d postgres redis minio

$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/educore"
$env:DATABASE_USER = "educore"
$env:DATABASE_PASSWORD = "educore"
$env:REDIS_URL = "redis://localhost:6379"
$env:S3_ENDPOINT = "http://localhost:9000"
$env:S3_ACCESS_KEY = "educore"
$env:S3_SECRET_KEY = "educore123"
$env:JWT_SECRET = "changez-ce-secret-en-production-256-bits-minimum"
$env:NOTIFICATIONS_SERVICE_URL = "http://localhost:8081"
$env:NOTIFICATIONS_SERVICE_KEY = "changez-cette-cle-en-production"
$env:IA_SERVICE_KEY = "changez-cette-cle-en-production"

$backendDir = Join-Path $Root "backend"
Set-Location $backendDir

$jar = Get-ChildItem target\educore-backend-*.jar -ErrorAction SilentlyContinue | Select-Object -First 1
$backendRunning = Test-BackendRunning

if ($backendRunning) {
    Write-Host "=== Backend deja actif sur le port 8080, build ignore ===" -ForegroundColor Green
} else {
    if ($jar) {
        Write-Host "=== Build backend sans clean, JAR existant ===" -ForegroundColor Cyan
        $env:MAVEN_OPTS = "-Xmx384m -XX:+UseSerialGC"
        mvn package "-Dmaven.test.skip=true" -T 1
    } else {
        Write-Host "=== Build backend, premier build ===" -ForegroundColor Cyan
        Stop-BackendOnPort8080
        $env:MAVEN_OPTS = "-Xmx384m -XX:+UseSerialGC"
        mvn package "-Dmaven.test.skip=true" -T 1
    }
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    $jar = Get-ChildItem target\educore-backend-*.jar | Select-Object -First 1
    Write-Host "=== Lancement backend : $($jar.Name) ===" -ForegroundColor Cyan
    $jarPath = $jar.FullName
    Start-Process powershell -ArgumentList @(
        "-NoExit", "-Command",
        "cd '$backendDir'; " +
        "`$env:DATABASE_URL='jdbc:postgresql://localhost:5432/educore'; " +
        "`$env:DATABASE_USER='educore'; `$env:DATABASE_PASSWORD='educore'; " +
        "`$env:REDIS_URL='redis://localhost:6379'; " +
        "`$env:S3_ENDPOINT='http://localhost:9000'; " +
        "`$env:S3_ACCESS_KEY='educore'; `$env:S3_SECRET_KEY='educore123'; " +
        "`$env:JWT_SECRET='changez-ce-secret-en-production-256-bits-minimum'; " +
        "java -jar '$jarPath'"
    )
}

Write-Host "=== Lancement frontend ===" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Root\frontend'; npm install; npm run dev"

Write-Host ""
Write-Host "Pret." -ForegroundColor Green
Write-Host "  Frontend -> http://localhost:5173/inscription"
Write-Host "  Backend  -> http://localhost:8080/swagger-ui.html"
Write-Host ""
Write-Host "Pour forcer un rebuild complet, arrete d'abord le backend (Ctrl+C dans sa fenetre)," -ForegroundColor DarkGray
Write-Host "puis relance ce script, ou execute : mvn clean package -Dmaven.test.skip=true" -ForegroundColor DarkGray
