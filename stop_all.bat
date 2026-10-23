@echo off
title Stop GovAsset Nexus Cluster
echo Stopping all running GovAsset Nexus microservices...

taskkill /F /IM java.exe 2>nul
taskkill /F /IM node.exe 2>nul
taskkill /F /IM uvicorn.exe 2>nul

echo Stopping Docker infrastructure containers...
docker compose -f docker-compose.infra.yml stop

echo All GovAsset Nexus services stopped.
pause
