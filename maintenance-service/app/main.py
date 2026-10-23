from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_fastapi_instrumentator import Instrumentator
from app.config import settings
from app.database import engine, Base
from app.routers.maintenance_router import router
from app.services.rabbitmq_consumer import start_background_consumer

# Create tables in MySQL if not present
try:
    Base.metadata.create_all(bind=engine)
    print("[MaintenanceService] MySQL tables verified/created successfully.")
except Exception as e:
    print(f"[MaintenanceService] Notice: Could not initialize MySQL tables immediately: {e}")

app = FastAPI(
    title="Government Asset Management - Maintenance & Lifecycle Service",
    description="Python FastAPI Microservice managing Work Orders, Field Inspections, Risk Triaging, and Saga Choreography with MySQL.",
    version="1.0.0"
)

# Prometheus Metrics Instrumentation
Instrumentator().instrument(app).expose(app)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)

@app.on_event("startup")
def startup_event():
    print("[MaintenanceService] Application starting up...")
    start_background_consumer()

@app.get("/")
def root():
    return {
        "service": "maintenance-service",
        "version": "1.0.0",
        "database": "MySQL 8.0",
        "docs": "/docs",
        "metrics": "/metrics"
    }
