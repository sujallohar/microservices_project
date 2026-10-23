from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from app.models import WorkOrderPriority, WorkOrderStatus, GrievanceStatus

class CreateWorkOrderSchema(BaseModel):
    asset_id: str
    asset_code: str
    title: str
    description: Optional[str] = None
    priority: WorkOrderPriority = WorkOrderPriority.MEDIUM
    estimated_cost: float = 0.0
    assigned_technician: Optional[str] = "REGIONAL_CREW"

class UpdateWorkOrderStatusSchema(BaseModel):
    status: WorkOrderStatus
    actual_cost: Optional[float] = None

class CreateInspectionSchema(BaseModel):
    asset_id: str
    asset_code: str
    inspector_name: str
    condition_score: int = Field(..., ge=0, le=100)
    structural_integrity: Optional[str] = "NORMAL"
    wear_and_tear: Optional[float] = 0.0
    defects_observed: Optional[str] = None
    inspection_notes: Optional[str] = None

class CreateGrievanceSchema(BaseModel):
    citizen_name: str
    contact_number: Optional[str] = None
    asset_code: str
    description: str
    location_details: Optional[str] = None

class WorkOrderResponse(BaseModel):
    id: str
    work_order_number: str
    asset_id: str
    asset_code: str
    title: str
    description: Optional[str]
    priority: str
    status: str
    estimated_cost: float
    actual_cost: float
    assigned_technician: str
    created_at: datetime
    completed_at: Optional[datetime]

    class Config:
        from_attributes = True

class InspectionResponse(BaseModel):
    id: str
    asset_code: str
    inspector_name: str
    condition_score: int
    structural_integrity: str
    inspection_notes: Optional[str]
    recommended_action: str
    inspection_date: datetime

    class Config:
        from_attributes = True
