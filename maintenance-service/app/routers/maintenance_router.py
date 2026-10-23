from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import MaintenanceWorkOrder, Inspection, CitizenGrievance, WorkOrderStatus
from app.schemas import (
    CreateWorkOrderSchema,
    UpdateWorkOrderStatusSchema,
    CreateInspectionSchema,
    CreateGrievanceSchema,
    WorkOrderResponse,
    InspectionResponse
)
from app.services.maintenance_service import MaintenanceService

router = APIRouter(prefix="/api/v1/maintenance", tags=["Maintenance & Lifecycle"])

@router.get("/work-orders", response_model=List[WorkOrderResponse])
def get_work_orders(
    status: Optional[WorkOrderStatus] = None,
    asset_code: Optional[str] = None,
    db: Session = Depends(get_db)
):
    query = db.query(MaintenanceWorkOrder)
    if status:
        query = query.filter(MaintenanceWorkOrder.status == status)
    if asset_code:
        query = query.filter(MaintenanceWorkOrder.asset_code == asset_code)
    return query.order_by(MaintenanceWorkOrder.created_at.desc()).all()

@router.post("/work-orders", response_model=WorkOrderResponse)
def create_work_order(data: CreateWorkOrderSchema, db: Session = Depends(get_db)):
    try:
        return MaintenanceService.create_work_order(db, data)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.put("/work-orders/{wo_id}/status", response_model=WorkOrderResponse)
def update_work_order_status(
    wo_id: str,
    data: UpdateWorkOrderStatusSchema,
    db: Session = Depends(get_db)
):
    updated = MaintenanceService.update_work_order_status(db, wo_id, data.status, data.actual_cost)
    if not updated:
        raise HTTPException(status_code=404, detail="Work order not found")
    return updated

@router.post("/inspections", response_model=InspectionResponse)
def log_inspection(data: CreateInspectionSchema, db: Session = Depends(get_db)):
    try:
        return MaintenanceService.log_inspection(db, data)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/inspections/{asset_code}", response_model=List[InspectionResponse])
def get_inspections(asset_code: str, db: Session = Depends(get_db)):
    return db.query(Inspection).filter(Inspection.asset_code == asset_code).order_by(Inspection.inspection_date.desc()).all()

@router.post("/citizen-reports")
def create_citizen_grievance(data: CreateGrievanceSchema, db: Session = Depends(get_db)):
    try:
        grv = MaintenanceService.create_citizen_grievance(db, data)
        return {
            "success": True,
            "message": "Grievance ticket created successfully",
            "ticketNumber": grv.ticket_number,
            "status": grv.status
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/analytics/triage")
def get_triage_queue(db: Session = Depends(get_db)):
    return {
        "success": True,
        "triageQueue": MaintenanceService.get_high_risk_triage(db)
    }

@router.get("/health")
def health():
    return {"status": "UP", "service": "maintenance-service", "database": "MySQL"}
