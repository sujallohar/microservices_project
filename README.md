# Government Asset Management and Lifecycle System (GovAsset Nexus)
## Polyglot Microservices Architecture Course Assignment

GovAsset Nexus is a distributed, cloud-native enterprise system for public asset governance, procurement lifecycle, condition monitoring, and field inspections. It is engineered strictly following polyglot microservice principles, database-per-service isolation, asynchronous Choreography Sagas with compensating transactions, transactional outbox pattern, resilience circuit breakers, and Kubernetes/Istio deployments.

---

## 🏛️ 1. Architecture & Polyglot Stack Overview

| Microservice | Language / Framework | Database | Protocol & Role |
| :--- | :--- | :--- | :--- |
| **Identity & RBAC Service** | **Java 21 / Spring Boot 3.2** | **Redis 7.2** | Port `4001`, gRPC `9091`. Issues JWTs, manages role-based access control (`ROLE_SUPER_ADMIN`, `ROLE_FINANCE_OFFICER`, `ROLE_ASSET_OFFICER`, `ROLE_MAINTENANCE_INSPECTOR`, `ROLE_CITIZEN`), session store, Redis token blacklist, and high-speed internal gRPC token validation. |
| **Finance & Procurement Service** | **Java 21 / Spring Boot 3.2** | **PostgreSQL 16** | Port `4000`. Manages departmental budgets, purchase orders, **Transactional Outbox table** (ACID consistency), **Resilience4j** circuit breakers, and acts as the **Saga Initiator** with compensating budget-refund logic. |
| **Core Asset Registry** | **Node.js (TypeScript) / Express** | **MongoDB 7.0** | Port `4002`, gRPC `9092`. Manages asset catalog, GPS GeoJSON locations, QR code generation, depreciation schedules, **Saga Step 2 participant**, and internal gRPC server. |
| **Maintenance & Lifecycle Service** | **Python 3.12 / FastAPI** | **MySQL 8.0** | Port `4003`. Manages work orders, physical inspections, dynamic condition scoring, citizen grievance tickets, and **Saga Step 3 participant** with compensation triggers. |

### Infrastructure & Distributed Patterns:
- **API Gateway**: **Spring Cloud Gateway** (`:8080`) handling routing, JWT extraction & validation, Redis token bucket rate limiting (100-500 req/min), and Resilience4j fallback routes.
- **Service Discovery**: Dual-mode — **Netflix Eureka** (`:8761`) for Docker Compose, and native **Kubernetes CoreDNS** for cluster deployment.
- **Synchronous Internal Validation**: High-speed **gRPC** interfaces defined in `shared/proto/` (`auth.proto`, `asset.proto`).
- **Asynchronous Message Broker**: **RabbitMQ 3.13** topic exchange (`govasset.topic.exchange`).
- **Transactional Outbox & CDC**: Finance service writes PO and Outbox events in the same PostgreSQL ACID transaction; Debezium CDC / Outbox Relay dispatches to RabbitMQ.
- **Choreography Saga with Compensation**:
  1. Finance approves purchase $\rightarrow$ Outbox publishes `finance.procurement.approved`.
  2. Asset Registry consumes $\rightarrow$ creates asset in MongoDB & generates QR $\rightarrow$ publishes `asset.registered`. *(If catalog validation fails: publishes `asset.registration.failed`)*.
  3. Maintenance consumes $\rightarrow$ provisions baseline inspection & warranty work order in MySQL $\rightarrow$ publishes `maintenance.scheduled` *(Transaction Complete)*.
  4. If downstream fails, `saga.compensate.procurement` triggers Finance to mark PO as `REJECTED_ROLLBACK` and restore budget!
- **Security**: JWT for external traffic, Istio `PeerAuthentication` (`STRICT` mTLS) for internal pod-to-pod encryption.
- **Observability**: Prometheus metrics (`:9090`) scraping all microservices + ELK/Filebeat daemonset logging stubs.

---

## 🚀 2. Quickstart: Running with Docker Compose

Ensure Docker Desktop is running on your machine, then execute:

```bash
# Navigate to the MAP assignment directory
cd "MAP assignment"

# Start the entire polyglot microservice cluster:
# (4 Databases + RabbitMQ + Eureka + Gateway + 4 Microservices + Prometheus)
docker compose up -d --build
```

### Accessing UIs & Endpoints:
- **API Gateway (Central Ingress)**: `http://localhost:8080`
- **RabbitMQ Management UI**: `http://localhost:15672` (User: `govasset`, Pass: `govasset_secret`)
- **Eureka Discovery Server**: `http://localhost:8761`
- **Prometheus Metrics Dashboard**: `http://localhost:9090`
- **Identity & RBAC Service**: `http://localhost:4001` (gRPC: `9091`)
- **Finance & Procurement Service**: `http://localhost:4000`
- **Asset Registry Service**: `http://localhost:4002` (gRPC: `9092`)
- **Maintenance & Lifecycle Service**: `http://localhost:4003`

---

## 🧪 3. Running the Automated Choreography Saga Test Suite

We have provided a comprehensive Python test suite that exercises the entire distributed flow through the API Gateway:

```bash
# Install requests if not already present
pip install requests

# Execute end-to-end Saga verification:
python test_saga_flow.py
```

### What the test verifies:
1. **Gateway Auth & JWT Token**: Logs in as `finance_officer` (`Finance@123`), validates token claims, and verifies downstream context headers (`X-User-Id`, `X-User-Roles`).
2. **ACID Transactional Outbox**: Creates and approves a Purchase Order in PostgreSQL; verifies simultaneous outbox record insertion.
3. **Saga Step 2 (MongoDB)**: Confirms that Asset Registry consumed `finance.procurement.approved`, inserted the asset into MongoDB, and created a QR code.
4. **Saga Step 3 (MySQL)**: Confirms that Maintenance Service consumed `asset.registered`, auto-provisioned an initial inspection and a commissioning work order in MySQL.
5. **Compensating Rollback Simulation**: Submits a defective purchase order; verifies downstream failure emission, receipt of `saga.compensate.procurement`, transition of PO status to `REJECTED_ROLLBACK`, and restoration of departmental budget funds.

---

## 🔑 4. Default Seed Credentials (RBAC)

Pre-seeded into Redis upon startup:

| Username | Password | Role | Department |
| :--- | :--- | :--- | :--- |
| `superadmin` | `Admin@123` | `ROLE_SUPER_ADMIN` | GENERAL_ADMIN |
| `finance_officer` | `Finance@123` | `ROLE_FINANCE_OFFICER` | FINANCE_DEPT |
| `asset_officer` | `Asset@123` | `ROLE_ASSET_OFFICER` | PUBLIC_WORKS |
| `inspector` | `Inspector@123` | `ROLE_MAINTENANCE_INSPECTOR` | URBAN_INFRASTRUCTURE |
| `auditor` | `Auditor@123` | `ROLE_AUDITOR` | CAG_AUDIT |
| `citizen` | `Citizen@123` | `ROLE_CITIZEN` | PUBLIC |

---

## ☸️ 5. Kubernetes & Istio Production Deployment

Standard Kubernetes manifests are provided under `k8s/`:

```bash
# 1. Create Namespace with Istio sidecar injection enabled
kubectl apply -f k8s/00-namespace.yaml

# 2. Apply Secrets and ConfigMaps
kubectl apply -f k8s/01-secrets-and-config.yaml

# 3. Deploy StatefulSets for all 4 databases and RabbitMQ
kubectl apply -f k8s/02-databases/

# 4. Deploy all 4 Polyglot Microservices and Spring Cloud Gateway
kubectl apply -f k8s/03-microservices/

# 5. Apply Istio STRICT mTLS & Ingress Gateway VirtualService
kubectl apply -f k8s/04-istio/

# 6. Apply Prometheus & ELK/Filebeat Observability stubs
kubectl apply -f k8s/05-observability/
```

---

## 📂 6. Repository Layout

```
MAP assignment/
├── shared/
│   └── proto/
│       ├── auth.proto                  # gRPC contract for Identity & RBAC
│       └── asset.proto                 # gRPC contract for Asset Registry
├── identity-service/                   # Java 21 Spring Boot + Redis 7 + gRPC
│   ├── src/main/java/com/govasset/identity/
│   ├── pom.xml
│   └── Dockerfile
├── finance-service/                    # Java 21 Spring Boot + PostgreSQL 16 + Outbox + Saga
│   ├── src/main/java/com/govasset/finance/
│   ├── debezium-postgres-outbox.json   # Debezium CDC configuration
│   ├── pom.xml
│   └── Dockerfile
├── asset-registry-service/             # Node.js TypeScript + MongoDB 7.0 + gRPC + Saga Step 2
│   ├── src/
│   ├── package.json
│   ├── tsconfig.json
│   └── Dockerfile
├── maintenance-service/                # Python 3.12 FastAPI + MySQL 8.0 + Saga Step 3
│   ├── app/
│   ├── requirements.txt
│   └── Dockerfile
├── api-gateway/                        # Spring Cloud Gateway (:8080) + Redis RateLimiter + CircuitBreaker
│   ├── src/main/java/com/govasset/gateway/
│   ├── pom.xml
│   └── Dockerfile
├── discovery-server/                   # Netflix Eureka Server (:8761)
│   ├── src/main/java/com/govasset/discovery/
│   ├── pom.xml
│   └── Dockerfile
├── k8s/                                # Production Kubernetes & Istio manifests
│   ├── 00-namespace.yaml
│   ├── 01-secrets-and-config.yaml
│   ├── 02-databases/                  # StatefulSets for Redis, Postgres, Mongo, MySQL, RabbitMQ
│   ├── 03-microservices/              # Deployments & Services for all microservices
│   ├── 04-istio/                      # STRICT mTLS PeerAuthentication & VirtualService
│   └── 05-observability/              # Prometheus operator config & Filebeat daemonset
├── observability/
│   └── prometheus.yml                  # Prometheus scrape config
├── docker-compose.infra.yml            # 4 Databases + RabbitMQ standalone
├── docker-compose.yml                  # Full stack compose file
├── test_saga_flow.py                   # Automated end-to-end integration & compensation test suite
└── README.md                           # Documentation
```
