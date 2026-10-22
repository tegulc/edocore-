# Build Docker sequentiel (evite 3 telechargements Maven en parallèle).
# Usage : .\scripts\docker-build.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

$env:DOCKER_BUILDKIT = "1"
$env:COMPOSE_PARALLEL_LIMIT = "1"

Write-Host "=== Demarrage infra (postgres, redis, minio) ===" -ForegroundColor Cyan
docker compose up -d postgres redis minio

Write-Host "=== Build backend (peut prendre 15-30 min) ===" -ForegroundColor Cyan
docker compose build backend
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "=== Build notifications ===" -ForegroundColor Cyan
docker compose build notifications
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "=== Build ia ===" -ForegroundColor Cyan
docker compose build ia
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "=== Demarrage de toute la stack ===" -ForegroundColor Cyan
docker compose up -d

Write-Host ""
Write-Host "Stack demarree :" -ForegroundColor Green
Write-Host "  Frontend  -> http://localhost:5173"
Write-Host "  Backend   -> http://localhost:8080/swagger-ui.html"
Write-Host "  MinIO     -> http://localhost:9001 (educore / educore123)"
