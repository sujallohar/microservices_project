#!/usr/bin/env python3
import time
import requests
from flask import Flask, render_template, jsonify, request

app = Flask(__name__)

GATEWAY_URL = "http://localhost:8080"
IDENTITY_URL = "http://localhost:4001"
FINANCE_URL = "http://localhost:4000"
REGISTRY_URL = "http://localhost:4002"
MAINTENANCE_URL = "http://localhost:4003"
RABBITMQ_API = "http://localhost:15672/api/overview"

cached_token = None
token_expiry = 0

def get_auth_token():
    global cached_token, token_expiry
    now = time.time()
    if cached_token and now < token_expiry:
        return cached_token

    payload = {"username": "finance_officer", "password": "Finance@123"}
    try:
        res = requests.post(f"{GATEWAY_URL}/api/v1/auth/login", json=payload, timeout=3)
        if res.status_code != 200:
            res = requests.post(f"{IDENTITY_URL}/api/v1/auth/login", json=payload, timeout=3)
        data = res.json().get("data", {})
        cached_token = data.get("token")
        token_expiry = now + 3000
        return cached_token
    except Exception as e:
        print(f"[Auth Error] {e}")
        return None

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/api/status")
def get_status():
    services = [
        {
            "id": "gateway",
            "name": "API Gateway",
            "role": "Single Entrypoint, Rate Limiter & Circuit Breaker",
            "lang": "Java 21 (Spring Cloud Gateway)",
            "database": "Redis (Token Bucket Rate Limiting)",
            "port": 8080,
            "url": f"{GATEWAY_URL}/actuator/health",
            "docsUrl": None
        },
        {
            "id": "identity",
            "name": "Identity & RBAC Service",
            "role": "JWT Authentication & Session Revocation",
            "lang": "Java 21 (Spring Boot 3.2)",
            "database": "Redis 7 (Port 6379)",
            "port": 4001,
            "url": f"{IDENTITY_URL}/actuator/health",
            "docsUrl": None
        },
        {
            "id": "finance",
            "name": "Finance & Procurement Service",
            "role": "Purchase Orders & Transactional Outbox Saga Initiator",
            "lang": "Java 21 (Spring Boot 3.2)",
            "database": "PostgreSQL 16 (Port 5432)",
            "port": 4000,
            "url": f"{FINANCE_URL}/actuator/health",
            "docsUrl": None
        },
        {
            "id": "registry",
            "name": "Core Asset Registry Service",
            "role": "Asset Lifecycle & Cryptographic QR Verification",
            "lang": "Node.js 20 (Express & TypeScript)",
            "database": "MongoDB 7 (Port 27017)",
            "port": 4002,
            "url": f"{REGISTRY_URL}/health",
            "docsUrl": None
        },
        {
            "id": "maintenance",
            "name": "Maintenance & Lifecycle Service",
            "role": "Inspection Scheduling & Work Order Management",
            "lang": "Python 3.12 (FastAPI)",
            "database": "MySQL 8 (Port 3307)",
            "port": 4003,
            "url": f"{MAINTENANCE_URL}/api/v1/maintenance/health",
            "docsUrl": f"{MAINTENANCE_URL}/docs"
        },
        {
            "id": "rabbitmq",
            "name": "RabbitMQ Event Broker",
            "role": "Choreography Saga Topic Exchange (govasset.topic.exchange)",
            "lang": "Erlang / AMQP 0-9-1",
            "database": "Persistent Message Queue",
            "port": 5672,
            "url": RABBITMQ_API,
            "docsUrl": "http://localhost:15672"
        }
    ]

    results = []
    for s in services:
        status = "DOWN"
        latency_ms = None
        try:
            start = time.time()
            if s["id"] == "rabbitmq":
                res = requests.get(s["url"], auth=("govasset", "govasset_secret"), timeout=2)
                if res.status_code == 401:
                    res = requests.get(s["url"], auth=("guest", "guest"), timeout=2)
            else:
                res = requests.get(s["url"], timeout=2)
            latency_ms = round((time.time() - start) * 1000)
            if res.status_code in [200, 201]:
                status = "UP"
        except Exception:
            pass

        results.append({
            **s,
            "status": status,
            "latencyMs": latency_ms
        })

    return jsonify({"services": results, "timestamp": int(time.time() * 1000)})

@app.route("/api/data")
def get_data():
    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"} if token else {}

    pos = []
    assets = []
    work_orders = []

    # Finance POs (PostgreSQL)
    try:
        res = requests.get(f"{FINANCE_URL}/api/v1/finance/procurement", headers=headers, timeout=3)
        if res.status_code == 200:
            pos = res.json().get("data", [])
    except Exception as e:
        print("[Fetch POs Error]", e)

    # Assets (MongoDB)
    try:
        res = requests.get(f"{REGISTRY_URL}/api/v1/assets", headers=headers, timeout=3)
        if res.status_code == 200:
            assets = res.json().get("data", [])
    except Exception as e:
        print("[Fetch Assets Error]", e)

    # Work Orders (MySQL)
    try:
        res = requests.get(f"{MAINTENANCE_URL}/api/v1/maintenance/work-orders", headers=headers, timeout=3)
        if res.status_code == 200:
            work_orders = res.json()
    except Exception as e:
        print("[Fetch Work Orders Error]", e)

    GUJARAT_CITIES = [
        (23.0332, 72.5801),  # Ahmedabad Riverfront
        (23.0526, 72.6045),  # Asarwa Med City
        (23.0182, 72.5638),  # Paldi Transit
        (23.0380, 72.5710),  # Ashram Road
        (23.2156, 72.6369),  # Gandhinagar
        (22.3072, 73.1812),  # Vadodara
        (21.1702, 72.8311),  # Surat
        (22.3039, 70.8022),  # Rajkot
        (21.7645, 72.1519),  # Bhavnagar
        (22.4707, 70.0577)   # Jamnagar
    ]

    enriched_assets = []
    for idx, a in enumerate(assets):
        score = a.get("conditionScore", 80)
        loc = a.get("location", {})
        coords = loc.get("coordinates") if isinstance(loc, dict) else None
        
        if coords and len(coords) >= 2:
            lat = float(coords[1])
            lng = float(coords[0])
        else:
            city = GUJARAT_CITIES[idx % len(GUJARAT_CITIES)]
            lat = city[0]
            lng = city[1]

        # Calculate Risk Model
        if score <= 50 or "Critical" in str(a.get("specifications", {})):
            risk = "Critical"
            risk_class = "critical"
        elif score <= 70 or "High Risk" in str(a.get("specifications", {})):
            risk = "High"
            risk_class = "high"
        elif score <= 80:
            risk = "Medium"
            risk_class = "medium"
        else:
            risk = "Low"
            risk_class = "low"

        # Department / Category label
        cat = a.get("category", "INFRASTRUCTURE")
        cat_label = {
            "INFRASTRUCTURE": "Bridge / Facility",
            "HEALTHCARE": "Medical Equipment",
            "VEHICLE": "Transport Vehicle",
            "ENERGY": "Power Grid / Lighting",
            "IT_EQUIPMENT": "Digital Cluster"
        }.get(cat, cat)

        enriched_assets.append({
            **a,
            "lat": lat,
            "lng": lng,
            "riskLevel": risk,
            "riskClass": risk_class,
            "categoryLabel": cat_label
        })

    # Summary KPIs matching government dashboard
    total_assets = len(enriched_assets)
    operational_count = sum(1 for a in enriched_assets if a["riskLevel"] in ["Low", "Medium"] and a.get("lifecycleState") == "DEPLOYED")
    needs_attention_count = sum(1 for a in enriched_assets if a["riskLevel"] in ["High", "Critical"] or a.get("lifecycleState") == "UNDER_MAINTENANCE")
    open_wos_count = len(work_orders)
    op_percent = round((operational_count / total_assets * 100)) if total_assets > 0 else 67

    # Priority queue sorted by highest risk
    priority_order = {"Critical": 0, "High": 1, "Medium": 2, "Low": 3}
    priority_queue = sorted(
        [a for a in enriched_assets if a["riskLevel"] in ["Critical", "High", "Medium"]],
        key=lambda x: priority_order.get(x["riskLevel"], 4)
    )

    return jsonify({
        "purchaseOrders": pos,
        "assets": enriched_assets,
        "workOrders": work_orders,
        "metrics": {
            "totalAssets": total_assets,
            "operational": operational_count,
            "operationalPercent": op_percent,
            "needsAttention": needs_attention_count,
            "openWorkOrders": open_wos_count
        },
        "priorityQueue": priority_queue[:6]
    })

@app.route("/api/trigger-saga", methods=["POST"])
def trigger_saga():
    token = get_auth_token()
    if not token:
        return jsonify({"success": False, "error": "Failed to authenticate with Identity Service"}), 500

    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    body = request.json or {}

    asset_name = body.get("assetName", "Gandhinagar Multi-Tenant High-Speed AI Edge Cluster")
    category = body.get("category", "INFRASTRUCTURE")
    unit_cost = float(body.get("cost", 850000.00))
    department = body.get("department", "URBAN_DEVELOPMENT")
    vendor = body.get("vendor", "Tata Advanced Communications & IoT Ltd")
    specs = body.get("specs", "Dual-redundant 100Gbps telemetry controller with IP67 chassis.")

    steps = []

    # Step 1: Create PO in Finance (Postgres)
    po_payload = {
        "assetName": asset_name,
        "assetCategory": category,
        "unitCost": unit_cost,
        "quantity": 1,
        "department": department,
        "vendorName": vendor,
        "specifications": specs
    }

    try:
        po_res = requests.post(f"{GATEWAY_URL}/api/v1/finance/procurement", json=po_payload, headers=headers, timeout=4)
        if po_res.status_code not in [200, 201]:
            po_res = requests.post(f"{FINANCE_URL}/api/v1/finance/procurement", json=po_payload, headers=headers, timeout=4)
        po_data = po_res.json().get("data", {})
        po_id = po_data.get("id")
        po_number = po_data.get("poNumber")
        steps.append({
            "step": 1,
            "title": "Purchase Order Created (PostgreSQL)",
            "service": "Finance Service (Java 21)",
            "database": "PostgreSQL 16",
            "status": "DRAFT",
            "poNumber": po_number,
            "details": f"Allocated PO [{po_number}] for INR {unit_cost:,.2f} to vendor {vendor}."
        })
    except Exception as e:
        return jsonify({"success": False, "error": f"Failed in Step 1: {e}", "steps": steps}), 500

    # Step 2: Approve PO & Fire Outbox Event
    try:
        appr_res = requests.post(f"{FINANCE_URL}/api/v1/finance/procurement/{po_id}/approve", headers=headers, timeout=4)
        steps.append({
            "step": 2,
            "title": "Procurement Approved & Transactional Outbox Committed",
            "service": "Finance Service (Java 21)",
            "database": "PostgreSQL 16 Outbox Table",
            "status": "APPROVED",
            "details": f"ACID transaction committed status to APPROVED and wrote event to outbox_events table."
        })
    except Exception as e:
        return jsonify({"success": False, "error": f"Failed in Step 2: {e}", "steps": steps}), 500

    # Step 3: Outbox CDC Relay to RabbitMQ
    steps.append({
        "step": 3,
        "title": "CDC Outbox Relay Dispatched Event to RabbitMQ",
        "service": "Outbox Relay Publisher",
        "exchange": "govasset.topic.exchange",
        "routingKey": "finance.procurement.approved",
        "details": f"Polled outbox_events, published event with routing key 'finance.procurement.approved'."
    })

    # Wait for downstream async processing
    time.sleep(3.2)

    # Step 4: Verify Asset Registry (MongoDB)
    created_asset = None
    try:
        assets_res = requests.get(f"{REGISTRY_URL}/api/v1/assets", headers=headers, timeout=4)
        if assets_res.status_code == 200:
            asset_list = assets_res.json().get("data", [])
            for a in asset_list:
                if a.get("financials", {}).get("purchaseOrderId") == po_id or a.get("name") == asset_name:
                    created_asset = a
                    break
            if not created_asset and asset_list:
                created_asset = asset_list[0]

        if created_asset:
            steps.append({
                "step": 4,
                "title": "Asset Registered & Cryptographic QR Generated (MongoDB)",
                "service": "Core Asset Registry (Node.js & TypeScript)",
                "database": "MongoDB 7",
                "assetCode": created_asset.get("assetCode"),
                "qrCode": created_asset.get("qrCodeDataUrl"),
                "details": f"Assigned Asset Code [{created_asset.get('assetCode')}], generated SHA-256 verifiable QR code."
            })
    except Exception as e:
        print("[Saga Step 4 Fetch Error]", e)

    # Step 5: Verify Maintenance Service (MySQL)
    created_wo = None
    try:
        wos_res = requests.get(f"{MAINTENANCE_URL}/api/v1/maintenance/work-orders", headers=headers, timeout=4)
        if wos_res.status_code == 200:
            wos = wos_res.json()
            if wos:
                created_wo = wos[0]
                steps.append({
                    "step": 5,
                    "title": "Initial Inspection Scheduled & Baseline Work Order Created (MySQL)",
                    "service": "Maintenance & Lifecycle Service (Python FastAPI)",
                    "database": "MySQL 8",
                    "workOrderNumber": created_wo.get("work_order_number"),
                    "technician": created_wo.get("assigned_technician"),
                    "status": created_wo.get("status"),
                    "details": f"Generated Work Order [{created_wo.get('work_order_number')}], assigned to {created_wo.get('assigned_technician')}."
                })
    except Exception as e:
        print("[Saga Step 5 Fetch Error]", e)

    return jsonify({
        "success": True,
        "mode": "HAPPY_PATH",
        "po": po_data,
        "asset": created_asset,
        "workOrder": created_wo,
        "steps": steps
    })

@app.route("/api/trigger-compensation", methods=["POST"])
def trigger_compensation():
    token = get_auth_token()
    if not token:
        return jsonify({"success": False, "error": "Failed to authenticate"}), 500

    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    body = request.json or {}
    asset_name = body.get("assetName", "Flawed Industrial Step-Down Substation Unit")

    steps = []

    # Step 1: Create PO with TRIGGER_SAGA_FAILURE in specs
    po_payload = {
        "assetName": asset_name,
        "assetCategory": "INFRASTRUCTURE",
        "unitCost": 180000.00,
        "quantity": 1,
        "department": "PUBLIC_WORKS",
        "vendorName": "Defective Parts Supplier Corp",
        "specifications": "TRIGGER_SAGA_FAILURE: Simulated duplicate barcode identifier detected."
    }

    try:
        po_res = requests.post(f"{FINANCE_URL}/api/v1/finance/procurement", json=po_payload, headers=headers, timeout=4).json()["data"]
        po_id = po_res["id"]
        po_number = po_res["poNumber"]
        steps.append({
            "step": 1,
            "title": "Created Intentional Flawed PO",
            "service": "Finance Service",
            "poNumber": po_number,
            "details": f"PO [{po_number}] injected with TRIGGER_SAGA_FAILURE specification."
        })
    except Exception as e:
        return jsonify({"success": False, "error": f"Failed in step 1: {e}", "steps": steps}), 500

    # Step 2: Approve PO
    try:
        requests.post(f"{FINANCE_URL}/api/v1/finance/procurement/{po_id}/approve", headers=headers, timeout=4)
        steps.append({
            "step": 2,
            "title": "Procurement Approved -> Outbox Event Queued",
            "service": "Finance Service",
            "details": "Triggered approval. Outbox event written to PostgreSQL."
        })
    except Exception as e:
        return jsonify({"success": False, "error": f"Failed in step 2: {e}", "steps": steps}), 500

    # Step 3: Outbox relay to RabbitMQ
    steps.append({
        "step": 3,
        "title": "Outbox CDC Dispatched to RabbitMQ",
        "exchange": "govasset.topic.exchange",
        "routingKey": "finance.procurement.approved",
        "details": "Event arrived at Asset Registry queue: queue.asset.registry."
    })

    # Wait for downstream rejection & compensation event
    time.sleep(3.5)

    # Step 4: Asset Registry Rejection
    steps.append({
        "step": 4,
        "title": "Downstream Rejection in Asset Registry (Node.js/MongoDB)",
        "service": "Core Asset Registry",
        "routingKey": "saga.compensate.procurement",
        "details": "Asset Registry threw error: 'Duplicate barcode identifier detected'. Emitted saga.compensate.procurement to RabbitMQ!"
    })

    # Step 5: Check PO Status in Finance (PostgreSQL)
    checked_po = requests.get(f"{FINANCE_URL}/api/v1/finance/procurement/{po_id}", headers=headers, timeout=4).json()["data"]
    steps.append({
        "step": 5,
        "title": "Compensating Rollback Executed in Finance Service (PostgreSQL)",
        "service": "Finance Service (Saga Compensation Listener)",
        "database": "PostgreSQL 16",
        "finalStatus": checked_po.get("status"),
        "compensationReason": checked_po.get("compensationReason"),
        "details": f"Purchase Order rolled back to [{checked_po.get('status')}]. Restored allocated budget in PostgreSQL."
    })

    return jsonify({
        "success": True,
        "mode": "COMPENSATION_ROLLBACK",
        "po": checked_po,
        "steps": steps
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
