# GovAsset Nexus (CivicAsset) — Microservices Course Defense & Faculty Viva Guide

**Course**: Microservices Architecture and Programming (MAP)  
**Project**: Government Asset Management and Lifecycle System (GovAsset Nexus / CivicAsset)  
**Architecture Paradigm**: Polyglot Microservices, Database-per-Service, Event-Driven Choreography Saga, Transactional Outbox Pattern, gRPC Internal RPC, Spring Cloud API Gateway.

---

## 1. High-Level Architectural Blueprint

```
                                      +---------------------------------------------+
                                      |         CivicAsset Command Center           |
                                      |     (Web Dashboard on Port 5000)            |
                                      +----------------------+----------------------+
                                                             | HTTP / REST (JWT)
                                                             v
                                      +---------------------------------------------+
                                      |          Spring Cloud API Gateway           |
                                      |                 (Port 8080)                 |
                                      |  - Redis Token Bucket Rate Limiter (20/40)  |
                                      |  - Resilience4j Circuit Breakers            |
                                      |  - JWT Authentication & Header Forwarding   |
                                      +----------------------+----------------------+
                                                             |
                   +-----------------------+-----------------+-----------------------+-----------------------+
                   | (Port 4001)           | (Port 4000)                             | (Port 4002)           | (Port 4003)
                   v                       v                                         v                       v
       +-----------------------+ +-----------------------+                   +-----------------------+ +-----------------------+
       |   Identity & RBAC     | | Finance & Procurement |                   |  Core Asset Registry  | | Maintenance/Lifecycle |
       |   Java 21 (Spring)    | |   Java 21 (Spring)    |                   | Node.js 20 (TS/Express)| | Python 3.12 (FastAPI) |
       +-----------+-----------+ +-----------+-----------+                   +-----------+-----------+ +-----------+-----------+
                   |                         |                                           |                         |
            (Redis Protocol)             (JDBC)                                      (Mongoose)                (SQLAlchemy)
                   v                         v                                           v                         v
          [( Redis 7 DB )]          [( PostgreSQL 16 )]                         [( MongoDB 7 DB )]          [( MySQL 8 DB )]
             Port: 6379                Port: 5432                                  Port: 27017                Host Port: 3307
             Sessions &                Outbox Table &                              Documents &                 Work Orders &
            Token Blacklist              PO Records                                GeoJSON Assets               Inspections
                                             |
                                             | (Transactional Outbox CDC Relay)
                                             v
                           +-----------------------------------+
                           |    RabbitMQ Topic Event Broker    |
                           |   Exchange: govasset.topic.exchange|
                           |   Ports: 5672 (AMQP) / 15672 (UI) |
                           +-----------------+-----------------+
                                             |
                   +-------------------------+-------------------------+
                   |                                                   |
                   v (finance.procurement.approved)                    v (asset.registered)
       +-----------------------+                           +-----------------------+
       |  Core Asset Registry  |                           | Maintenance & Lifecycle|
       |  (Catalogs in Mongo   |                           | (Creates Work Order   |
       |   & Generates QR)     |                           |  in MySQL DB)         |
       +-----------+-----------+                           +-----------------------+
                   |
                   | (Error: Duplicate / Constraint Rejection)
                   v (saga.compensate.procurement)
       +-----------------------+
       | Finance & Procurement |
       | (Compensates in Postgres:
       |  PO -> REJECTED_ROLLBACK)
       +-----------------------+
```

---

## 2. Microservice Deep-Dive (Language & Database Justification)

### Microservice 1: Identity & RBAC Service
* **Technology**: **Java 21 / Spring Boot 3.2**
* **Database**: **Redis 7 (In-Memory Key-Value & Cache)**
* **Network Ports**: HTTP `:4001`, gRPC `:9091`
* **Why Java + Redis?**:
  * **Domain**: Authentication and authorization are the most critical path in any enterprise government architecture. Every single request passing through the API Gateway must validate user identity.
  * **Language Rationale**: Java with Spring Security provides industry-standard, battle-tested cryptographic primitives, SHA-512 JWT token signing, and compile-time type safety.
  * **Database Rationale (Redis)**: Disk-based relational databases introduce 10–20ms of read latency per authentication check. Redis stores blacklisted/revoked tokens and active sessions in RAM with sub-millisecond $O(1)$ lookups, enabling the Gateway to maintain high throughput without querying heavy disk stores.
* **Internal Architecture**:
  * `AuthController.java`: Exposes `/api/v1/auth/login`, `/api/v1/auth/validate`, `/api/v1/auth/logout`.
  * `JwtTokenProvider.java`: Generates signed HMAC-SHA512 JWTs packed with government roles (`ROLE_SUPER_ADMIN`, `ROLE_FINANCE_OFFICER`, etc.).
  * `AuthGrpcServiceImpl.java`: Implements high-speed binary gRPC interface (`auth.proto`) on port `9091` for sub-millisecond token verification without HTTP header overhead.

---

### Microservice 2: Finance & Procurement Service
* **Technology**: **Java 21 / Spring Boot 3.2**
* **Database**: **PostgreSQL 16 (Relational ACID & JSONB)**
* **Network Port**: HTTP `:4000`
* **Why Java + PostgreSQL?**:
  * **Domain**: Financial management, municipal budget allocations, purchase order contracts, and auditable accounting ledgers.
  * **Language Rationale**: Java's Spring Data JPA and `@Transactional` boundaries provide enterprise transaction orchestration with strict ACID compliance.
  * **Database Rationale (PostgreSQL)**: Financial ledgers cannot tolerate eventual consistency or lost updates. PostgreSQL enforces strict foreign key constraints, table locking, check constraints on budget balances, and ACID transactions. Additionally, its native `JSONB` support allows storing dynamic procurement specifications and outbox event payloads efficiently.
* **Key Patterns Implemented**:
  1. **Transactional Outbox Pattern**: When a Purchase Order is approved, the application writes both the updated PO state (`APPROVED`) and the corresponding event into the `outbox_events` table in the **same ACID transaction**.
  2. **Change Data Capture (CDC) Outbox Relay**: A background daemon (`OutboxRelayPublisher.java`) polls unhandled events every 1000ms, publishes them to RabbitMQ, and marks them `PROCESSED = TRUE`. This completely eliminates the Dual-Write Hazard.
  3. **Saga Compensation Consumer**: Subscribes to `queue.finance.compensations`. If downstream services fail, it receives `saga.compensate.procurement` and rolls back the PO in PostgreSQL to `REJECTED_ROLLBACK`.

---

### Microservice 3: Core Asset Registry Service
* **Technology**: **Node.js 20 / TypeScript / Express**
* **Database**: **MongoDB 7 (Document Store & GeoJSON)**
* **Network Ports**: HTTP `:4002`, gRPC `:9092`
* **Why Node.js + MongoDB?**:
  * **Domain**: Government physical asset cataloging, cryptographic QR code generation, dynamic specifications (bridges vs. medical MRI scanners vs. buses), and geographic GIS coordinates.
  * **Language Rationale**: Node.js excels at asynchronous I/O and rapid payload transformation. The `qrcode` engine produces base64 cryptographic data URLs on-the-fly with non-blocking event loops.
  * **Database Rationale (MongoDB)**: Government public assets have wildly polymorphic schemas: a bridge has girder stress ratings, an ICU ambulance has ventilator calibration dates, and a solar substation has inverter wattage. Relational tables would require massive sparse tables or complex `EAV` (Entity-Attribute-Value) anti-patterns. MongoDB's document model stores polymorphic specifications naturally, and native `2dsphere` indexes allow spatial geospatial queries for GIS map rendering.
* **Key Features**:
  * `sagaConsumer.ts`: Listens to `queue.asset.registry` for `finance.procurement.approved` events. Catalogs the asset into MongoDB, assigns a unique government asset code (e.g. `GJ-PWD-BRG-0001`), generates the cryptographic QR code, and emits `asset.registered`.
  * `AssetGrpcServiceImpl.ts`: High-speed binary gRPC server on port `9092` (`asset.proto`) allowing the Gateway or Maintenance Service to fetch asset details without HTTP REST overhead.

---

### Microservice 4: Maintenance & Lifecycle Service
* **Technology**: **Python 3.12 / FastAPI / SQLAlchemy / Pydantic**
* **Database**: **MySQL 8 (Relational ACID)**
* **Network Port**: HTTP `:4003` (Host MySQL Port `:3307`)
* **Why Python + MySQL?**:
  * **Domain**: Work order scheduling, predictive maintenance, warranty inspections, condition scoring algorithms, and technician dispatch.
  * **Language Rationale**: Python is the premier language for maintenance telemetry, predictive degradation modeling, condition score decay algorithms, and fast async REST APIs via FastAPI and Uvicorn.
  * **Database Rationale (MySQL)**: Maintenance work orders have structured, relational operational workflows (Work Order $\rightarrow$ Technician $\rightarrow$ Asset ID $\rightarrow$ Maintenance Logs). MySQL provides relational integrity and fast read queries.
* **Key Features**:
  * `saga_consumer.py`: Runs an asynchronous Pika consumer connected to RabbitMQ. When `asset.registered` is received, it automatically generates a baseline commissioning inspection work order (e.g., `WO-INIT-20261023...`) and assigns the `CHIEF_FIELD_INSPECTOR`.
  * Auto-generated Interactive Swagger / OpenAPI documentation available natively at `/docs`.

---

## 3. Inter-Service Communication & Architecture Patterns

### A. Synchronous vs. Asynchronous Communication
1. **External Ingress (Client $\rightarrow$ API Gateway)**:
   * **Protocol**: HTTP/REST over JSON with JWT Bearer Token.
   * **Spring Cloud Gateway (`:8080`)**:
     * Single Entrypoint for all client traffic.
     * **Redis Token Bucket Rate Limiting**: Enforces rate limiting per IP/User (replenish rate: 20 req/s, burst: 40).
     * **Resilience4j Circuit Breakers**: Automatically opens circuit if downstream microservice error rate exceeds 50%, returning graceful fallbacks.
2. **Internal High-Speed RPC (Microservice $\leftrightarrow$ Microservice)**:
   * **Protocol**: **gRPC (HTTP/2 + Protocol Buffers)**.
   * Defined in `shared/proto/auth.proto` and `shared/proto/asset.proto`.
   * Enables binary, strongly-typed, multiplexed RPC calls between Java, Node.js, and Python without JSON serialization penalty.
3. **Internal Event-Driven Asynchronous Choreography**:
   * **Protocol**: **AMQP 0-9-1 over RabbitMQ** (`:5672`).
   * Exchange: `govasset.topic.exchange` (Topic Exchange).
   * Decouples the microservices completely: Finance does not call Asset Registry directly, nor does Asset Registry call Maintenance directly.

---

### B. The Dual-Write Problem & The Transactional Outbox Pattern
* **The Problem**: In a distributed system, an application cannot atomically write to a database AND publish a message to a message broker in a single transaction (unless using slow, blocking distributed 2PC protocols like XA). If the database write succeeds but the broker crashes, the message is lost. If the broker accepts the message but the database transaction rolls back, downstream services process phantom data!
* **Our Solution**:
  1. The Finance service opens a **single PostgreSQL transaction**.
  2. It updates `purchase_orders` to `APPROVED`.
  3. In the **same database transaction**, it inserts a record into `outbox_events` table with the event payload.
  4. Both commit atomically.
  5. A dedicated background CDC relay (`OutboxRelayPublisher`) reads unhandled events, publishes them to RabbitMQ, and marks them `PROCESSED`.
  6. **Guarantee**: Guaranteed **At-Least-Once Delivery** without dual-write inconsistency!

---

### C. The Choreography-Based Saga Flow

#### Happy Path Flow:
$$\begin{aligned}
\text{Finance Officer Approves PO} &\longrightarrow \text{PostgreSQL commits PO + Outbox Event} \\
&\longrightarrow \text{CDC Relay polls Outbox } \xrightarrow{\text{AMQP}} \text{RabbitMQ topic: finance.procurement.approved} \\
&\longrightarrow \text{Node.js Asset Registry consumes event} \\
&\longrightarrow \text{MongoDB stores Asset Document \& creates QR code} \\
&\longrightarrow \text{Asset Registry publishes: asset.registered} \\
&\longrightarrow \text{Python Maintenance consumes event} \\
&\longrightarrow \text{MySQL stores baseline Work Order (WO-INIT-...) }
\end{aligned}$$

#### Compensating Rollback Flow:
$$\begin{aligned}
\text{Downstream Catalog Fails} &\longrightarrow \text{Node.js catches Duplicate Barcode / Constraint Exception} \\
&\longrightarrow \text{Publishes compensating event: saga.compensate.procurement} \\
&\longrightarrow \text{RabbitMQ routes to queue: queue.finance.compensations} \\
&\longrightarrow \text{Java Finance Service receives event} \\
&\longrightarrow \text{PostgreSQL rolls back PO status to: REJECTED_ROLLBACK} \\
&\longrightarrow \text{Restores Allocated Municipal Budget \& Logs Compensation Reason}
\end{aligned}$$

---

## 4. Key Questions Faculty Will Ask & Model Answers

### Q1: "Why did you use Database-per-Service instead of a single shared database?"
> **Answer**:  
> "A shared database creates tight coupling at the data layer, violating microservice autonomy. In a shared database, schema changes in one service can break other services, and all services become locked into the same database engine.  
> By implementing Database-per-Service, each service chooses the optimal persistence engine for its domain model (Polyglot Persistence): Redis for in-memory session tokens, PostgreSQL for ACID procurement ledgers, MongoDB for polymorphic GIS asset documents, and MySQL for relational work orders. Each service encapsulates its data and exposes it only via well-defined APIs or events."

### Q2: "How do you guarantee data consistency without distributed transactions (2PC)?"
> **Answer**:  
> "Distributed transactions like Two-Phase Commit (2PC) or WS-AtomicTransaction are blocking, suffer from high latency, create availability bottlenecks (CAP theorem - sacrificing Availability for Consistency), and are not supported across polyglot databases like MongoDB and Redis.  
> Instead, we implement **Eventual Consistency** using the **Saga Pattern (Choreography-based)** paired with the **Transactional Outbox Pattern**. Each microservice executes a local ACID transaction in its own database and emits an event. If a subsequent step in the business process fails, compensating transactions are published to undo the previously committed local transactions."

### Q3: "What is the Transactional Outbox Pattern and why is it necessary?"
> **Answer**:  
> "When an application needs to update a database and send a message to RabbitMQ, performing both operations in code creates a 'Dual-Write Hazard'—one can fail while the other succeeds.  
> The Transactional Outbox Pattern solves this by storing outbound events in a dedicated database table (`outbox_events`) within the exact same ACID transaction as the business entity. A separate polling relay asynchronously reads these records and dispatches them to RabbitMQ. This guarantees that an event is never published unless the database write was committed."

### Q4: "Why did you choose Choreography Saga over Orchestration Saga?"
> **Answer**:  
> "In an Orchestration Saga, a centralized orchestrator service must explicitly direct every downstream service what to do, introducing a central point of logic and coupling.  
> In our system, **Choreography Saga** allows microservices to remain completely autonomous. Services subscribe to domain events over a RabbitMQ topic exchange and react independently. The Finance service doesn't even know that a Python Maintenance service exists; it simply emits that procurement was approved, and downstream consumers react according to their own bounded contexts."

### Q5: "What is the purpose of the API Gateway in your architecture?"
> **Answer**:  
> "The Spring Cloud Gateway acts as the single entrypoint (Reverse Proxy) for all external clients, abstracting the internal microservice topology. It handles:  
> 1. **Centralized Authentication**: Validates JWT tokens so backend microservices do not need to duplicate auth logic.  
> 2. **Rate Limiting**: Uses Redis Token Bucket algorithm to protect backend services from DoS attacks.  
> 3. **Circuit Breaking**: Uses Resilience4j to fail fast and return graceful fallbacks if a service degrades, preventing cascading failures across the cluster.  
> 4. **CORS & Routing**: Decouples client applications from internal host port numbers."

### Q6: "Why do you have both REST and gRPC in this project?"
> **Answer**:  
> "We use **REST** for external client-facing APIs because JSON/HTTP is human-readable, firewall-friendly, and universally supported by web browsers and third-party consumers.  
> However, for internal synchronous microservice-to-microservice communication, REST introduces JSON serialization overhead and text parsing latency. We implemented **gRPC** (`auth.proto` and `asset.proto`) over HTTP/2 with Protocol Buffers, providing binary serialization, multiplexed connections, and strongly-typed interfaces that run significantly faster for internal RPCs."
