import enum
from datetime import datetime
from sqlalchemy import Column, String, Integer, Float, DateTime, Enum, Text
from app.database import Base

class WorkOrderPriority(str, enum.Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"

class WorkOrderStatus(str, enum.Enum):
    REPORTED = "REPORTED"
    ASSIGNED = "ASSIGNED"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"

class GrievanceStatus(str, enum.Enum):
    OPEN = "OPEN"
    TRIAGED = "TRIAGED"
    CONVERTED_TO_WORK_ORDER = "CONVERTED_TO_WORK_ORDER"
    RESOLVED = "RESOLVED"

class MaintenanceWorkOrder(Base):
    __tablename__ = "maintenance_work_orders"

    id = Column(String(64), primary_key=True)
    work_order_number = Column(String(64), unique=True, nullable=False, index=True)
    asset_id = Column(String(64), nullable=False, index=True)
    asset_code = Column(String(64), nullable=False, index=True)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    priority = Column(Enum(WorkOrderPriority), default=WorkOrderPriority.MEDIUM, nullable=False)
    status = Column(Enum(WorkOrderStatus), default=WorkOrderStatus.REPORTED, nullable=False, index=True)
    estimated_cost = Column(Float, default=0.0)
    actual_cost = Column(Float, default=0.0)
    assigned_technician = Column(String(128), default="FIELD_CREW_ALPHA")
    source_event = Column(String(128), nullable=True) # e.g. "SAGA_PROCUREMENT_PROVISION", "CITIZEN_GRIEVANCE"
    created_at = Column(DateTime, default=datetime.utcnow)
    completed_at = Column(DateTime, nullable=True)

class Inspection(Base):
    __tablename__ = "inspections"

    id = Column(String(64), primary_key=True)
    asset_id = Column(String(64), nullable=False, index=True)
    asset_code = Column(String(64), nullable=False, index=True)
    inspector_name = Column(String(128), nullable=False)
    condition_score = Column(Integer, nullable=False) # 0 to 100
    structural_integrity = Column(String(64), default="NORMAL") # NORMAL, MODERATE_WEAR, SEVERE_CRACKING
    wear_and_tear = Column(Float, default=0.0)
    defects_observed = Column(Text, nullable=True)
    inspection_notes = Column(Text, nullable=True)
    recommended_action = Column(String(128), default="CONTINUE_ROUTINE_MONITORING")
    inspection_date = Column(DateTime, default=datetime.utcnow)

class CitizenGrievance(Base):
    __tablename__ = "citizen_grievances"

    id = Column(String(64), primary_key=True)
    ticket_number = Column(String(64), unique=True, nullable=False, index=True)
    citizen_name = Column(String(128), nullable=False)
    contact_number = Column(String(32), nullable=True)
    asset_code = Column(String(64), nullable=False, index=True)
    description = Column(Text, nullable=False)
    location_details = Column(String(255), nullable=True)
    status = Column(Enum(GrievanceStatus), default=GrievanceStatus.OPEN, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

class AssetRiskMetric(Base):
    __tablename__ = "asset_risk_metrics"

    id = Column(String(64), primary_key=True)
    asset_code = Column(String(64), unique=True, nullable=False, index=True)
    risk_index = Column(Float, default=0.0) # 0.0 (safe) to 1.0 (high risk)
    failure_probability = Column(Float, default=0.0)
    recommendation = Column(String(255), default="OPTIMAL_OPERATION")
    last_calculated_at = Column(DateTime, default=datetime.utcnow)
