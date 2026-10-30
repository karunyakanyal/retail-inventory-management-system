# Retail Inventory Management System (RIMS)
**Project Code:** PRG_RIMS_001 | **Client:** XYZ Retail Corporation | **Vendor:** HCL (Agile Practice)

---

## 1. Project Overview

The **Retail Inventory Management System (RIMS)** is an enterprise-grade omnichannel inventory control, multi-location tracking, order orchestration, and automated replenishment platform developed for **XYZ Retail Corporation**. XYZ operates a distributed retail footprint consisting of **500+ physical retail stores**, **3 regional distribution warehouses**, and digital sales channels.

RIMS replaces fragmented spreadsheets and legacy batch updates with a real-time, event-driven architecture that delivers:
- **Sub-second inventory visibility** across warehouses and retail store locations.
- **Atomic stock allocations and transfers** preventing double-booking and phantom stock.
- **Automated low-stock and safety-stock threshold alerts** to trigger timely procurement.
- **Comprehensive end-to-end order lifecycle management** with returns and restocking.
- **Supplier relationship management and PO fulfillment tracking**.
- **Immutable audit logging** for all state-mutating operations.

---

## 2. Technology Stack

| Layer | Technology | Version / Details |
|---|---|---|
| **Backend Framework** | Java / Spring Boot | Java 21 LTS, Spring Boot 3.4.1 |
| **Persistence & ORM** | Spring Data JPA / Hibernate | HikariCP connection pool, ACID transaction management |
| **Databases** | H2 Database / MySQL 8.0 | H2 In-Memory (default dev/test profile), MySQL 8.0 (production profile) |
| **Security & Auth** | Spring Security | HTTP Basic Authentication, BCrypt password hashing, method-level RBAC |
| **API Documentation** | SpringDoc OpenAPI | OpenAPI 3.0, Swagger UI integration |
| **Operational Telemetry** | Spring Boot Actuator | Health probes, readiness/liveness endpoints, system metrics |
| **Frontend Framework** | React / Vite | React 19, Vite 6, React Router DOM 7 |
| **Data Fetching & State** | Axios | Custom interceptor for HTTP Basic auth, automatic 401 redirect |
| **Visual Analytics** | Recharts | Responsive SVG charts for 30-day sales trends and inventory distribution |
| **Containerization** | Docker | Multi-stage Dockerfiles (Maven + JRE 21 Alpine; Node + Nginx Alpine) |
| **Orchestration** | Kubernetes / Docker Compose | Complete production manifests (Namespace, Deployment, PVC, Service, Ingress) |
| **Continuous Integration**| Jenkins | Declarative pipeline (`Jenkinsfile`) with build, test, containerize, and deploy |

---

## 3. Directory & Folder Structure

```
RIMS/
├── backend/                                # Spring Boot 3 Backend Application
│   ├── src/main/java/com/hcl/rims/
│   │   ├── config/                         # Security & CORS configuration
│   │   ├── dto/                            # Request/Response Data Transfer Objects with Bean Validation
│   │   ├── exception/                      # Global exception handler with structured JSON error responses
│   │   ├── model/                          # 14 JPA Entities (User, Product, StockLevel, SalesOrder, etc.)
│   │   ├── repository/                     # Spring Data JPA Repositories
│   │   ├── security/                       # Custom UserDetailsService & Security filter chain
│   │   ├── seed/                           # DataSeeder (Users, 30 Products, 3 Warehouses, 5 Stores, etc.)
│   │   ├── service/                        # Domain service implementations & AuditService
│   │   └── web/                            # REST Controllers (Auth, Product, Inventory, Order, PO, etc.)
│   ├── src/main/resources/
│   │   ├── application.properties          # Default profile (H2, auto-schema, Actuator, Swagger)
│   │   └── application-mysql.properties    # Production profile (MySQL DB URL, credentials, ddl-auto=update)
│   ├── src/test/java/com/hcl/rims/
│   │   └── RimsApplicationTests.java       # 18 integration tests covering all critical domain workflows
│   ├── Dockerfile                          # Multi-stage Maven 3.9 + Temurin 21 JRE Alpine image
│   ├── pom.xml                             # Maven project dependencies
│   ├── mvnw & mvnw.cmd                     # Cross-platform Maven wrapper scripts
├── frontend/                               # React 19 + Vite Single Page Application
│   ├── src/
│   │   ├── api/                            # Axios client & centralized API service layer
│   │   ├── components/                     # Layout, Sidebar, Toast alert notifications, ProtectedRoute
│   │   ├── context/                        # AuthContext (sessionStorage authentication & role state)
│   │   ├── pages/                          # Dashboard, Products, Inventory, Transfers, Orders, POs, Reports, Users
│   │   ├── App.jsx & main.jsx              # Router definitions & app root entrypoint
│   │   └── index.css                       # Clean, modern custom stylesheet (no heavy UI framework)
│   ├── public/                             # Static assets
│   ├── Dockerfile                          # Multi-stage Node 22 build + Nginx 1.25 Alpine reverse proxy
│   ├── nginx.conf                          # Reverse proxy for /api and SPA fallback routing
│   ├── package.json                        # Dependencies: react, react-dom, react-router-dom, axios, recharts
│   ├── vite.config.js                      # Vite bundler configuration
│   └── .gitignore                          # Excludes node_modules/, dist/, logs
├── k8s/                                    # Production Kubernetes Manifests
│   ├── 00-namespace.yaml                   # Dedicated 'rims' namespace
│   ├── 01-mysql.yaml                       # MySQL Deployment, 20Gi PVC, ClusterIP Service, Secret
│   ├── 02-backend.yaml                     # Backend Deployment (3 replicas, health probes) & Service
│   ├── 03-frontend.yaml                    # Frontend Deployment (2 replicas, Nginx) & Service
│   ├── 04-ingress.yaml                     # NGINX Ingress rules routing / and /api
│   └── README.md                           # Kubernetes cluster deployment guide
├── docs/                                   # Project Documentation & Agile Deliverables
│   ├── RIMS_Agile_Workbook.xlsx            # Comprehensive 14-sheet Agile/Scrum workbook
│   ├── RIMS_Project_Report.docx            # 17-section formal technical & architectural report
│   ├── architecture_diagram.png            # Cloud-native layered architecture diagram
│   ├── erd_diagram.png                     # Complete 14-entity ER diagram
│   ├── generate_diagrams.py                # Matplotlib automated diagram generator
│   └── generate_report.py                  # Python-docx automated report compiler
├── docker-compose.yml                      # Full-stack composition (MySQL 8, backend, frontend)
├── Jenkinsfile                             # 6-stage declarative CI/CD pipeline
├── .dockerignore                           # Excludes target/, node_modules/, .git from container context
├── .env.example                            # Sample environment variables for Docker Compose
├── .gitignore                              # Excludes target/, node_modules/, dist/, .env, *.log
└── README.md                               # Project documentation entry point
```

---

## 4. Features per Functional Module

1. **Authentication & RBAC**:
   - HTTP Basic authentication over HTTPS.
   - Passwords securely hashed with BCrypt (10 rounds).
   - Role-Based Access Control enforcing `ADMIN`, `MANAGER`, `STAFF`, and `VIEWER` authorizations.
   - Inactive accounts immediately barred from system access.

2. **Product Catalog Management**:
   - Full CRUD operations on products and categories with SKU uniqueness validation.
   - Multi-criteria search and filtering by product name, category ID, and price range.
   - Server-side pagination and sorting (`Page<ProductDTO>`).
   - Bulk CSV product import with row-by-row error validation.
   - Instant catalog CSV export stream.

3. **Barcode & RFID Scanning**:
   - Universal scan endpoint `GET /api/scan/{code}` resolving both optical 1D/2D barcodes and RFID electronic tags.
   - Returns full product pricing, category metadata, and active stock levels.

4. **Multi-Location Inventory Tracking**:
   - Real-time stock levels maintained individually per Warehouse and Store location.
   - Atomic inter-location stock transfers executed in a single transaction (decrements source, increments destination, rejects if balance is insufficient).
   - Manual inventory adjustments with mandatory reason logging.
   - Low-stock and safety-stock alert endpoint (`GET /api/inventory/low-stock`) identifying items with `quantity <= reorderPoint`.

5. **Sales Orders & Omnichannel Fulfillment**:
   - Order creation with immediate, atomic stock allocation (fails with HTTP 400 if stock is insufficient).
   - Enforced status lifecycle: `NEW` $\rightarrow$ `ALLOCATED` $\rightarrow$ `PICKED` $\rightarrow$ `PACKED` $\rightarrow$ `SHIPPED`. Invalid transitions are rejected.
   - Order cancellation with automatic allocation release.
   - Customer return processing with automated inventory restocking.

6. **Procurement & Purchase Orders (PO)**:
   - Supplier catalog with contact details, status, and performance tracking.
   - Purchase order generation linked to designated regional warehouses.
   - PO receiving workflow that marks items `RECEIVED` and increments warehouse stock levels automatically.
   - Supplier metrics calculating on-time delivery rate percentage and fulfillment history.

7. **Analytics, KPIs & Executive Reporting**:
   - Real-time KPI summaries: Total inventory valuation ($), stockout count (items with 0 qty), overstock count (items exceeding $3 \times$ reorder point), and order fulfillment rate (%).
   - Sales trends grouped by calendar date for charting.
   - Instant CSV export downloads for both inventory status and order records.

8. **User Administration (Admin Only)**:
   - Dedicated administration console to create users, update profiles, reassign roles, and activate/deactivate accounts.

9. **Enterprise ERP Integration**:
   - Bi-directional JSON and CSV endpoints to export and import products, stock balances, and sales orders for external ERP synchronizations (SAP/Oracle).

10. **Immutable Audit Logging**:
    - Every state-mutating operation automatically records target entity name, entity ID, action verb, authenticated username, timestamp, and metadata payload in an append-only audit ledger.

---

## 5. Seeded Default Users

The application database seeds with four pre-configured user accounts covering all roles. All accounts share the same default password:

| Username | Password | Role | Description |
|---|---|---|---|
| **admin** | `Pass@123` | `ADMIN` | Superuser: full system access, user administration, security policies |
| **manager** | `Pass@123` | `MANAGER` | Catalog management, PO creation, ERP sync, inventory reviews |
| **staff** | `Pass@123` | `STAFF` | Stock adjustments, inventory transfers, order fulfillment, PO receiving |
| **viewer** | `Pass@123` | `VIEWER` | Read-only access: view dashboards, search catalog, inspect inventory |

---

## 6. Execution Commands (Windows PowerShell)

Ensure **Java 21 LTS** and **Node.js 18+ (Node 22 recommended)** are installed on your workstation.

### 6.1 Backend Service

Navigate to the backend directory and launch the Spring Boot service using the bundled Maven wrapper:

```powershell
# Navigate to backend directory
cd "d:\Retail project\RIMS\backend"

# Run Spring Boot backend (defaults to port 8080 on H2 in-memory DB)
.\mvnw.cmd spring-boot:run

# Optional: Run on custom port (e.g., port 8081)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### 6.2 Frontend SPA

In a separate PowerShell terminal window, navigate to the frontend directory:

```powershell
# Navigate to frontend directory
cd "d:\Retail project\RIMS\frontend"

# Install node dependencies
npm install

# Run Vite development server (defaults to port 5173 targeting http://localhost:8080)
npm run dev

# Optional: Launch frontend pointing to backend running on port 8081
$env:VITE_API_URL="http://localhost:8081"; npm run dev
```

Open your browser and navigate to **`http://localhost:5173`**.

### 6.3 Automated Integration Tests

Run the complete suite of backend integration tests (18 tests verifying auth, allocation, status flow, atomic transfers, PO receiving, and alerts):

```powershell
cd "d:\Retail project\RIMS\backend"
.\mvnw.cmd test
```

### 6.4 Production Frontend Build Verification

To verify that the React/Vite frontend builds cleanly with zero errors:

```powershell
cd "d:\Retail project\RIMS\frontend"
npm run build
```

### 6.5 Full-Stack Container Deployment (Docker Compose)

To build and spin up the multi-container stack (MySQL 8 database, Spring Boot backend, and Nginx frontend):

```powershell
cd "d:\Retail project\RIMS"
docker compose up --build
```

---

## 7. Swagger Documentation & Health Endpoints

When the backend is running, the following endpoints are available:

| Endpoint Description | URL (Default Port 8080) | URL (Custom Port 8081) |
|---|---|---|
| **Swagger UI (Interactive API Docs)** | `http://localhost:8080/swagger-ui/index.html` | `http://localhost:8081/swagger-ui/index.html` |
| **OpenAPI v3 Specification (JSON)** | `http://localhost:8080/v3/api-docs` | `http://localhost:8081/v3/api-docs` |
| **Actuator Health Probe** | `http://localhost:8080/actuator/health` | `http://localhost:8081/actuator/health` |
| **Actuator Application Info** | `http://localhost:8080/actuator/info` | `http://localhost:8081/actuator/info` |

---

## 8. Role-Based Access Control (RBAC) Permissions Matrix

| Feature / Action | API Endpoint Pattern | ADMIN | MANAGER | STAFF | VIEWER |
|---|---|:---:|:---:|:---:|:---:|
| **User Administration** | `/api/users/**` | **Full** | No Access | No Access | No Access |
| **Catalog Modification** | `POST/PUT/DELETE /api/products/**` | **Full** | **Full** | Read Only | Read Only |
| **Category Modification** | `POST/PUT/DELETE /api/categories/**` | **Full** | **Full** | Read Only | Read Only |
| **Catalog & Scan Lookup** | `GET /api/products/**`, `/api/scan/**` | **Full** | **Full** | **Full** | Read Only |
| **Inventory Adjustments** | `POST /api/inventory/adjust` | **Full** | **Full** | **Full** | Read Only |
| **Inter-Location Transfers** | `POST /api/inventory/transfer` | **Full** | **Full** | **Full** | Read Only |
| **Order Processing & Lifecycle** | `POST /api/orders/**`, `PUT status` | **Full** | **Full** | **Full** | Read Only |
| **Purchase Order Creation** | `POST /api/purchase-orders` | **Full** | **Full** | Read Only | Read Only |
| **Purchase Order Receiving** | `PUT /api/purchase-orders/{id}/receive`| **Full** | **Full** | **Full** | Read Only |
| **Executive Reports & KPIs** | `GET /api/reports/**` | **Full** | **Full** | **Full** | Read Only |
| **ERP Import / Export** | `/api/erp/**` | **Full** | **Full** | Read Only | Read Only |

---

## 9. Project Documentation Deliverables

Located in the `RIMS/docs/` directory:

1. **`RIMS_Agile_Workbook.xlsx`**:
   - Comprehensive 14-sheet enterprise Agile/Scrum workbook created via Python `openpyxl`.
   - Includes: *Project Info*, *Product Backlog* (with live WSJF formula and priority rank), *Estimation* (story points per epic), *Product Burndown* (15 sprints with line chart), *Resource Plan* (19 headcount allocation), *IT Services Plan*, *Training Plan*, *Risk Management*, *Opportunity Management*, *Defects-Comments Data*, *Impediments Log*, *Action Items*, *Story Clarity Index*, and *Metrics*.
   - Evaluated and recalculated with headless LibreOffice for cached formula values.

2. **`RIMS_Project_Report.docx`**:
   - Formal 17-section project completion and system architecture report compiled via Python `docx`.
   - Includes: Professional cover page, dynamic Table of Contents field, page numbers in footer (`Page X of Y`), 13 stylized tables with Navy header shading (`#1B365D`), and two embedded high-resolution system diagrams (`architecture_diagram.png` and `erd_diagram.png`).
   - Details SMART target achievements, onsite-offshore governance, 15-sprint schedule, complete 14-entity schema, 18/18 test results, and PCI audit trajectory (88.4% to 97.6%).

---

## 10. DevOps & Infrastructure Note

> [!NOTE]
> The containerization and cloud orchestration assets (**`backend/Dockerfile`**, **`frontend/Dockerfile`**, **`docker-compose.yml`**, **`k8s/*.yaml`**, and **`Jenkinsfile`**) are fully authored, production-ready specifications engineered for turnkey deployment in a target cloud or on-premise cluster. They are validated via declarative syntax and dry-run specifications, but are not actively executed or hosted within this local workstation development environment where Docker and Kubernetes runtime daemons are not running.
