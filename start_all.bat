@echo off
title GovAsset Nexus - Microservices Launcher
echo ======================================================================
echo   Starting GovAsset Nexus Polyglot Microservices Cluster...
echo ======================================================================

echo [1/6] Starting Infrastructure Containers (Redis, Postgres, Mongo, MySQL, RabbitMQ)...
docker compose -f docker-compose.infra.yml up -d

echo [2/6] Starting Identity and RBAC Service (Port 4001, gRPC 9091)...
start "Identity Service" cmd /k "cd identity-service && java -jar target\identity-service-1.0.0.jar"

echo [3/6] Starting Finance and Procurement Service (Port 4000)...
start "Finance Service" cmd /k "cd finance-service && java -jar target\finance-service-1.0.0.jar"

echo [4/6] Starting Spring Cloud API Gateway (Port 8080)...
start "API Gateway" cmd /k "cd api-gateway && java -jar target\api-gateway-1.0.0.jar"

echo [5/6] Starting Core Asset Registry Service (Port 4002, gRPC 9092)...
start "Asset Registry Service" cmd /k "cd asset-registry-service && node dist\index.js"

echo [6/6] Starting Maintenance and Lifecycle Service (Port 4003)...
start "Maintenance Service" cmd /k "cd maintenance-service && .\.venv\Scripts\uvicorn.exe app.main:app --host 0.0.0.0 --port 4003"

echo [Bonus] Starting Executive Command Center Dashboard (Port 5000)...
start "Command Center Dashboard" cmd /k "cd dashboard && python app.py"

echo ======================================================================
echo   All services launched!
echo   Open Command Center in browser: http://localhost:5000
echo   Open RabbitMQ Management UI:    http://localhost:15672 (guest/guest)
echo   Open FastAPI Swagger UI:        http://localhost:4003/docs
echo   Open API Gateway Actuator:      http://localhost:8080/actuator/health
echo ======================================================================
pause
