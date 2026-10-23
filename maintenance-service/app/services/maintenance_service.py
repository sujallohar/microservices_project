import uuid
from datetime import datetime
from sqlalchemy.orm import Session
from app.models import MaintenanceWorkOrder, Inspection, CitizenGrievance, AssetRiskMetric, WorkOrderStatus, WorkOrderPriority, GrievanceStatus
from app.schemas import CreateWorkOrderSchema, CreateInspectionSchema, CreateGrievanceSchema

class MaintenanceService:

    @staticmethod
    def provision_saga_baseline(db: Session, asset_event: dict):
        """
        Saga Step 3 Handler:
        Provisions a baseline inspection record and an initial preventive work order
        in MySQL for the newly registered asset.
        """
        asset_id = asset_event.get("assetId", "")
        asset_code = asset_event.get("assetCode", "")
        asset_name = asset_event.get("name", "Asset")

        # 1. Baseline Inspection
        baseline_inspection = Inspection(
            id=str(uuid.uuid4()),
            asset_id=asset_id,
            asset_code=asset_code,
            inspector_name="SYSTEM_PROVISION_INSPECTOR",
            condition_score=100,
            structural_integrity="NEW_PROCUREMENT",
            wear_and_tear=0.0,
            defects_observed="None. Asset accepted under procurement warranty.",
            inspection_notes=f"Initial compliance baseline established for {asset_name}.",
            recommended_action="COMMISSION_FOR_OPERATIONS",
            inspection_date=datetime.utcnow()
        )
        db.add(baseline_inspection)

        # 2. Initial Commissioning Work Order
        wo_number = f"WO-INIT-{datetime.utcnow().strftime('%Y%m%d%H%M%S')}"
        initial_wo = MaintenanceWorkOrder(
            id=str(uuid.uuid4()),
            work_order_number=wo_number,
            asset_id=asset_id,
            asset_code=asset_code,
            title=f"Initial Commissioning & Warranty Inspection: {asset_name}",
            description="Perform physical mounting, calibration, and barcode verification.",
            priority=WorkOrderPriority.MEDIUM,
            status=WorkOrderStatus.REPORTED,
            estimated_cost=2500.0,
            assigned_technician="CHIEF_FIELD_INSPECTOR",
            source_event="SAGA_PROCUREMENT_PROVISION",
            created_at=datetime.utcnow()
        )
        db.add(initial_wo)

        # 3. Initialize Risk Metric
        risk_metric = AssetRiskMetric(
            id=str(uuid.uuid4()),
            asset_code=asset_code,
            risk_index=0.05,
            failure_probability=0.02,
            recommendation="NEW_ASSET_MONITORING",
            last_calculated_at=datetime.utcnow()
        )
        db.add(risk_metric)

        db.commit()
        return initial_wo

    @staticmethod
    def create_work_order(db: Session, data: CreateWorkOrderSchema):
        wo_number = f"WO-{datetime.utcnow().strftime('%Y%m%d%H%M%S')}"
        wo = MaintenanceWorkOrder(
            id=str(uuid.uuid4()),
            work_order_number=wo_number,
            asset_id=data.asset_id,
            asset_code=data.asset_code,
            title=data.title,
            description=data.description,
            priority=data.priority,
            status=WorkOrderStatus.REPORTED,
            estimated_cost=data.estimated_cost,
            assigned_technician=data.assigned_technician,
            created_at=datetime.utcnow()
        )
        db.add(wo)
        db.commit()
        db.refresh(wo)
        return wo

    @staticmethod
    def update_work_order_status(db: Session, wo_id: str, new_status: WorkOrderStatus, actual_cost: float = None):
        wo = db.query(MaintenanceWorkOrder).filter(MaintenanceWorkOrder.id == wo_id).first()
        if not wo:
            return None
        wo.status = new_status
        if actual_cost is not None:
            wo.actual_cost = actual_cost
        if new_status == WorkOrderStatus.COMPLETED:
            wo.completed_at = datetime.utcnow()
        db.commit()
        db.refresh(wo)
        return wo

    @staticmethod
    def log_inspection(db: Session, data: CreateInspectionSchema):
        # Determine recommended action based on condition score
        action = "CONTINUE_ROUTINE_MONITORING"
        if data.condition_score < 40:
            action = "EMERGENCY_DECOMMISSION_OR_REPLACE"
        elif data.condition_score < 70:
            action = "SCHEDULE_CORRECTIVE_MAINTENANCE"

        insp = Inspection(
            id=str(uuid.uuid4()),
            asset_id=data.asset_id,
            asset_code=data.asset_code,
            inspector_name=data.inspector_name,
            condition_score=data.condition_score,
            structural_integrity=data.structural_integrity,
            wear_and_tear=data.wear_and_tear,
            defects_observed=data.defects_observed,
            inspection_notes=data.inspection_notes,
            recommended_action=action,
            inspection_date=datetime.utcnow()
        )
        db.add(insp)

        # Recalculate Risk Metric
        risk_val = max(0.0, min(1.0, (100 - data.condition_score) / 100.0 * 0.85 + (data.wear_and_tear or 0.0) * 0.15))
        metric = db.query(AssetRiskMetric).filter(AssetRiskMetric.asset_code == data.asset_code).first()
        if not metric:
            metric = AssetRiskMetric(id=str(uuid.uuid4()), asset_code=data.asset_code)
            db.add(metric)
        metric.risk_index = round(risk_val, 3)
        metric.failure_probability = round(risk_val * 0.65, 3)
        metric.recommendation = action
        metric.last_calculated_at = datetime.utcnow()

        db.commit()
        db.refresh(insp)
        return insp

    @staticmethod
    def create_citizen_grievance(db: Session, data: CreateGrievanceSchema):
        ticket = f"GRV-GUJ-{datetime.utcnow().strftime('%Y%m%d%H%M')}"
        grv = CitizenGrievance(
            id=str(uuid.uuid4()),
            ticket_number=ticket,
            citizen_name=data.citizen_name,
            contact_number=data.contact_number,
            asset_code=data.asset_code,
            description=data.description,
            location_details=data.location_details,
            status=GrievanceStatus.OPEN,
            created_at=datetime.utcnow()
        )
        db.add(grv)
        db.commit()
        db.refresh(grv)
        return grv

    @staticmethod
    def get_high_risk_triage(db: Session):
        metrics = db.query(AssetRiskMetric).order_by(AssetRiskMetric.risk_index.desc()).limit(15).all()
        return [
            {
                "assetCode": m.asset_code,
                "riskIndex": m.risk_index,
                "failureProbability": m.failure_probability,
                "recommendation": m.recommendation,
                "lastCalculated": m.last_calculated_at
            }
            for m in metrics
        ]
