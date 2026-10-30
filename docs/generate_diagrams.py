import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches

def generate_diagrams(out_dir):
    os.makedirs(out_dir, exist_ok=True)
    
    # -------------------------------------------------------------------------
    # 1. Architecture Diagram
    # -------------------------------------------------------------------------
    fig, ax = plt.subplots(figsize=(11, 7), dpi=300)
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 8.5)
    ax.axis('off')

    # Color Palette
    c_client = "#E0F2FE"       # Light blue
    c_ingress = "#FEF3C7"      # Light amber
    c_backend = "#EDE9FE"      # Light purple
    c_data = "#DCFCE7"         # Light green
    c_db = "#FEE2E2"           # Light red
    c_erp = "#F3F4F6"          # Light gray
    c_border = "#1E293B"

    # Helper to draw box
    def draw_layer(y, height, title, subtitle, items, bg_color):
        rect = patches.FancyBboxPatch((0.5, y), 10.0, height, boxstyle="round,pad=0.1,rounding_size=0.15",
                                      facecolor=bg_color, edgecolor=c_border, linewidth=1.5)
        ax.add_patch(rect)
        ax.text(0.7, y + height - 0.35, title, fontsize=12, fontweight='bold', color="#0F172A")
        ax.text(4.0, y + height - 0.35, subtitle, fontsize=9.5, fontstyle='italic', color="#475569")
        
        # Sub-boxes
        n = len(items)
        w = (9.6 - (n - 1) * 0.2) / n
        for i, (item_title, item_desc) in enumerate(items):
            bx = 0.7 + i * (w + 0.2)
            by = y + 0.2
            b_rect = patches.FancyBboxPatch((bx, by), w, height - 0.7, boxstyle="round,pad=0.08,rounding_size=0.1",
                                            facecolor="#FFFFFF", edgecolor="#94A3B8", linewidth=1.0)
            ax.add_patch(b_rect)
            ax.text(bx + w/2, by + (height - 0.7)*0.65, item_title, fontsize=10, fontweight='bold', ha='center', color="#1E293B")
            ax.text(bx + w/2, by + (height - 0.7)*0.3, item_desc, fontsize=8, ha='center', color="#64748B")

    # Layer 1: Client Tier
    draw_layer(6.9, 1.4, "Presentation Layer (Client Tier)", "Single Page Application & Web Clients", [
        ("React 19 SPA", "Vite 8, React Router v7"),
        ("State & Analytics", "Context API, Recharts Library"),
        ("Barcode / RFID UI", "REST Scanner Client, Quick Lookup"),
        ("Responsive Layout", "Pure Enterprise CSS, Sidebar Navigation")
    ], c_client)

    # Arrow 1 -> 2
    ax.annotate('', xy=(5.5, 6.75), xytext=(5.5, 6.9), arrowprops=dict(arrowstyle="->", color="#0F172A", lw=1.8))

    # Layer 2: API Gateway / Ingress
    draw_layer(5.3, 1.35, "API Gateway & Ingress Layer", "Reverse Proxy, Routing & Security Perimeter", [
        ("Nginx Reverse Proxy", "Port 80/8080 proxy, SPA fallback"),
        ("Kubernetes Ingress", "Path routing (/api, /), TLS Termination"),
        ("CORS & Network Policy", "Allowed origins localhost, 127.0.0.1"),
        ("Load Balancing", "Round-robin Pod distribution")
    ], c_ingress)

    # Arrow 2 -> 3
    ax.annotate('', xy=(5.5, 5.15), xytext=(5.5, 5.3), arrowprops=dict(arrowstyle="->", color="#0F172A", lw=1.8))

    # Layer 3: Application Tier
    draw_layer(3.6, 1.45, "Application Tier (Microservice Core)", "Spring Boot 3.3.5 / Java 21 LTS", [
        ("REST Controllers", "Product, Inventory, Order, PO, User, Erp"),
        ("Spring Security", "HTTP Basic, BCrypt Hash, RBAC Matrix"),
        ("Business Domain Logic", "Order State Machine, Atomic Transfers"),
        ("Audit & Monitoring", "AuditLog Service, Actuator Health/Metrics")
    ], c_backend)

    # Arrow 3 -> 4
    ax.annotate('', xy=(5.5, 3.45), xytext=(5.5, 3.6), arrowprops=dict(arrowstyle="->", color="#0F172A", lw=1.8))

    # Layer 4: Data Access Tier
    draw_layer(2.0, 1.35, "Data Access & Object Mapping Tier", "Spring Data JPA & Hibernate 6", [
        ("Spring Data Repositories", "14 JPA Repositories, Custom Queries"),
        ("Hibernate ORM", "Entity Lifecycle, Optimistic Locking"),
        ("Connection Pooling", "HikariCP Managed Connection Pool"),
        ("Schema Validation", "Hibernate DDL Auto & Flyway ready")
    ], c_data)

    # Arrow 4 -> 5
    ax.annotate('', xy=(5.5, 1.85), xytext=(5.5, 2.0), arrowprops=dict(arrowstyle="->", color="#0F172A", lw=1.8))

    # Layer 5: Persistence & External Systems
    draw_layer(0.3, 1.45, "Persistence & Integration Layer", "Relational Database Tier & Enterprise ERP Feeds", [
        ("Production Database", "MySQL 8.0 Multi-AZ, InnoDB Engine"),
        ("Dev / Test Database", "H2 In-Memory DB, Fast Unit Testing"),
        ("Persistent Storage", "K8s PVC / AWS EBS Volumes for Data"),
        ("ERP Integration Feeds", "Bidirectional SAP/Oracle Sync (JSON/CSV)")
    ], c_db)

    plt.tight_layout()
    arch_path = os.path.join(out_dir, "architecture_diagram.png")
    plt.savefig(arch_path, dpi=300, bbox_inches='tight')
    plt.close()
    print("Saved:", arch_path)

    # -------------------------------------------------------------------------
    # 2. Entity-Relationship Diagram (ERD)
    # -------------------------------------------------------------------------
    fig, ax = plt.subplots(figsize=(13, 9), dpi=300)
    ax.set_xlim(0, 13)
    ax.set_ylim(0, 9.5)
    ax.axis('off')

    # Color codes for entity categories
    e_cat = "#E0E7FF"   # Catalog: Indigo
    e_inv = "#FEF3C7"   # Inventory: Amber
    e_ord = "#DCFCE7"   # Orders: Green
    e_sup = "#FCE7F3"   # Supplier: Pink
    e_sec = "#F3F4F6"   # Security: Gray

    def draw_entity(x, y, w, h, name, fields, color):
        # Header
        h_rect = patches.FancyBboxPatch((x, y + h - 0.45), w, 0.45, boxstyle="round,pad=0.04,rounding_size=0.08",
                                        facecolor="#1E293B", edgecolor="#1E293B", linewidth=1.2)
        ax.add_patch(h_rect)
        ax.text(x + w/2, y + h - 0.25, name, fontsize=10, fontweight='bold', color="#FFFFFF", ha='center')

        # Body
        b_rect = patches.FancyBboxPatch((x, y), w, h - 0.45, boxstyle="round,pad=0.04,rounding_size=0.08",
                                        facecolor=color, edgecolor="#64748B", linewidth=1.2)
        ax.add_patch(b_rect)
        
        for idx, f in enumerate(fields):
            fy = y + h - 0.7 - idx * 0.22
            ax.text(x + 0.1, fy, f, fontsize=7.5, fontfamily='monospace', color="#0F172A")

    # Catalog Entities
    draw_entity(0.5, 6.8, 2.6, 2.2, "CATEGORY", [
        "PK id: Long",
        "   name: String",
        "   description: String",
        "   active: boolean"
    ], e_cat)

    draw_entity(3.6, 6.4, 2.8, 2.6, "PRODUCT", [
        "PK id: Long",
        "FK category_id: Long",
        "   sku: String (UQ)",
        "   name: String",
        "   barcode: String",
        "   rfid_tag: String",
        "   price: BigDecimal",
        "   cost_price: BigDecimal",
        "   active: boolean"
    ], e_cat)

    # Inventory Entities
    draw_entity(7.0, 7.0, 2.6, 2.0, "WAREHOUSE", [
        "PK id: Long",
        "   code: String (UQ)",
        "   name: String",
        "   address: String",
        "   active: boolean"
    ], e_inv)

    draw_entity(10.0, 7.0, 2.5, 2.0, "STORE", [
        "PK id: Long",
        "   code: String (UQ)",
        "   name: String",
        "   address: String",
        "   active: boolean"
    ], e_inv)

    draw_entity(7.0, 4.0, 2.8, 2.4, "STOCK_LEVEL", [
        "PK id: Long",
        "FK product_id: Long",
        "   location: String",
        "   qty: Integer",
        "   reorder_point: Integer",
        "   safety_stock: Integer",
        "   updated_at: DateTime"
    ], e_inv)

    draw_entity(10.0, 4.0, 2.6, 2.4, "STOCK_TRANSFER", [
        "PK id: Long",
        "FK product_id: Long",
        "   transfer_number: UQ",
        "   source_location: String",
        "   dest_location: String",
        "   quantity: Integer",
        "   status: TransferStatus"
    ], e_inv)

    # Order Entities
    draw_entity(0.5, 3.8, 2.6, 2.2, "CUSTOMER", [
        "PK id: Long",
        "   name: String",
        "   email: String",
        "   phone: String",
        "   address: String"
    ], e_ord)

    draw_entity(3.6, 3.2, 2.8, 2.7, "SALES_ORDER", [
        "PK id: Long",
        "FK customer_id: Long",
        "   order_number: UQ",
        "   order_date: DateTime",
        "   status: OrderStatus",
        "   total_amount: Decimal",
        "   location: String",
        "   stock_allocated: bool"
    ], e_ord)

    draw_entity(3.6, 0.4, 2.8, 2.3, "ORDER_LINE", [
        "PK id: Long",
        "FK order_id: Long",
        "FK product_id: Long",
        "   quantity: Integer",
        "   unit_price: Decimal",
        "   subtotal: Decimal"
    ], e_ord)

    # Supplier & PO Entities
    draw_entity(7.0, 0.4, 2.7, 2.6, "SUPPLIER", [
        "PK id: Long",
        "   name: String",
        "   contact_person: String",
        "   email: String",
        "   phone: String",
        "   city: String",
        "   state: String"
    ], e_sup)

    draw_entity(10.0, 0.4, 2.6, 2.6, "PURCHASE_ORDER", [
        "PK id: Long",
        "FK supplier_id: Long",
        "   po_number: UQ",
        "   order_date: DateTime",
        "   expected_date: Date",
        "   warehouse_loc: String",
        "   status: POStatus",
        "   total_amount: Decimal"
    ], e_sup)

    draw_entity(10.0, 3.1, 2.6, 0.7, "PO_LINE", [
        "PK id, FK po, FK prod, qty, cost"
    ], e_sup)

    # Security & Audit
    draw_entity(0.5, 0.8, 2.6, 2.4, "USER", [
        "PK id: Long",
        "   username: String (UQ)",
        "   password: String(Hash)",
        "   full_name: String",
        "   email: String",
        "   role: Role (ADMIN..)",
        "   enabled: boolean"
    ], e_sec)

    # Audit Log
    draw_entity(0.5, 6.2, 2.6, 0.4, "AUDIT_LOG", [
        "id, action, entity, user, ts"
    ], e_sec)

    # Relationship connector lines
    def connect(x1, y1, x2, y2, label=""):
        ax.plot([x1, x2], [y1, y2], color="#475569", linestyle="--", linewidth=1.2)
        if label:
            mx, my = (x1 + x2)/2, (y1 + y2)/2
            ax.text(mx, my, label, fontsize=7, color="#0F172A", backgroundcolor="#FFFFFF", ha='center')

    # Category -> Product
    connect(3.1, 7.8, 3.6, 7.8, "1:N")
    # Product -> StockLevel
    connect(6.4, 7.0, 7.0, 5.5, "1:N")
    # Product -> StockTransfer
    connect(6.4, 7.5, 10.0, 5.5, "1:N")
    # Customer -> SalesOrder
    connect(3.1, 4.8, 3.6, 4.8, "1:N")
    # SalesOrder -> OrderLine
    connect(5.0, 3.2, 5.0, 2.7, "1:N")
    # Product -> OrderLine
    connect(4.0, 6.4, 4.0, 2.7, "1:N")
    # Supplier -> PurchaseOrder
    connect(9.7, 1.5, 10.0, 1.5, "1:N")
    # PurchaseOrder -> POLine
    connect(11.3, 3.0, 11.3, 3.1, "1:N")

    plt.tight_layout()
    erd_path = os.path.join(out_dir, "erd_diagram.png")
    plt.savefig(erd_path, dpi=300, bbox_inches='tight')
    plt.close()
    print("Saved:", erd_path)

if __name__ == "__main__":
    generate_diagrams(r"d:\Retail project\RIMS\docs")
