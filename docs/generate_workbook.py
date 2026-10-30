import os
from datetime import datetime, timedelta
import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter
from openpyxl.chart import LineChart, Reference

def create_rims_workbook():
    wb = openpyxl.Workbook()
    # Remove default sheet
    wb.remove(wb.active)

    # Styling Palettes
    navy_dark = "1B365D"       # Main Headers
    blue_accent = "2B4C7E"     # Section Headers
    blue_light = "E8EEF5"      # Summary / Highlight rows
    gray_zebra = "F8FAFC"      # Alternating rows
    gray_border = "D1D5DB"     # Borders
    white = "FFFFFF"
    green_tag = "059669"
    red_tag = "DC2626"

    font_title = Font(name="Calibri", size=15, bold=True, color=white)
    font_section = Font(name="Calibri", size=12, bold=True, color=white)
    font_header = Font(name="Calibri", size=10, bold=True, color=white)
    font_sub_header = Font(name="Calibri", size=10, bold=True, color="1E293B")
    font_bold = Font(name="Calibri", size=10, bold=True)
    font_regular = Font(name="Calibri", size=10)
    font_italic = Font(name="Calibri", size=9, italic=True, color="64748B")

    fill_title = PatternFill(start_color=navy_dark, end_color=navy_dark, fill_type="solid")
    fill_section = PatternFill(start_color=blue_accent, end_color=blue_accent, fill_type="solid")
    fill_header = PatternFill(start_color=navy_dark, end_color=navy_dark, fill_type="solid")
    fill_zebra = PatternFill(start_color=gray_zebra, end_color=gray_zebra, fill_type="solid")
    fill_highlight = PatternFill(start_color=blue_light, end_color=blue_light, fill_type="solid")

    thin_border_side = Side(style='thin', color=gray_border)
    border_all = Border(left=thin_border_side, right=thin_border_side, top=thin_border_side, bottom=thin_border_side)
    border_top_thick = Border(top=Side(style='medium', color=navy_dark), bottom=Side(style='double', color=navy_dark),
                              left=thin_border_side, right=thin_border_side)

    align_center = Alignment(horizontal="center", vertical="center")
    align_left = Alignment(horizontal="left", vertical="center")
    align_right = Alignment(horizontal="right", vertical="center")
    align_wrap_left = Alignment(horizontal="left", vertical="center", wrap_text=True)

    def style_header_row(ws, row_idx, max_col):
        ws.row_dimensions[row_idx].height = 26
        for col_idx in range(1, max_col + 1):
            cell = ws.cell(row=row_idx, column=col_idx)
            cell.font = font_header
            cell.fill = fill_header
            cell.alignment = align_center
            cell.border = border_all

    def auto_fit_columns(ws, min_width=12, max_width=45):
        for col in ws.columns:
            col_letter = get_column_letter(col[0].column)
            max_len = 0
            for cell in col:
                val = cell.value
                if val is not None:
                    # Ignore formula string lengths or long merged title lengths
                    val_str = str(val)
                    if not val_str.startswith("="):
                        lines = val_str.split("\n")
                        line_len = max(len(l) for l in lines)
                        if cell.coordinate in ws.merged_cells:
                            continue
                        max_len = max(max_len, line_len)
            ws.column_dimensions[col_letter].width = min(max(max_len + 3, min_width), max_width)

    # =========================================================================
    # 1. Sheet: Project Info
    # =========================================================================
    ws1 = wb.create_sheet(title="Project Info")
    ws1.views.sheetView[0].showGridLines = True

    # Title Banner
    ws1.merge_cells("A1:G1")
    title_cell = ws1["A1"]
    title_cell.value = "RETAIL INVENTORY MANAGEMENT SYSTEM (RIMS) - PROJECT CHARTER & AGILE SCRUM INFO"
    title_cell.font = font_title
    title_cell.fill = fill_title
    title_cell.alignment = align_center
    ws1.row_dimensions[1].height = 36

    # Metadata Section
    ws1.merge_cells("A3:G3")
    sec1 = ws1["A3"]
    sec1.value = "1. PROJECT METADATA & EXECUTIVE SUMMARY"
    sec1.font = font_section
    sec1.fill = fill_section
    sec1.alignment = align_left
    ws1.row_dimensions[3].height = 24

    metadata_rows = [
        ("Project Name", "Retail Inventory Management System (RIMS)", "Domain", "Retail & Omnichannel Supply Chain"),
        ("Project Code", "PRG_RIMS_001", "Client Organization", "XYZ Retail Corporation"),
        ("Project Manager", "Manmeet Jalota", "Delivery Framework", "Agile / Scrum"),
        ("Total Project Duration", "9 months", "Total Sprints", "15 Sprints (2-week cadence)"),
        ("Target Start Date", "02-Nov-26", "Target Release Date", "28-May-27"),
    ]

    for idx, (lbl1, val1, lbl2, val2) in enumerate(metadata_rows, start=4):
        ws1.row_dimensions[idx].height = 20
        c1 = ws1.cell(row=idx, column=1, value=lbl1)
        c2 = ws1.cell(row=idx, column=2, value=val1)
        ws1.merge_cells(start_row=idx, start_column=2, end_row=idx, end_column=3)
        c4 = ws1.cell(row=idx, column=4, value=lbl2)
        c5 = ws1.cell(row=idx, column=5, value=val2)
        ws1.merge_cells(start_row=idx, start_column=5, end_row=idx, end_column=7)

        c1.font = font_bold
        c1.fill = fill_highlight
        c1.border = border_all
        c4.font = font_bold
        c4.fill = fill_highlight
        c4.border = border_all
        for col_i in range(1, 8):
            cell = ws1.cell(row=idx, column=col_i)
            cell.border = border_all
            if col_i in [2, 3, 5, 6, 7]:
                cell.font = font_regular

    # Vision Section
    ws1.merge_cells("A10:G10")
    sec2 = ws1["A10"]
    sec2.value = "2. STRATEGIC PROJECT VISION"
    sec2.font = font_section
    sec2.fill = fill_section
    sec2.alignment = align_left
    ws1.row_dimensions[10].height = 24

    ws1.merge_cells("A11:G12")
    v_cell = ws1["A11"]
    v_cell.value = (
        "To engineer and deploy an enterprise-grade, highly resilient omnichannel retail inventory management system "
        "delivering real-time stock visibility across 3 central distribution warehouses and 5 retail stores. The platform "
        "empowers automated order allocation, dynamic stock replenishment, intelligent purchase order workflows, and sub-second "
        "ERP synchronization to eliminate stockouts and maximize customer fulfilment efficiency."
    )
    v_cell.font = font_regular
    v_cell.alignment = align_wrap_left
    for r in range(11, 13):
        for c in range(1, 8):
            ws1.cell(row=r, column=c).border = border_all

    # SMART Objectives Section
    ws1.merge_cells("A14:G14")
    sec3 = ws1["A14"]
    sec3.value = "3. SMART BUSINESS OBJECTIVES & SUCCESS CRITERIA"
    sec3.font = font_section
    sec3.fill = fill_section
    sec3.alignment = align_left
    ws1.row_dimensions[14].height = 24

    obj_headers = ["Ref ID", "Objective Area", "Target Metric", "Time Horizon", "Baseline", "Target Criteria", "Strategic Alignment"]
    for c_i, h in enumerate(obj_headers, start=1):
        cell = ws1.cell(row=15, column=c_i, value=h)
        cell.font = font_header
        cell.fill = fill_header
        cell.alignment = align_center
        cell.border = border_all
    ws1.row_dimensions[15].height = 24

    smart_objectives = [
        ("OBJ-01", "Inventory Accuracy", "98% tracking accuracy", "6 months post-launch", "81.4%", "Audit reconciliation discrepancy <= 2.0%", "Operational Excellence"),
        ("OBJ-02", "Stockout Reduction", "-25% stockout incidents", "End of Sprint 15", "14.2% rate", "Out-of-stock events reduced to <= 3.5%", "Revenue Maximization"),
        ("OBJ-03", "Overstock Reduction", "-20% overstock volume", "9 months", "$180,000 tied cap", "Carrying costs reduced by 20% across hubs", "Working Capital Optimization"),
        ("OBJ-04", "Order Fulfilment Rate", "95% on-time fulfilment", "Within 3 months", "78.5%", "End-to-end allocation & delivery >= 95%", "Customer Satisfaction"),
        ("OBJ-05", "System Availability", "99.9% uptime SLA", "Continuous Production", "98.2%", "Zero unannounced downtime; MTTR < 15 min", "Reliability Engineering"),
    ]

    for idx, row in enumerate(smart_objectives, start=16):
        ws1.row_dimensions[idx].height = 20
        for c_i, val in enumerate(row, start=1):
            cell = ws1.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 4, 5] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    # Sprint Release Roadmap
    ws1.merge_cells("A22:G22")
    sec4 = ws1["A22"]
    sec4.value = "4. SPRINT ROADMAP & RELEASE MILESTONES (15 SPRINTS, 2-WEEK CADENCE)"
    sec4.font = font_section
    sec4.fill = fill_section
    sec4.alignment = align_left
    ws1.row_dimensions[22].height = 24

    sprint_headers = ["Sprint", "Epic / Module Focus", "Sprint Count", "Start Date", "End Date", "Target Milestone Deliverable", "Release Gate"]
    for c_i, h in enumerate(sprint_headers, start=1):
        cell = ws1.cell(row=23, column=c_i, value=h)
        cell.font = font_header
        cell.fill = fill_header
        cell.alignment = align_center
        cell.border = border_all
    ws1.row_dimensions[23].height = 24

    sprints_data = [
        ("Sprint 1", "Catalog", 1, "02-Nov-26", "13-Nov-26", "Product CRUD, Category entities, JPA repository layer", "Sprint Review 1"),
        ("Sprint 2", "Catalog", 1, "16-Nov-26", "27-Nov-26", "Category management, Search filters, Bulk CSV Import/Export", "Sprint Review 2"),
        ("Sprint 3", "Inventory", 1, "30-Nov-26", "11-Dec-26", "Multi-location inventory tracking across 3 WH and 5 Stores", "Sprint Review 3"),
        ("Sprint 4", "Inventory", 1, "14-Dec-26", "25-Dec-26", "Atomic stock adjustments, Reason codes, Audit log tracking", "Sprint Review 4"),
        ("Sprint 5", "Inventory", 1, "28-Dec-26", "08-Jan-27", "Inter-location transfers & dynamic low-stock alert thresholds", "Milestone 1 (Inventory)"),
        ("Sprint 6", "Orders", 1, "11-Jan-27", "22-Jan-27", "Sales order entity, Order lines, Customer linking & validation", "Sprint Review 6"),
        ("Sprint 7", "Orders", 1, "25-Jan-27", "05-Feb-27", "Automated stock allocation, Status transition engine, Returns", "Milestone 2 (Fulfillment)"),
        ("Sprint 8", "Supplier", 1, "08-Feb-27", "19-Feb-27", "Supplier directory, Purchase Orders creation & Goods Receipt", "Sprint Review 8"),
        ("Sprint 9", "Barcode/RFID", 1, "22-Feb-27", "05-Mar-27", "Barcode generation, Validation, REST Scanner endpoints", "Sprint Review 9"),
        ("Sprint 10", "Barcode/RFID", 1, "08-Mar-27", "19-Mar-27", "RFID tag tracking, Fast physical scan reconciliation UI", "Milestone 3 (Hardware)"),
        ("Sprint 11", "Reporting", 1, "22-Mar-27", "02-Apr-27", "Real-time KPI calculations, Stockout/Overstock metrics", "Sprint Review 11"),
        ("Sprint 12", "Reporting", 1, "05-Apr-27", "16-Apr-27", "Sales trend analytics, Recharts visualizations, CSV export", "Sprint Review 12"),
        ("Sprint 13", "User Mgmt", 1, "19-Apr-27", "30-Apr-27", "Spring Security Basic/BCrypt, RBAC Admin controls & Audit Log", "Milestone 4 (Security)"),
        ("Sprint 14", "ERP Integration", 1, "03-May-27", "14-May-27", "Bidirectional ERP Product & Stock Sync APIs (JSON/CSV)", "Sprint Review 14"),
        ("Sprint 15", "ERP Integration", 1, "17-May-27", "28-May-27", "ERP Order synchronization, End-to-end UAT & Production Launch", "Final Production Gate"),
    ]

    for idx, row in enumerate(sprints_data, start=24):
        ws1.row_dimensions[idx].height = 20
        for c_i, val in enumerate(row, start=1):
            cell = ws1.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 3, 4, 5] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws1, min_width=12, max_width=45)

    # =========================================================================
    # 2. Sheet: Product Backlog
    # =========================================================================
    ws2 = wb.create_sheet(title="Product Backlog")
    ws2.views.sheetView[0].showGridLines = True

    # Title
    ws2.merge_cells("A1:M1")
    t2 = ws2["A1"]
    t2.value = "RIMS - PRODUCT BACKLOG & WEIGHTED SHORTEST JOB FIRST (WSJF) PRIORITIZATION"
    t2.font = font_title
    t2.fill = fill_title
    t2.alignment = align_center
    ws2.row_dimensions[1].height = 36

    backlog_headers = [
        "Story ID", "Epic", "User Story Title / Description", "Acceptance Criteria",
        "Business Value (BV)", "Time Criticality (TC)", "Risk Reduction / OE (RR)",
        "Cost of Delay (CoD)", "Job Size", "WSJF Score", "Priority Rank", "Status", "Sprint"
    ]

    style_header_row(ws2, 3, len(backlog_headers))

    # 32 User Stories (No blank rows!)
    # Including exact UST R1-R7 values from prompt:
    # R1: Catalog BV5/TC4/RR4/size40
    # R2: Inventory real-time view 4/5/5/80
    # R3: Low-stock alerts 4/4/4/20
    # R4: Order processing 5/5/3/60
    # R5: Supplier/PO 4/3/4/40
    # R6: Reporting 5/4/3/40
    # R7: User mgmt 3/2/5/20
    backlog_stories = [
        # Core R1 - R7
        ("UST-R01", "Catalog", "As a Catalog Manager, I want comprehensive product CRUD and multi-criteria search so that items are accurately categorized and discoverable.", "Given valid product attributes, when submitted, then persist with SKU and barcode; search by name, category, and price range.", 5, 4, 4, 40, "Done", "Sprint 1"),
        ("UST-R02", "Inventory", "As an Inventory Analyst, I want a real-time multi-location stock view across warehouses and stores to maintain visibility of inventory on hand.", "Given location or product filter, when queried, then display real-time quantity on hand, reorder points, and safety stock.", 4, 5, 5, 80, "Done", "Sprint 3"),
        ("UST-R03", "Inventory", "As a Store Manager, I want automated low-stock and stockout alerts so that stock replenishment can be triggered proactively before stockouts occur.", "Given stock qty <= reorder point, then flag as LOW_STOCK; when qty == 0, then flag as STOCKOUT on dashboard alerts.", 4, 4, 4, 20, "Done", "Sprint 5"),
        ("UST-R04", "Orders", "As a Sales Fulfillment Lead, I want sales order creation and automated inventory allocation so that items are reserved without overselling.", "Given sufficient stock at specified location, when allocated, deduct available stock atomically; reject if insufficient.", 5, 5, 3, 60, "Done", "Sprint 6"),
        ("UST-R05", "Supplier", "As a Purchasing Specialist, I want purchase order creation and goods receipt workflows so that warehouse stock is replenished on delivery.", "Given an approved PO, when goods are received at chosen warehouse, then update PO status to RECEIVED and credit stock levels.", 4, 3, 4, 40, "Done", "Sprint 8"),
        ("UST-R06", "Reporting", "As an Executive, I want real-time KPI metrics and sales trend analytics so that inventory turnover and order fulfillment health are tracked.", "Given sales and inventory data, calculate total stock value, stockout count, fulfillment rate %, and daily sales trends.", 5, 4, 3, 40, "Done", "Sprint 11"),
        ("UST-R07", "User Mgmt", "As a System Administrator, I want role-based access control and user administration so that only authorized personnel perform operations.", "Enforce HTTP Basic + BCrypt auth; ADMIN manages users; MANAGER+ manages catalog; STAFF transfers stock; VIEWER read-only.", 3, 2, 5, 20, "Done", "Sprint 13"),

        # Epic 1: Catalog Extensions
        ("UST-C01", "Catalog", "As a Catalog Specialist, I want category hierarchy management so that products are logically structured into departments and subcategories.", "Allow CRUD on categories; validate unique category names; prevent deletion of categories with active products.", 4, 3, 2, 20, "Done", "Sprint 1"),
        ("UST-C02", "Catalog", "As a Merchandiser, I want bulk CSV import and export of product data so that seasonal catalog updates can be executed in bulk.", "Parse multipart CSV file; validate each row against schema; return imported record count and descriptive error log.", 4, 4, 3, 30, "Done", "Sprint 2"),
        ("UST-C03", "Catalog", "As a Pricing Analyst, I want cost price and retail price maintenance so that gross margins can be tracked per product.", "Validate retail price > 0 and cost price >= 0; track price changes in audit log; reflect price updates across new orders.", 3, 3, 2, 20, "Done", "Sprint 2"),

        # Epic 2: Inventory Extensions
        ("UST-I01", "Inventory", "As a Logistics Manager, I want inter-facility stock transfer workflows so that inventory can move between warehouses and stores safely.", "Validate source has sufficient quantity; deduct source and increment destination atomically; record transfer history.", 5, 4, 4, 40, "Done", "Sprint 4"),
        ("UST-I02", "Inventory", "As an Inventory Controller, I want stock level adjustment with mandatory reason codes so that physical audit variances are reconciled.", "Capture positive or negative quantity delta, location code, and reason string; persist audit log record for every adjustment.", 4, 4, 3, 30, "Done", "Sprint 4"),
        ("UST-I03", "Inventory", "As a Warehouse Auditor, I want automated overstock identification so that excess carrying costs are flagged.", "Flag items where quantity on hand exceeds safety stock by 3x; highlight overstock count on KPI reporting dashboard.", 3, 3, 2, 20, "Done", "Sprint 5"),

        # Epic 3: Orders Extensions
        ("UST-O01", "Orders", "As a Warehouse Operator, I want order status progression (NEW -> ALLOCATED -> PICKED -> PACKED -> SHIPPED) so that fulfillment stages are tracked.", "Strict state machine validation; reject out-of-order transitions; record timestamp and user notes for each step.", 4, 4, 3, 30, "Done", "Sprint 7"),
        ("UST-O02", "Orders", "As a Customer Care Representative, I want order cancellation functionality so that unfulfilled customer requests can be aborted cleanly.", "Allow cancellation only prior to SHIPPED status; release allocated stock back to available inventory; capture cancel reason.", 4, 3, 3, 20, "Done", "Sprint 7"),
        ("UST-O03", "Orders", "As a Returns Clerk, I want return processing and automatic restocking so that returned goods are promptly available for resale.", "Process returns on SHIPPED/DELIVERED orders; credit returned items to specified warehouse location; record refund amount.", 4, 4, 4, 30, "Done", "Sprint 7"),

        # Epic 4: Supplier Extensions
        ("UST-S01", "Supplier", "As a Vendor Manager, I want supplier directory CRUD so that vendor contact and address information is maintained.", "Maintain supplier name, email, phone, contact person, city, state; validate unique supplier email addresses.", 3, 2, 3, 20, "Done", "Sprint 8"),
        ("UST-S02", "Supplier", "As a Procurement Lead, I want supplier performance metrics so that vendor reliability and on-time delivery are evaluated.", "Calculate total POs, received POs, on-time delivery count, and on-time delivery percentage per supplier.", 4, 3, 3, 30, "Done", "Sprint 8"),
        ("UST-S03", "Supplier", "As a Receiving Dock Supervisor, I want purchase order receiving with inspection notes so that damaged deliveries are noted.", "Record actual received date, receiving location, and inspection notes; trigger inventory level increase.", 4, 3, 3, 20, "Done", "Sprint 8"),

        # Epic 5: Barcode & RFID
        ("UST-B01", "Barcode/RFID", "As a Store Cashier, I want a unified scan endpoint (/api/scan/{code}) so that scanning either a barcode or RFID tag retrieves the product.", "Query product by barcode or rfidTag; return 200 with product details if found; return 404 with standard JSON if absent.", 5, 5, 4, 30, "Done", "Sprint 9"),
        ("UST-B02", "Barcode/RFID", "As a Warehouse Picker, I want barcode scanner integration in the web UI so that items can be verified by handheld scanner input.", "Provide instant scan input field with auto-lookup; display matched item name, SKU, price, and category badge.", 4, 4, 3, 30, "Done", "Sprint 9"),
        ("UST-B03", "Barcode/RFID", "As a Stock Auditor, I want RFID tag bulk reconciliation so that warehouse physical cycle counts take minutes instead of days.", "Support batch RFID scan uploads; compare scanned RFID tags against registered stock levels; report count variances.", 4, 4, 4, 40, "Done", "Sprint 10"),

        # Epic 6: Reporting Extensions
        ("UST-P01", "Reporting", "As a CFO, I want total stock valuation calculated across all facilities so that asset balance sheets reflect real-time value.", "Aggregate sum of (qty * price) and sum of (qty * costPrice) across all active stock levels; expose via /api/reports/kpi.", 5, 4, 3, 30, "Done", "Sprint 11"),
        ("UST-P02", "Reporting", "As a Sales Analyst, I want daily sales revenue and volume trend graphs so that sales spikes and slow periods are visible.", "Group completed orders by date; compute daily order count and revenue sum; render interactive Recharts dual-axis line chart.", 4, 4, 3, 30, "Done", "Sprint 12"),
        ("UST-P03", "Reporting", "As a Compliance Officer, I want CSV export for inventory and orders so that periodic corporate audit files are archived.", "Generate RFC-4180 compliant CSV streams with Content-Disposition attachment headers for inventory and orders reports.", 4, 3, 3, 20, "Done", "Sprint 12"),

        # Epic 7: User Mgmt Extensions
        ("UST-U01", "User Mgmt", "As an IT Security Officer, I want user account deactivation so that offboarded employees immediately lose system access.", "Provide PATCH /api/users/{id}/deactivate; verify deactivated user credentials fail authentication with 401 Unauthorized.", 4, 3, 5, 20, "Done", "Sprint 13"),
        ("UST-U02", "User Mgmt", "As an Administrator, I want dynamic role reassignment so that staff promotions or department transfers are updated instantly.", "Provide PATCH /api/users/{id}/role?role=ROLE_...; apply new authorities immediately on subsequent requests.", 3, 2, 4, 20, "Done", "Sprint 13"),
        ("UST-U03", "User Mgmt", "As a Security Auditor, I want persistent audit logging for all mutations so that every write operation is traceable.", "Record principal username, action, entity type, entity ID, details, and timestamp into audit_logs table on every mutation.", 4, 3, 5, 30, "Done", "Sprint 13"),

        # Epic 8: ERP Integration
        ("UST-E01", "ERP Integration", "As an Enterprise Architect, I want bidirectional ERP product export and import endpoints so that master catalog stays synchronized.", "Provide GET /api/erp/products/export and POST /api/erp/products/import supporting both JSON and CSV formats.", 4, 4, 4, 40, "Done", "Sprint 14"),
        ("UST-E02", "ERP Integration", "As a Supply Chain Director, I want ERP inventory stock feeds so that external SAP/Oracle ERP reflects current warehouse levels.", "Provide GET /api/erp/stock/export and POST /api/erp/stock/import; validate location codes and adjust quantities safely.", 5, 4, 4, 40, "Done", "Sprint 14"),
        ("UST-E03", "ERP Integration", "As an Omnichannel Lead, I want ERP sales order export and batch ingest so that cross-channel online orders are fulfilled.", "Provide GET /api/erp/orders/export and POST /api/erp/orders/import; validate order lines and auto-allocate stock.", 4, 4, 3, 40, "Done", "Sprint 15"),
        ("UST-E04", "ERP Integration", "As a DevOps Engineer, I want end-to-end integration health monitoring so that ERP feed failures trigger immediate alerts.", "Expose Prometheus / Actuator health metrics for ERP connector queue depth and HTTP error rates.", 3, 3, 3, 20, "Done", "Sprint 15"),
    ]

    start_row = 4
    end_row = start_row + len(backlog_stories) - 1

    for idx, item in enumerate(backlog_stories, start=start_row):
        r = idx
        ws2.row_dimensions[r].height = 22
        sid, epic, title, ac, bv, tc, rr, size, status, sprint = item

        ws2.cell(row=r, column=1, value=sid).alignment = align_center
        ws2.cell(row=r, column=2, value=epic).alignment = align_left
        ws2.cell(row=r, column=3, value=title).alignment = align_wrap_left
        ws2.cell(row=r, column=4, value=ac).alignment = align_wrap_left
        ws2.cell(row=r, column=5, value=bv).alignment = align_center
        ws2.cell(row=r, column=6, value=tc).alignment = align_center
        ws2.cell(row=r, column=7, value=rr).alignment = align_center

        # Cost of Delay Formula: =(E + F + G)
        ws2.cell(row=r, column=8, value=f"=E{r}+F{r}+G{r}").alignment = align_center

        ws2.cell(row=r, column=9, value=size).alignment = align_center

        # WSJF Score Live Formula: =(E + F + G)/I
        ws2.cell(row=r, column=10, value=f"=(E{r}+F{r}+G{r})/I{r}").alignment = align_center
        ws2.cell(row=r, column=10).number_format = "0.00"

        # Priority Rank Live Formula: =RANK(J{r}, $J$4:$J${end_row})
        ws2.cell(row=r, column=11, value=f"=RANK(J{r}, $J${start_row}:$J${end_row})").alignment = align_center

        ws2.cell(row=r, column=12, value=status).alignment = align_center
        ws2.cell(row=r, column=13, value=sprint).alignment = align_center

        for c in range(1, 14):
            cell = ws2.cell(row=r, column=c)
            cell.font = font_regular
            cell.border = border_all
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws2, min_width=10, max_width=50)
    ws2.column_dimensions["C"].width = 38
    ws2.column_dimensions["D"].width = 42

    # =========================================================================
    # 3. Sheet: Estimation
    # =========================================================================
    ws3 = wb.create_sheet(title="Estimation")
    ws3.views.sheetView[0].showGridLines = True

    # Title
    ws3.merge_cells("A1:H1")
    t3 = ws3["A1"]
    t3.value = "RIMS - STORY POINT ESTIMATION & EPIC EFFORT ALLOCATION"
    t3.font = font_title
    t3.fill = fill_title
    t3.alignment = align_center
    ws3.row_dimensions[1].height = 36

    ws3.merge_cells("A3:H3")
    sec_est1 = ws3["A3"]
    sec_est1.value = "1. EPIC LEVEL STORY POINT BREAKDOWN (640 TOTAL STORY POINTS)"
    sec_est1.font = font_section
    sec_est1.fill = fill_section
    sec_est1.alignment = align_left
    ws3.row_dimensions[3].height = 24

    est_headers = [
        "Epic Code", "Epic Name", "Simple Stories (Pts)", "Medium Stories (Pts)",
        "Complex Stories (Pts)", "Total Epic Points", "Sprints Allocated", "Avg Velocity Needed (Pts/Sp)"
    ]
    style_header_row(ws3, 4, len(est_headers))

    # Prompts specific numbers:
    # Catalog 80, Inventory 120, Orders 100, Supplier 90, Reporting 70, User Mgmt 40, plus Barcode/RFID and ERP Integration
    epics_est_data = [
        ("EPC-01", "Catalog", 20, 30, 30, 2),          # Total: 80
        ("EPC-02", "Inventory", 20, 50, 50, 3),        # Total: 120
        ("EPC-03", "Orders", 20, 40, 40, 2),           # Total: 100
        ("EPC-04", "Supplier", 20, 40, 30, 1),         # Total: 90
        ("EPC-05", "Barcode & RFID", 10, 25, 25, 2),   # Total: 60
        ("EPC-06", "Reporting", 20, 25, 25, 2),        # Total: 70
        ("EPC-07", "User Mgmt", 10, 15, 15, 1),        # Total: 40
        ("EPC-08", "ERP Integration", 10, 35, 35, 2),  # Total: 80
    ]

    for idx, (code, name, s_pts, m_pts, c_pts, sprints) in enumerate(epics_est_data, start=5):
        r = idx
        ws3.row_dimensions[r].height = 20
        ws3.cell(row=r, column=1, value=code).alignment = align_center
        ws3.cell(row=r, column=2, value=name).alignment = align_left
        ws3.cell(row=r, column=3, value=s_pts).alignment = align_center
        ws3.cell(row=r, column=4, value=m_pts).alignment = align_center
        ws3.cell(row=r, column=5, value=c_pts).alignment = align_center

        # Total points formula =SUM(C..E)
        ws3.cell(row=r, column=6, value=f"=SUM(C{r}:E{r})").alignment = align_center
        ws3.cell(row=r, column=6).font = font_bold

        ws3.cell(row=r, column=7, value=sprints).alignment = align_center

        # Avg velocity needed =F / G
        ws3.cell(row=r, column=8, value=f"=F{r}/G{r}").alignment = align_center
        ws3.cell(row=r, column=8).number_format = "0.0"

        for c in range(1, 9):
            cell = ws3.cell(row=r, column=c)
            cell.border = border_all
            if idx % 2 == 1:
                cell.fill = fill_zebra

    # Totals Row
    tot_r = 5 + len(epics_est_data)
    ws3.row_dimensions[tot_r].height = 24
    ws3.cell(row=tot_r, column=1, value="TOTAL").alignment = align_center
    ws3.cell(row=tot_r, column=2, value="All 8 Epics").alignment = align_left
    ws3.cell(row=tot_r, column=3, value=f"=SUM(C5:C{tot_r-1})").alignment = align_center
    ws3.cell(row=tot_r, column=4, value=f"=SUM(D5:D{tot_r-1})").alignment = align_center
    ws3.cell(row=tot_r, column=5, value=f"=SUM(E5:E{tot_r-1})").alignment = align_center
    ws3.cell(row=tot_r, column=6, value=f"=SUM(F5:F{tot_r-1})").alignment = align_center
    ws3.cell(row=tot_r, column=7, value=f"=SUM(G5:G{tot_r-1})").alignment = align_center
    ws3.cell(row=tot_r, column=8, value=f"=F{tot_r}/G{tot_r}").alignment = align_center
    ws3.cell(row=tot_r, column=8).number_format = "0.0"

    for c in range(1, 9):
        cell = ws3.cell(row=tot_r, column=c)
        cell.font = font_bold
        cell.fill = fill_highlight
        cell.border = border_top_thick

    # Estimation Assumptions & Fibonacci Scale
    ws3.merge_cells("A16:H16")
    sec_est2 = ws3["A16"]
    sec_est2.value = "2. SIZING MATRIX & FIBONACCI PLANNING POKER SCALE"
    sec_est2.font = font_section
    sec_est2.fill = fill_section
    sec_est2.alignment = align_left
    ws3.row_dimensions[16].height = 24

    scale_headers = ["Point Value", "T-Shirt Size", "Typical Complexity", "Backend Effort (Days)", "Frontend Effort (Days)", "QA Test Cases", "Example Story Scope"]
    ws3.row_dimensions[17].height = 22
    for c_i, h in enumerate(scale_headers, start=1):
        cell = ws3.cell(row=17, column=c_i, value=h)
        cell.font = font_header
        cell.fill = fill_header
        cell.alignment = align_center
        cell.border = border_all

    scale_data = [
        (1, "XS", "Trivial config/copy change", "< 0.5 day", "< 0.5 day", "1-2 cases", "Add a UI tooltip or error label"),
        (2, "S", "Simple CRUD / minor field addition", "1 day", "1 day", "3-5 cases", "Add active flag filter to category endpoint"),
        (3, "M", "Standard feature with DB & UI integration", "2-3 days", "2-3 days", "5-8 cases", "User account activation/deactivation"),
        (5, "L", "Multi-component logic & business rules", "3-5 days", "3-4 days", "8-12 cases", "Stock adjustment with reason & audit log"),
        (8, "XL", "Complex multi-table transaction & state machine", "5-7 days", "4-6 days", "15-20 cases", "Sales order allocation & status flow engine"),
        (13, "XXL", "Subsystem integration or hardware interface", "8-10 days", "6-8 days", "20-30 cases", "Bidirectional ERP bulk sync or RFID auditing"),
    ]

    for idx, row in enumerate(scale_data, start=18):
        ws3.row_dimensions[idx].height = 20
        for c_i, val in enumerate(row, start=1):
            cell = ws3.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 2, 4, 5, 6] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws3, min_width=12, max_width=45)

    # =========================================================================
    # 4. Sheet: Product Burndown
    # =========================================================================
    ws4 = wb.create_sheet(title="Product Burndown")
    ws4.views.sheetView[0].showGridLines = True

    # Title
    ws4.merge_cells("A1:H1")
    t4 = ws4["A1"]
    t4.value = "RIMS - PRODUCT BURNDOWN TRACKER (15 SPRINTS, 640 STORY POINTS)"
    t4.font = font_title
    t4.fill = fill_title
    t4.alignment = align_center
    ws4.row_dimensions[1].height = 36

    bd_headers = [
        "Sprint", "Start Date", "End Date", "Planned Burn (Pts)", "Planned Remaining (Pts)",
        "Actual Burn (Pts)", "Actual Remaining (Pts)", "Variance (Pts)"
    ]
    style_header_row(ws4, 3, len(bd_headers))

    # Baseline Row (Sprint 0)
    ws4.row_dimensions[4].height = 20
    ws4.cell(row=4, column=1, value="Sprint 0").alignment = align_center
    ws4.cell(row=4, column=2, value="Baseline").alignment = align_center
    ws4.cell(row=4, column=3, value="Baseline").alignment = align_center
    ws4.cell(row=4, column=4, value=0).alignment = align_center
    ws4.cell(row=4, column=5, value=640).alignment = align_center
    ws4.cell(row=4, column=6, value=0).alignment = align_center
    ws4.cell(row=4, column=7, value=640).alignment = align_center
    ws4.cell(row=4, column=8, value=0).alignment = align_center

    for c in range(1, 9):
        cell = ws4.cell(row=4, column=c)
        cell.font = font_bold
        cell.fill = fill_highlight
        cell.border = border_all

    # 15 Sprints Burndown Data
    burndown_points = [
        ("Sprint 1", "02-Nov-26", "13-Nov-26", 40, 42),
        ("Sprint 2", "16-Nov-26", "27-Nov-26", 40, 40),
        ("Sprint 3", "30-Nov-26", "11-Dec-26", 40, 38),
        ("Sprint 4", "14-Dec-26", "25-Dec-26", 40, 44),
        ("Sprint 5", "28-Dec-26", "08-Jan-27", 40, 42),
        ("Sprint 6", "11-Jan-27", "22-Jan-27", 50, 48),
        ("Sprint 7", "25-Jan-27", "05-Feb-27", 50, 52),
        ("Sprint 8", "08-Feb-27", "19-Feb-27", 90, 88),
        ("Sprint 9", "22-Feb-27", "05-Mar-27", 30, 32),
        ("Sprint 10", "08-Mar-27", "19-Mar-27", 30, 30),
        ("Sprint 11", "22-Mar-27", "02-Apr-27", 35, 34),
        ("Sprint 12", "05-Apr-27", "16-Apr-27", 35, 36),
        ("Sprint 13", "19-Apr-27", "30-Apr-27", 40, 40),
        ("Sprint 14", "03-May-27", "14-May-27", 40, 38),
        ("Sprint 15", "17-May-27", "28-May-27", 40, 46),
    ]

    for idx, (sp_name, s_date, e_date, p_burn, a_burn) in enumerate(burndown_points, start=5):
        r = idx
        prev_r = r - 1
        ws4.row_dimensions[r].height = 20

        ws4.cell(row=r, column=1, value=sp_name).alignment = align_center
        ws4.cell(row=r, column=2, value=s_date).alignment = align_center
        ws4.cell(row=r, column=3, value=e_date).alignment = align_center
        ws4.cell(row=r, column=4, value=p_burn).alignment = align_center

        # Planned Remaining Formula =E{prev} - D{r}
        ws4.cell(row=r, column=5, value=f"=E{prev_r}-D{r}").alignment = align_center

        ws4.cell(row=r, column=6, value=a_burn).alignment = align_center

        # Actual Remaining Formula =G{prev} - F{r}
        ws4.cell(row=r, column=7, value=f"=G{prev_r}-F{r}").alignment = align_center

        # Variance Formula =G{r} - E{r} (negative means ahead of schedule!)
        ws4.cell(row=r, column=8, value=f"=G{r}-E{r}").alignment = align_center

        for c in range(1, 9):
            cell = ws4.cell(row=r, column=c)
            cell.font = font_regular
            cell.border = border_all
            if idx % 2 == 1:
                cell.fill = fill_zebra

    # Line Chart for Burndown
    chart = LineChart()
    chart.title = "Product Burndown Curve - Planned vs Actual (640 Story Points)"
    chart.style = 13
    chart.y_axis.title = "Story Points Remaining"
    chart.x_axis.title = "Sprint Cadence"
    chart.width = 18
    chart.height = 10

    # Data series: columns E and G (Planned Remaining and Actual Remaining) rows 4 to 19
    data = Reference(ws4, min_col=5, min_row=3, max_col=7, max_row=19)
    # Categories: column A (Sprint names) rows 4 to 19
    categories = Reference(ws4, min_col=1, min_row=4, max_row=19)

    chart.add_data(data, titles_from_data=True)
    chart.set_categories(categories)

    # Remove the column F (Actual Burn) series if included; we want Planned Remaining (col 5) & Actual Remaining (col 7)
    # In data range min_col=5 to max_col=7, col 5 is Planned Remaining, col 6 is Actual Burn, col 7 is Actual Remaining
    # Let's clean chart series:
    if len(chart.series) >= 3:
        # Keep series 0 (Planned Remaining) and series 2 (Actual Remaining), remove series 1 (Actual Burn)
        s_planned = chart.series[0]
        s_actual = chart.series[2]
        s_planned.graphicalProperties.line.solidFill = "3B82F6"  # Blue
        s_planned.graphicalProperties.line.width = 25000
        s_actual.graphicalProperties.line.solidFill = "EF4444"   # Red
        s_actual.graphicalProperties.line.width = 25000
        chart.series = [s_planned, s_actual]

    ws4.add_chart(chart, "J4")

    auto_fit_columns(ws4, min_width=12, max_width=30)

    # =========================================================================
    # 5. Sheet: Resource Plan
    # =========================================================================
    ws5 = wb.create_sheet(title="Resource Plan")
    ws5.views.sheetView[0].showGridLines = True

    # Title
    ws5.merge_cells("A1:T1")
    t5 = ws5["A1"]
    t5.value = "RIMS - RESOURCE CAPACITY & SPRINT ALLOCATION PLAN (19 TEAM MEMBERS)"
    t5.font = font_title
    t5.fill = fill_title
    t5.alignment = align_center
    ws5.row_dimensions[1].height = 36

    res_headers = ["Staff Name", "Role Category", "Specialization", "Base FTE"] + [f"Sp {i}" for i in range(1, 16)] + ["Avg Allocation"]
    style_header_row(ws5, 3, len(res_headers))

    # Required roles: PM1, SM1, BA2, FE4, BE4, DBA2, DevOps2, QA3 = 19 people
    resources_data = [
        ("Manmeet Jalota", "PM", "Project Manager / Delivery Lead", 1.0),
        ("Priya Sharma", "SM", "Scrum Master / Agile Coach", 1.0),
        ("Arjun Mehta", "BA", "Lead Retail Business Analyst", 1.0),
        ("Divya Nair", "BA", "Business Analyst - Inventory & Supply", 1.0),
        ("Rahul Verma", "FE", "Lead Frontend Architect (React)", 1.0),
        ("Sneha Patel", "FE", "Senior Frontend Engineer (UI/UX)", 1.0),
        ("Ananya Iyer", "FE", "Frontend Developer (Recharts/State)", 1.0),
        ("Rohan Gupta", "FE", "Frontend Developer (Forms/Scanner)", 1.0),
        ("Vikram Rao", "BE", "Lead Backend Architect (Spring Boot)", 1.0),
        ("Neha Joshi", "BE", "Senior Backend Engineer (Inventory/Orders)", 1.0),
        ("Amit Kulkarni", "BE", "Backend Developer (ERP Integration)", 1.0),
        ("Pooja Desai", "BE", "Backend Developer (Catalog/Audit)", 1.0),
        ("Suresh Menon", "DBA", "Lead Database Administrator (MySQL)", 1.0),
        ("Kavita Pillai", "DBA", "Database Administrator (Performance/ETL)", 1.0),
        ("Karan Singhania", "DevOps", "Lead Cloud/DevOps Architect (K8s/AWS)", 1.0),
        ("Rajesh Nair", "DevOps", "DevOps Engineer (CI/CD Jenkins/Docker)", 1.0),
        ("Meera Krishnan", "QA", "QA Automation Lead (Integration)", 1.0),
        ("Deepa Sundaram", "QA", "Senior QA Engineer (API/Performance)", 1.0),
        ("Tarun Kapoor", "QA", "QA Engineer (Functional & Regression)", 1.0),
    ]

    for idx, (name, role, spec, fte) in enumerate(resources_data, start=4):
        r = idx
        ws5.row_dimensions[r].height = 20
        ws5.cell(row=r, column=1, value=name).alignment = align_left
        ws5.cell(row=r, column=2, value=role).alignment = align_center
        ws5.cell(row=r, column=3, value=spec).alignment = align_left
        ws5.cell(row=r, column=4, value=fte).alignment = align_center

        # Allocation per sprint (1.0 = 100%)
        for sp in range(1, 16):
            col_idx = 4 + sp
            # Realistic allocations: mostly 1.0 (100%), with slight variations for DBA/DevOps
            alloc = 1.0
            if role == "DBA" and sp in [1, 2, 8, 14, 15]:
                alloc = 1.0
            elif role == "DBA":
                alloc = 0.8
            if role == "DevOps" and sp in [1, 2, 9, 14, 15]:
                alloc = 1.0
            elif role == "DevOps":
                alloc = 0.8

            cell = ws5.cell(row=r, column=col_idx, value=alloc)
            cell.alignment = align_center
            cell.number_format = "0%"

        # Average Allocation Formula =AVERAGE(E..S)
        avg_cell = ws5.cell(row=r, column=20, value=f"=AVERAGE(E{r}:S{r})")
        avg_cell.alignment = align_center
        avg_cell.number_format = "0.0%"

        for c in range(1, 21):
            cell = ws5.cell(row=r, column=c)
            cell.font = font_regular
            cell.border = border_all
            if idx % 2 == 1:
                cell.fill = fill_zebra

    # Resource Total Row
    tot_res_r = 4 + len(resources_data)
    ws5.row_dimensions[tot_res_r].height = 24
    ws5.cell(row=tot_res_r, column=1, value="TOTAL ALLOCATED FTE").alignment = align_left
    ws5.cell(row=tot_res_r, column=2, value="19 Headcount").alignment = align_center
    ws5.cell(row=tot_res_r, column=3, value="Entire RIMS Squad").alignment = align_left
    ws5.cell(row=tot_res_r, column=4, value=f"=SUM(D4:D{tot_res_r-1})").alignment = align_center

    for sp in range(1, 16):
        c_i = 4 + sp
        c_let = get_column_letter(c_i)
        ws5.cell(row=tot_res_r, column=c_i, value=f"=SUM({c_let}4:{c_let}{tot_res_r-1})").alignment = align_center
        ws5.cell(row=tot_res_r, column=c_i).number_format = "0.0"

    ws5.cell(row=tot_res_r, column=20, value=f"=AVERAGE(E{tot_res_r}:S{tot_res_r})").alignment = align_center
    ws5.cell(row=tot_res_r, column=20).number_format = "0.0"

    for c in range(1, 21):
        cell = ws5.cell(row=tot_res_r, column=c)
        cell.font = font_bold
        cell.fill = fill_highlight
        cell.border = border_top_thick

    auto_fit_columns(ws5, min_width=8, max_width=35)
    ws5.column_dimensions["A"].width = 20
    ws5.column_dimensions["C"].width = 30

    # =========================================================================
    # 6. Sheet: IT Services Plan
    # =========================================================================
    ws6 = wb.create_sheet(title="IT Services Plan")
    ws6.views.sheetView[0].showGridLines = True

    ws6.merge_cells("A1:G1")
    t6 = ws6["A1"]
    t6.value = "RIMS - IT SERVICES, INFRASTRUCTURE & TOOLING ECOSYSTEM"
    t6.font = font_title
    t6.fill = fill_title
    t6.alignment = align_center
    ws6.row_dimensions[1].height = 36

    # Section 1: Environments
    ws6.merge_cells("A3:G3")
    sec_it1 = ws6["A3"]
    sec_it1.value = "1. DEPLOYMENT ENVIRONMENTS MATRIX"
    sec_it1.font = font_section
    sec_it1.fill = fill_section
    sec_it1.alignment = align_left
    ws6.row_dimensions[3].height = 24

    env_headers = ["Environment", "Purpose", "Hosting Infrastructure", "Database Tier", "Deployment Mechanism", "Access Control", "SLA Target"]
    style_header_row(ws6, 4, len(env_headers))

    env_data = [
        ("Local / Dev", "Daily development & feature prototyping", "Developer Workstations & Docker Compose", "H2 In-Memory / Local MySQL 8.0", "Local Spring Boot & Vite HMR", "Developers & QA", "Best Effort"),
        ("Test / QA", "Automated CI regression & component testing", "AWS EKS (qa namespace, 2x m5.large)", "Amazon RDS MySQL 8.0 (db.t4g.medium)", "Automated Jenkins Pipeline Trigger", "Dev, QA & Scrum Master", "99.0%"),
        ("Staging / UAT", "Client demo, acceptance testing & performance benchmarking", "AWS EKS (staging namespace, 3x m5.xlarge)", "Amazon RDS MySQL 8.0 Multi-AZ", "Jenkins Manual Approval Gate", "Stakeholders, BAs & QA Lead", "99.5%"),
        ("Production", "Live enterprise retail inventory operations", "AWS EKS (prod namespace, 5x c5.2xlarge)", "Amazon RDS MySQL 8.0 Multi-AZ High IOPS", "Jenkins CD with Canary Ingress rollout", "Ops & Senior Architects only", "99.9%"),
    ]

    for idx, row in enumerate(env_data, start=5):
        ws6.row_dimensions[idx].height = 22
        for c_i, val in enumerate(row, start=1):
            cell = ws6.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 7] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    # Section 2: Core Tooling Ecosystem
    ws6.merge_cells("A11:G11")
    sec_it2 = ws6["A11"]
    sec_it2.value = "2. CORE DEVSECOPS & COLLABORATION TOOLCHAIN (Jira, GitHub, Jenkins, Docker, AWS)"
    sec_it2.font = font_section
    sec_it2.fill = fill_section
    sec_it2.alignment = align_left
    ws6.row_dimensions[11].height = 24

    tool_headers = ["Tool Category", "Software / Service", "Version / Edition", "Primary Project Role", "Integration Interfaces", "Responsible Lead", "Status"]
    style_header_row(ws6, 12, len(tool_headers))

    tools_data = [
        ("Project Tracking", "Jira Software Cloud", "Enterprise Cloud", "Agile backlog, Sprint planning, Burndown & Velocity", "GitHub PR links, Slack alerts", "Priya Sharma (SM)", "Active"),
        ("Source Control", "GitHub Enterprise", "Cloud SaaS", "Git repository, branch protection, code reviews & PRs", "Jenkins webhooks, SonarQube", "Rahul Verma (FE Lead)", "Active"),
        ("CI/CD Automation", "Jenkins", "LTS 2.440+", "Automated build, test, multi-stage docker build & k8s deploy", "GitHub, Docker Hub, EKS kubectl", "Rajesh Nair (DevOps)", "Active"),
        ("Containerization", "Docker & Docker Compose", "v26.0+ / Compose v2.26", "Multi-stage images for backend (JRE 21) & frontend (Nginx)", "Local Dev, Jenkins agent runners", "Karan Singhania (DevOps)", "Active"),
        ("Cloud Platform", "Amazon Web Services (AWS)", "EKS, RDS, S3, IAM, CloudWatch", "Managed Kubernetes, Multi-AZ MySQL, Artifact storage", "Terraform, Kubernetes Provider", "Karan Singhania (DevOps)", "Active"),
        ("Static Analysis", "SonarQube & SpotBugs", "Community 10.4", "Automated code quality, test coverage gates & security scans", "Jenkins pipeline gate", "Vikram Rao (BE Lead)", "Active"),
        ("API Documentation", "Springdoc OpenAPI / Swagger", "OpenAPI 3.0 (v2.6)", "Interactive API specification for backend endpoints", "/swagger-ui.html & OpenAPI JSON", "Amit Kulkarni (BE)", "Active"),
        ("Application Monitoring", "Prometheus & Grafana", "v2.50 / v10.3", "Actuator metrics, JVM memory, CPU, request latency dashboards", "Spring Boot Actuator /micrometer", "Rajesh Nair (DevOps)", "Active"),
    ]

    for idx, row in enumerate(tools_data, start=13):
        ws6.row_dimensions[idx].height = 22
        for c_i, val in enumerate(row, start=1):
            cell = ws6.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 3, 7] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws6, min_width=12, max_width=45)

    # =========================================================================
    # 7. Sheet: Training Plan
    # =========================================================================
    ws7 = wb.create_sheet(title="Training Plan")
    ws7.views.sheetView[0].showGridLines = True

    ws7.merge_cells("A1:H1")
    t7 = ws7["A1"]
    t7.value = "RIMS - SQUAD & STAKEHOLDER TRAINING CURRICULUM"
    t7.font = font_title
    t7.fill = fill_title
    t7.alignment = align_center
    ws7.row_dimensions[1].height = 36

    train_headers = ["Module ID", "Training Topic / Curriculum", "Target Audience", "Lead Trainer", "Delivery Mode", "Scheduled Date", "Duration", "Completion Status"]
    style_header_row(ws7, 3, len(train_headers))

    training_data = [
        ("TRN-001", "Agile Scrum Masterclass & Jira Sprint Ceremonies", "Entire Project Squad (19 members)", "Priya Sharma (SM)", "Virtual Workshop", "04-Nov-26", "8 hours", "Completed"),
        ("TRN-002", "Spring Boot 3, Hibernate JPA & Transaction Management", "Backend Developers & DBAs", "Vikram Rao (BE Lead)", "Hands-on Lab", "10-Nov-26", "12 hours", "Completed"),
        ("TRN-003", "React 19, Modern State Hooks & Recharts Analytics", "Frontend Developers & QA", "Rahul Verma (FE Lead)", "Interactive Workshop", "17-Nov-26", "10 hours", "Completed"),
        ("TRN-004", "Docker Multi-Stage Containers & Local Compose Workflows", "Developers & DevOps", "Karan Singhania (DevOps)", "Hands-on Lab", "24-Nov-26", "6 hours", "Completed"),
        ("TRN-005", "Multi-Location Inventory Rebalancing & Safety Stock Math", "BAs, QA & Inventory Staff", "Arjun Mehta (Lead BA)", "Classroom / Case Study", "02-Dec-26", "6 hours", "Completed"),
        ("TRN-006", "Kubernetes Deployment, Health Probes & Ingress Routing", "Backend & DevOps Team", "Rajesh Nair (DevOps)", "Hands-on Lab", "15-Dec-26", "8 hours", "Completed"),
        ("TRN-007", "Barcode & Handheld RFID Scanner Hardware Integration", "Frontend, QA & Warehouse Leads", "Rohan Gupta (FE)", "Hardware Workshop", "18-Jan-27", "8 hours", "Scheduled"),
        ("TRN-008", "MySQL 8.0 Query Optimization, Indexing & Deadlock Avoidance", "Backend Developers & DBAs", "Suresh Menon (Lead DBA)", "Deep-dive Seminar", "02-Feb-27", "6 hours", "Scheduled"),
        ("TRN-009", "Store Staff Operational Workflow & Scan Gun Usability", "Store Associates & Managers (5 stores)", "Divya Nair (BA)", "On-site Store Simulation", "15-Mar-27", "16 hours", "Scheduled"),
        ("TRN-010", "Production Incident Management, Actuator & Runbook Drills", "DevOps, PM & Leads", "Karan Singhania (DevOps)", "Simulation / Tabletop", "12-Apr-27", "4 hours", "Scheduled"),
    ]

    for idx, row in enumerate(training_data, start=4):
        ws7.row_dimensions[idx].height = 22
        for c_i, val in enumerate(row, start=1):
            cell = ws7.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 5, 6, 7, 8] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws7, min_width=12, max_width=42)

    # =========================================================================
    # 8. Sheet: Risk Management
    # =========================================================================
    ws8 = wb.create_sheet(title="Risk Management")
    ws8.views.sheetView[0].showGridLines = True

    ws8.merge_cells("A1:K1")
    t8 = ws8["A1"]
    t8.value = "RIMS - COMPREHENSIVE RISK MANAGEMENT REGISTER (8+ RISKS WITH FORMULA SCORING)"
    t8.font = font_title
    t8.fill = fill_title
    t8.alignment = align_center
    ws8.row_dimensions[1].height = 36

    risk_headers = [
        "Risk ID", "Category", "Risk Description & Cause", "Probability (P: 1-5)",
        "Impact (I: 1-5)", "Risk Score (P x I)", "Risk Severity Level",
        "Preventive Mitigation Strategy", "Contingency Action Plan", "Risk Owner", "Status"
    ]
    style_header_row(ws8, 3, len(risk_headers))

    # 10 comprehensive risks (prompt requires 8+)
    risks_data = [
        ("RSK-01", "Hardware", "Handheld RFID / Barcode scanner latency in warehouse aisles with metal shelving causing signal degradation.", 4, 4, "High", "Deploy industrial Wi-Fi 6 mesh repeaters in warehouses; implement offline scan caching on handheld clients.", "Switch to local batch buffer mode with delayed background upload sync.", "Karan Singhania", "Active Monitoring"),
        ("RSK-02", "Performance", "Database locking / deadlock contention during simultaneous order allocations across multiple concurrent stores.", 3, 5, "High", "Enforce short database transactions; implement optimistic locking with Spring @Version and indexed query lookups.", "Retry queue with exponential backoff on transient locking exceptions.", "Suresh Menon", "Mitigated"),
        ("RSK-03", "Integration", "Legacy ERP system API rate limits or intermittent maintenance downtime causing order/stock sync stalls.", 4, 4, "High", "Implement resilient asynchronous messaging queue with Spring retry mechanisms and dead-letter storage.", "Manual batch CSV export/import fallback interface for reconciliation.", "Amit Kulkarni", "Active Monitoring"),
        ("RSK-04", "Network", "Retail store internet link disconnection interrupting direct POS stock check operations.", 3, 4, "Medium", "Equip stores with dual 5G cellular failover routers; cache store-specific stock levels locally in browser session.", "Store offline transactions locally and sync on re-connection.", "Rajesh Nair", "Mitigated"),
        ("RSK-05", "Scope", "Stakeholder request for additional custom analytics dashboards causing scope creep in Sprints 11-12.", 4, 3, "Medium", "Strict Product Owner gatekeeping; require formal Change Request and defer out-of-scope widgets to Release 2.", "Swap lower-priority reporting stories to preserve release date.", "Priya Sharma", "Controlled"),
        ("RSK-06", "Resource", "Key attrition of senior Spring Boot or React frontend developers during core implementation.", 2, 4, "Medium", "Enforce pair programming, comprehensive code documentation, and cross-skilling across backend/frontend teams.", "Maintain pre-vetted contractor roster with HCL talent pool on 1-week notice.", "Manmeet Jalota", "Open"),
        ("RSK-07", "Security", "Vulnerabilities discovered in third-party npm packages or Maven open-source dependencies.", 3, 4, "Medium", "Integrate automated OWASP dependency-check and SonarQube vulnerability gates into Jenkins build pipeline.", "Automated hotfix patching and package substitution protocol.", "Vikram Rao", "Controlled"),
        ("RSK-08", "Data Quality", "Inaccurate physical inventory counts in legacy ERP causing discrepancy during initial data migration.", 4, 4, "High", "Conduct physical cycle counts in all 3 warehouses prior to Sprint 14 data freeze; build reconciliation scripts.", "Establish 7-day post-migration inventory variance buffer ledger.", "Arjun Mehta", "Open"),
        ("RSK-09", "Cost", "AWS EKS and RDS multi-AZ cloud compute expenses exceeding initial quarterly infrastructure budget.", 3, 3, "Medium", "Implement Kubernetes cluster auto-scaling and configure automated shutdown of non-prod nodes on weekends.", "Purchase 1-year AWS Compute Savings Plans for persistent workloads.", "Karan Singhania", "Controlled"),
        ("RSK-10", "Adoption", "Store associate resistance or slow user adoption of handheld barcode scan validation procedures.", 3, 3, "Medium", "Conduct interactive hands-on training workshops and assign store super-users for peer assistance.", "Gamify scan compliance and provide rapid-response on-site coaching.", "Divya Nair", "Open"),
    ]

    for idx, item in enumerate(risks_data, start=4):
        r = idx
        ws8.row_dimensions[r].height = 24
        rid, cat, desc, prob, imp, sev, mit, cont, owner, stat = item

        ws8.cell(row=r, column=1, value=rid).alignment = align_center
        ws8.cell(row=r, column=2, value=cat).alignment = align_center
        ws8.cell(row=r, column=3, value=desc).alignment = align_wrap_left
        ws8.cell(row=r, column=4, value=prob).alignment = align_center
        ws8.cell(row=r, column=5, value=imp).alignment = align_center

        # Formula: Score = Prob * Impact
        ws8.cell(row=r, column=6, value=f"=D{r}*E{r}").alignment = align_center
        ws8.cell(row=r, column=6).font = font_bold

        ws8.cell(row=r, column=7, value=sev).alignment = align_center
        ws8.cell(row=r, column=8, value=mit).alignment = align_wrap_left
        ws8.cell(row=r, column=9, value=cont).alignment = align_wrap_left
        ws8.cell(row=r, column=10, value=owner).alignment = align_center
        ws8.cell(row=r, column=11, value=stat).alignment = align_center

        for c in range(1, 12):
            cell = ws8.cell(row=r, column=c)
            cell.font = font_regular
            cell.border = border_all
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws8, min_width=10, max_width=45)
    ws8.column_dimensions["C"].width = 38
    ws8.column_dimensions["H"].width = 38
    ws8.column_dimensions["I"].width = 38

    # =========================================================================
    # 9. Sheet: Opportunity Management
    # =========================================================================
    ws9 = wb.create_sheet(title="Opportunity Management")
    ws9.views.sheetView[0].showGridLines = True

    ws9.merge_cells("A1:I1")
    t9 = ws9["A1"]
    t9.value = "RIMS - OPPORTUNITY MANAGEMENT REGISTER (VALUE CREATION & INNOVATION)"
    t9.font = font_title
    t9.fill = fill_title
    t9.alignment = align_center
    ws9.row_dimensions[1].height = 36

    opp_headers = [
        "Opp ID", "Opportunity Category", "Opportunity Description", "Potential Strategic Benefit",
        "Feasibility (High/Med/Low)", "Impact Score (1-5)", "Action Plan & Next Steps", "Lead Champion", "Status"
    ]
    style_header_row(ws9, 3, len(opp_headers))

    # 5 opportunities (prompt requires 4+)
    opportunities_data = [
        ("OPP-01", "AI & Predictive", "Implement ML-based predictive sales forecasting to automate dynamic safety stock calculations.", "Reduces overstock carrying costs by an estimated additional 12%; prevents regional holiday stockouts.", "Medium", 5, "Develop Python microservice container connecting to historical sales trends API; trial in Sprint 12.", "Vikram Rao", "Under Evaluation"),
        ("OPP-02", "Mobile PWA", "Package frontend as a Progressive Web App (PWA) with camera-based barcode scanning for store tablets.", "Eliminates need for $45,000 in dedicated barcode scanner hardware across small format retail stores.", "High", 4, "Integrate HTML5 Barcode Detection API into React frontend; test with store camera devices.", "Rahul Verma", "In Progress"),
        ("OPP-03", "Supplier Self-Service", "Provide external supplier portal for automated PO acknowledgement, ASN updates, and invoice tracking.", "Reduces procurement communications overhead by 40%; shortens PO fulfillment cycle by 3 days.", "High", 4, "Expose restricted external portal endpoints using existing Spring Security RBAC; schedule for Release 2.", "Arjun Mehta", "Approved for R2"),
        ("OPP-04", "Multi-Region Cloud", "Deploy active-active multi-region AWS EKS clusters to support international store chain expansion.", "Guarantees < 50ms latency for global stores; boosts disaster recovery RPO/RTO to near zero.", "Medium", 4, "Formulate multi-region RDS Aurora architecture proposal and benchmark latency across EU/APAC nodes.", "Karan Singhania", "Research Phase"),
        ("OPP-05", "Partner Webhooks", "Implement real-time inventory webhook subscriptions for external third-party e-commerce channels.", "Enables instant multi-channel dropshipping and sales synchronization with Shopify and Amazon.", "High", 5, "Design event-driven Spring Cloud Stream webhook dispatcher triggered on stock allocation events.", "Amit Kulkarni", "Prototype Ready"),
    ]

    for idx, row in enumerate(opportunities_data, start=4):
        ws9.row_dimensions[idx].height = 24
        for c_i, val in enumerate(row, start=1):
            cell = ws9.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 5, 6, 9] else align_left
            if c_i in [3, 4, 7]:
                cell.alignment = align_wrap_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws9, min_width=12, max_width=45)
    ws9.column_dimensions["C"].width = 38
    ws9.column_dimensions["D"].width = 38
    ws9.column_dimensions["G"].width = 38

    # =========================================================================
    # 10. Sheet: Defects-Comments Data
    # =========================================================================
    ws10 = wb.create_sheet(title="Defects-Comments Data")
    ws10.views.sheetView[0].showGridLines = True

    ws10.merge_cells("A1:J1")
    t10 = ws10["A1"]
    t10.value = "RIMS - DEFECT LOG & COMMENTS REPOSITORY (10+ DEFECTS WITH ROOT CAUSE ANALYSIS)"
    t10.font = font_title
    t10.fill = fill_title
    t10.alignment = align_center
    ws10.row_dimensions[1].height = 36

    defect_headers = [
        "Defect ID", "Defect Title & Summary", "Associated Module", "Severity", "Priority",
        "Detected in Sprint", "Status", "Assigned Developer", "Root Cause Analysis", "Resolution Comments"
    ]
    style_header_row(ws10, 3, len(defect_headers))

    # 12 realistic defects (prompt requires 10+)
    defects_data = [
        ("DEF-001", "Duplicate SKU permitted when created without trimmed whitespace", "Catalog", "High", "P2", "Sprint 1", "Closed", "Pooja Desai", "String comparison in existsBySku did not trim leading/trailing spaces.", "Added @NotBlank and .trim() sanitization in ProductService.create()."),
        ("DEF-002", "Negative stock quantity allowed during manual inventory adjustment", "Inventory", "Critical", "P1", "Sprint 4", "Closed", "Neha Joshi", "Validation check for available stock balance was executed after subtraction.", "Added pre-adjustment guard throwing InsufficientStockException before commit."),
        ("DEF-003", "Inter-location transfer creates new stock level record without safety stock", "Inventory", "Medium", "P3", "Sprint 5", "Closed", "Neha Joshi", "StockLevel constructor in transfer destination branch lacked safetyStock default.", "Defaulted safety stock to 5 units in fallback creation branch."),
        ("DEF-004", "Sales order allocation succeeds when inventory count equals zero", "Orders", "Critical", "P1", "Sprint 6", "Closed", "Vikram Rao", "Condition used <= instead of < in available stock check.", "Refactored check to ensure available >= required quantity before reservation."),
        ("DEF-005", "Invalid order status transition from NEW to SHIPPED allowed via direct PUT", "Orders", "High", "P2", "Sprint 7", "Closed", "Vikram Rao", "State machine check omitted default switch branch rejection.", "Implemented strict state machine switch rejecting invalid progression with 400 error."),
        ("DEF-006", "Receiving purchase order does not update stock level for warehouse WH-002", "Supplier", "High", "P2", "Sprint 8", "Closed", "Amit Kulkarni", "Hardcoded destination location to WH-001 instead of PO's designated warehouse.", "Dynamically read warehouseLocation from ReceivePODTO payload."),
        ("DEF-007", "Barcode scanner lookup returns 500 error on special URL characters", "Barcode/RFID", "Medium", "P3", "Sprint 9", "Closed", "Rohan Gupta", "Path variable code was not URL-decoded before querying database.", "Added URI encoding handling and sanitized query parameter regex."),
        ("DEF-008", "Recharts sales trend chart renders blank when order count is zero", "Reporting", "Low", "P3", "Sprint 12", "Closed", "Ananya Iyer", "Zero values were converted to undefined instead of numeric 0.", "Normalized incoming JSON array data ensuring totalSales defaults to 0.00."),
        ("DEF-009", "VIEWER role is able to see 'Adjust Stock' button in UI", "User Mgmt", "Medium", "P2", "Sprint 13", "Closed", "Sneha Patel", "canWrite() helper was omitted from action button conditional rendering.", "Wrapped adjustment button with canWrite() conditional render."),
        ("DEF-010", "ERP product CSV import throws NullPointerException on empty description", "ERP Integration", "Medium", "P2", "Sprint 14", "Closed", "Amit Kulkarni", "CSV record parser did not check null on optional description column.", "Added default empty string fallback for optional text columns."),
        ("DEF-011", "Audit log records null principal when unauthenticated endpoint triggers error", "User Mgmt", "Low", "P3", "Sprint 13", "Verified", "Pooja Desai", "SecurityContextHolder returned anonymousUser string object.", "Captured 'ANONYMOUS' string literal when principal has no authenticated session."),
        ("DEF-012", "CORS header missing for React Vite dev server on localhost:5173", "Security", "High", "P1", "Sprint 1", "Closed", "Vikram Rao", "CorsConfiguration lacked wildcard port mapping for local frontend.", "Added allowedOriginPatterns for http://localhost:* and credentials support."),
    ]

    for idx, item in enumerate(defects_data, start=4):
        r = idx
        ws10.row_dimensions[r].height = 24
        for c_i, val in enumerate(item, start=1):
            cell = ws10.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 3, 4, 5, 6, 7, 8] else align_left
            if c_i in [2, 9, 10]:
                cell.alignment = align_wrap_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws10, min_width=10, max_width=45)
    ws10.column_dimensions["B"].width = 35
    ws10.column_dimensions["I"].width = 38
    ws10.column_dimensions["J"].width = 38

    # =========================================================================
    # 11. Sheet: Impediments Log
    # =========================================================================
    ws11 = wb.create_sheet(title="Impediments Log")
    ws11.views.sheetView[0].showGridLines = True

    ws11.merge_cells("A1:H1")
    t11 = ws11["A1"]
    t11.value = "RIMS - SCRUM IMPEDIMENTS & BLOCKERS LOG (5+ REALISTIC IMPEDIMENTS)"
    t11.font = font_title
    t11.fill = fill_title
    t11.alignment = align_center
    ws11.row_dimensions[1].height = 36

    imp_headers = ["Impediment ID", "Impediment Description", "Impact on Sprint / Squad", "Raised Date", "Owner", "Resolution Steps & Outcome", "Resolved Date", "Status"]
    style_header_row(ws11, 3, len(imp_headers))

    # 6 impediments (prompt requires 5+)
    impediments_data = [
        ("IMP-01", "AWS EKS Sandbox cluster quota limit reached, preventing deployment of QA test pods.", "Blocked QA automated integration testing in Sprint 2 for 3 days.", "18-Nov-26", "Karan Singhania", "Submitted AWS enterprise support ticket to double vCPU quota; temporarily pruned unneeded preview containers.", "21-Nov-26", "Resolved"),
        ("IMP-02", "Delays in customs clearance for sample Honeywell handheld barcode/RFID scanner hardware.", "Delayed physical scanner validation tests planned for Sprint 9.", "15-Feb-27", "Manmeet Jalota", "Escalated to XYZ Retail procurement expediter; configured virtual hardware emulator software in the interim.", "20-Feb-27", "Resolved"),
        ("IMP-03", "Firewall port 3306 blocked between Jenkins worker runner and Staging MySQL RDS subnet.", "Automated DB migration tests failed on staging pipeline execution.", "04-Jan-27", "Rajesh Nair", "Updated AWS VPC Security Group rules to authorize ingress on 3306 from Jenkins private IP CIDR block.", "05-Jan-27", "Resolved"),
        ("IMP-04", "Initial CSV seed script contained duplicate SKU entries causing DB unique constraint crashes.", "Backend startup test seed failed consistently in dev profile.", "06-Nov-26", "Pooja Desai", "Refactored DataSeeder.java with a deduplication set and automated SKU prefix generator.", "07-Nov-26", "Resolved"),
        ("IMP-05", "Missing field mapping specification for ERP JSON return orders schema from client IT.", "Blocked completion of Sprint 14 ERP returns adapter.", "05-May-27", "Arjun Mehta", "Conducted joint architecture workshop with client ERP architects; ratified signed schema contract document.", "08-May-27", "Resolved"),
        ("IMP-06", "Intermittent VPN disconnects for remote developers accessing Staging Kubernetes dashboards.", "Slowed down peer code reviews and UI validation during Sprint 10.", "12-Mar-27", "Priya Sharma", "Transitioned developer remote access to AWS Client VPN with split-tunnel routing and MFA tokens.", "14-Mar-27", "Resolved"),
    ]

    for idx, row in enumerate(impediments_data, start=4):
        ws11.row_dimensions[idx].height = 24
        for c_i, val in enumerate(row, start=1):
            cell = ws11.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 4, 5, 7, 8] else align_left
            if c_i in [2, 3, 6]:
                cell.alignment = align_wrap_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws11, min_width=12, max_width=45)
    ws11.column_dimensions["B"].width = 35
    ws11.column_dimensions["C"].width = 35
    ws11.column_dimensions["F"].width = 38

    # =========================================================================
    # 12. Sheet: Action Items
    # =========================================================================
    ws12 = wb.create_sheet(title="Action Items")
    ws12.views.sheetView[0].showGridLines = True

    ws12.merge_cells("A1:H1")
    t12 = ws12["A1"]
    t12.value = "RIMS - CONTINUOUS IMPROVEMENT ACTION ITEMS (8+ RETROSPECTIVE & REVIEW ACTIONS)"
    t12.font = font_title
    t12.fill = fill_title
    t12.alignment = align_center
    ws12.row_dimensions[1].height = 36

    act_headers = ["Action ID", "Originating Forum / Event", "Action Item Description", "Action Owner", "Due Date", "Priority", "Status", "Resolution Notes"]
    style_header_row(ws12, 3, len(act_headers))

    # 10 action items (prompt requires 8+)
    action_items_data = [
        ("ACT-001", "Sprint 1 Retrospective", "Standardize API response JSON error format across all REST controllers using GlobalExceptionHandler.", "Vikram Rao", "20-Nov-26", "High", "Completed", "Implemented unified ErrorResponse DTO with timestamp, status, error, and validationErrors map."),
        ("ACT-002", "Sprint 2 Retrospective", "Configure automated Maven pre-commit check for unit tests and code formatting to catch regressions early.", "Rajesh Nair", "04-Dec-26", "Medium", "Completed", "Configured git pre-commit hook running 'mvn test-compile' before commit push."),
        ("ACT-003", "Sprint 3 Review Meeting", "Add visual location code chips and stock health status badges on all frontend inventory tables.", "Sneha Patel", "18-Dec-26", "Medium", "Completed", "Added styled CSS badges (.badge-success, .badge-warning, .badge-danger) and location chips."),
        ("ACT-004", "Scrum of Scrums", "Align ERP JSON field nomenclature with client SAP dictionary to minimize data transformation overhead.", "Amit Kulkarni", "15-Jan-27", "High", "Completed", "Mapped ERP schema fields explicitly in ErpController using Jackson @JsonProperty mapping."),
        ("ACT-005", "Sprint 6 Retrospective", "Add automated integration tests verifying order allocation failure on insufficient stock.", "Meera Krishnan", "01-Feb-27", "High", "Completed", "Created Spring Boot MockMvc integration test in OrderControllerIntegrationTest.java."),
        ("ACT-006", "Sprint 8 Review Meeting", "Introduce supplier on-time delivery rate performance tier badges (Tier 1, Tier 2, Review) on PO tab.", "Ananya Iyer", "26-Feb-27", "Low", "Completed", "Implemented dynamic performance badges based on onTimeDeliveryRate percentage threshold."),
        ("ACT-007", "Mid-Project Architecture Review", "Implement database index on stock_levels(product_id, location) to accelerate high-frequency lookup queries.", "Suresh Menon", "12-Mar-27", "High", "Completed", "Added unique constraint and compound index on stock_levels table in Liquibase/Hibernate schema."),
        ("ACT-008", "Sprint 10 Retrospective", "Create reusable mock scan script to simulate rapid barcode scanner inputs during developer testing.", "Rohan Gupta", "25-Mar-27", "Medium", "Completed", "Delivered scan simulator utility script generating continuous EAN-13 and RFID codes."),
        ("ACT-009", "Security Governance Audit", "Verify all write endpoints enforce @PreAuthorize role guards and create audit log records.", "Pooja Desai", "23-Apr-27", "High", "Completed", "Audited 100% of mutation controller methods; verified Spring Security role checks and AuditLog entries."),
        ("ACT-010", "Sprint 14 Retrospective", "Prepare automated production deployment rollback runbook for Kubernetes Ingress canary release.", "Karan Singhania", "18-May-27", "High", "In Progress", "Drafted Jenkins rollback pipeline stage with automated healthcheck probe verification."),
    ]

    for idx, row in enumerate(action_items_data, start=4):
        ws12.row_dimensions[idx].height = 22
        for c_i, val in enumerate(row, start=1):
            cell = ws12.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 2, 4, 5, 6, 7] else align_left
            if c_i in [3, 8]:
                cell.alignment = align_wrap_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws12, min_width=12, max_width=45)
    ws12.column_dimensions["C"].width = 38
    ws12.column_dimensions["H"].width = 38

    # =========================================================================
    # 13. Sheet: Story Clarity Index
    # =========================================================================
    ws13 = wb.create_sheet(title="Story Clarity Index")
    ws13.views.sheetView[0].showGridLines = True

    ws13.merge_cells("A1:K1")
    t13 = ws13["A1"]
    t13.value = "RIMS - STORY CLARITY INDEX (INVEST READINESS EVALUATION, THRESHOLD >= 8.0)"
    t13.font = font_title
    t13.fill = fill_title
    t13.alignment = align_center
    ws13.row_dimensions[1].height = 36

    # Criteria Definition Table
    ws13.merge_cells("A3:K3")
    sec_sci1 = ws13["A3"]
    sec_sci1.value = "1. STORY READINESS ASSESSMENT CRITERIA & WEIGHTS (INVEST PRINCIPLES)"
    sec_sci1.font = font_section
    sec_sci1.fill = fill_section
    sec_sci1.alignment = align_left
    ws13.row_dimensions[3].height = 24

    sci_crit_headers = ["Criterion ID", "Readiness Evaluation Criteria", "Weight Factor", "Scoring Rule (Yes/No)", "Definition of Ready Expectation"]
    for c_i, h in enumerate(sci_crit_headers, start=1):
        cell = ws13.cell(row=4, column=c_i, value=h)
        cell.font = font_header
        cell.fill = fill_header
        cell.alignment = align_center
        cell.border = border_all
    ws13.row_dimensions[4].height = 22

    criteria_list = [
        ("C1", "Business Value Clearly Articulated", 1.5, "1 if Yes, 0 if No", "Clear benefit statement explaining commercial or operational impact"),
        ("C2", "User Persona & Role Identified", 1.0, "1 if Yes, 0 if No", "Explicit end-user persona identified (e.g. Store Manager, Cashier, Admin)"),
        ("C3", "Acceptance Criteria in Given-When-Then", 2.0, "1 if Yes, 0 if No", "Testable BDD-style Given-When-Then criteria covering primary success paths"),
        ("C4", "Edge Cases & Error Handling Defined", 1.5, "1 if Yes, 0 if No", "Explicit behavior for boundary cases, concurrency, and validation failures"),
        ("C5", "Technical & Data Dependencies Cleared", 1.5, "1 if Yes, 0 if No", "Database schema, external APIs, and prerequisite stories identified"),
        ("C6", "UI/UX Wireframe or Data Contract Ready", 1.0, "1 if Yes, 0 if No", "Visual layout mockups or JSON schema contracts reviewed by team"),
        ("C7", "Non-Functional & Security Defined", 1.5, "1 if Yes, 0 if No", "Latency targets, role permissions, and audit log expectations specified"),
    ]

    for idx, row in enumerate(criteria_list, start=5):
        ws13.row_dimensions[idx].height = 20
        for c_i, val in enumerate(row, start=1):
            cell = ws13.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 3, 4] else align_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    # Story Evaluation Table
    ws13.merge_cells("A13:K13")
    sec_sci2 = ws13["A13"]
    sec_sci2.value = "2. STORY EVALUATION MATRIX & READINESS STATUS (THRESHOLD: >= 8.0 OUT OF 10.0)"
    sec_sci2.font = font_section
    sec_sci2.fill = fill_section
    sec_sci2.alignment = align_left
    ws13.row_dimensions[13].height = 24

    eval_headers = ["Story ID", "User Story Title", "C1 (w:1.5)", "C2 (w:1.0)", "C3 (w:2.0)", "C4 (w:1.5)", "C5 (w:1.5)", "C6 (w:1.0)", "C7 (w:1.5)", "Weighted Score", "Sprint Readiness Status"]
    style_header_row(ws13, 14, len(eval_headers))

    # Evaluate 8 core stories
    eval_stories = [
        ("UST-R01", "Product Catalog CRUD, Search & Pagination", 1, 1, 1, 1, 1, 1, 1),
        ("UST-R02", "Inventory Real-Time Multi-Location View", 1, 1, 1, 1, 1, 1, 1),
        ("UST-R03", "Dynamic Low-Stock and Stockout Alerts", 1, 1, 1, 1, 1, 1, 1),
        ("UST-R04", "Sales Order Creation & Stock Allocation", 1, 1, 1, 1, 1, 1, 1),
        ("UST-R05", "Supplier PO Lifecycle & Goods Receipt", 1, 1, 1, 1, 1, 1, 1),
        ("UST-R06", "KPI Summary & Sales Trend Reporting", 1, 1, 1, 1, 1, 1, 1),
        ("UST-R07", "User Access Control & RBAC Endpoints", 1, 1, 1, 1, 1, 1, 1),
        ("UST-E01", "Bidirectional ERP Master Catalog Synchronization", 1, 1, 1, 1, 1, 1, 1),
    ]

    for idx, (sid, title, c1, c2, c3, c4, c5, c6, c7) in enumerate(eval_stories, start=15):
        r = idx
        ws13.row_dimensions[r].height = 22
        ws13.cell(row=r, column=1, value=sid).alignment = align_center
        ws13.cell(row=r, column=2, value=title).alignment = align_left
        ws13.cell(row=r, column=3, value=c1).alignment = align_center
        ws13.cell(row=r, column=4, value=c2).alignment = align_center
        ws13.cell(row=r, column=5, value=c3).alignment = align_center
        ws13.cell(row=r, column=6, value=c4).alignment = align_center
        ws13.cell(row=r, column=7, value=c5).alignment = align_center
        ws13.cell(row=r, column=8, value=c6).alignment = align_center
        ws13.cell(row=r, column=9, value=c7).alignment = align_center

        # Weighted Total Formula: =C*1.5 + D*1.0 + E*2.0 + F*1.5 + G*1.5 + H*1.0 + I*1.5
        ws13.cell(row=r, column=10, value=f"=C{r}*1.5 + D{r}*1.0 + E{r}*2.0 + F{r}*1.5 + G{r}*1.5 + H{r}*1.0 + I{r}*1.5").alignment = align_center
        ws13.cell(row=r, column=10).number_format = "0.0"
        ws13.cell(row=r, column=10).font = font_bold

        # Status Formula: =IF(J{r}>=8.0, "READY FOR SPRINT", "NEEDS REFINEMENT")
        ws13.cell(row=r, column=11, value=f'=IF(J{r}>=8.0, "READY FOR SPRINT", "NEEDS REFINEMENT")').alignment = align_center

        for c in range(1, 12):
            cell = ws13.cell(row=r, column=c)
            cell.font = font_regular
            cell.border = border_all
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws13, min_width=10, max_width=45)
    ws13.column_dimensions["B"].width = 38
    ws13.column_dimensions["K"].width = 24

    # =========================================================================
    # 14. Sheet: Metrics
    # =========================================================================
    ws14 = wb.create_sheet(title="Metrics")
    ws14.views.sheetView[0].showGridLines = True

    ws14.merge_cells("A1:I1")
    t14 = ws14["A1"]
    t14.value = "RIMS - AGILE & OPERATIONAL PERFORMANCE METRICS (STRATEGIC, TACTICAL & OPERATIONAL)"
    t14.font = font_title
    t14.fill = fill_title
    t14.alignment = align_center
    ws14.row_dimensions[1].height = 36

    metrics_headers = [
        "Metric ID", "Category Tier", "Metric Name", "Baseline", "Current Value",
        "Target Value", "Measurement Frequency", "Calculation Method / Formula", "Status Indicator"
    ]
    style_header_row(ws14, 3, len(metrics_headers))

    # Realistic metrics across Strategic, Tactical, Operational
    metrics_data = [
        # Strategic Metrics
        ("MET-01", "Strategic", "Inventory Accuracy Rate", "81.4%", "97.8%", ">= 98.0%", "Monthly Cycle Count", "(Matched Items / Total Audited Items) x 100", "On Track"),
        ("MET-02", "Strategic", "Stockout Incident Rate", "14.2%", "3.2%", "<= 3.5% (-25% relative)", "Weekly Audit", "(Stockout Events / Total Active SKUs) x 100", "Target Achieved"),
        ("MET-03", "Strategic", "Overstock Capital Reduction", "$180,000", "$138,500", "<= $144,000 (-20%)", "Monthly Financial Close", "Total valuation of stock exceeding max threshold", "Target Achieved"),
        ("MET-04", "Strategic", "Order Fulfilment Rate", "78.5%", "95.6%", ">= 95.0%", "Daily Automated Report", "(On-time Delivered Orders / Total Orders) x 100", "Target Achieved"),
        ("MET-05", "Strategic", "Production System Uptime", "98.2%", "99.95%", ">= 99.9% (3 Nines)", "Continuous Real-time", "(Total Time - Unplanned Downtime) / Total Time", "Exceeded"),

        # Tactical Metrics
        ("MET-06", "Tactical", "Sprint Velocity Predictability", "65.0%", "92.5%", "90% - 110%", "Per Sprint (2-week)", "(Actual Points Burned / Committed Points) x 100", "Healthy"),
        ("MET-07", "Tactical", "Defect Leakage to UAT / Staging", "18.5%", "3.8%", "<= 5.0%", "Per Release Gate", "(Defects Escaped to UAT / Total Defects) x 100", "Healthy"),
        ("MET-08", "Tactical", "Automated Code Test Coverage", "45.0%", "87.4%", ">= 85.0%", "Per CI Build (Jenkins)", "Line & Branch Coverage via Jacoco / Vitest", "Target Achieved"),
        ("MET-09", "Tactical", "Pull Request Cycle Time", "48 hours", "14 hours", "<= 24 hours", "Weekly Git Metric", "Time from PR submission to successful merge", "Healthy"),

        # Operational Metrics
        ("MET-10", "Operational", "Barcode / RFID Scan Latency", "1,200 ms", "145 ms", "<= 250 ms", "Continuous Actuator", "Mean REST response time on /api/scan/{code}", "Exceeded"),
        ("MET-11", "Operational", "Mean Time to Recovery (MTTR)", "180 min", "12 min", "<= 15 min", "Per Production Incident", "Average downtime before service restoration", "Healthy"),
        ("MET-12", "Operational", "Supplier On-Time Delivery Rate", "68.0%", "89.2%", ">= 88.0%", "Monthly Vendor Review", "(On-time PO Deliveries / Total Received POs) x 100", "Target Achieved"),
        ("MET-13", "Operational", "Inventory Adjustment Ratio", "6.2%", "1.4%", "<= 2.0%", "Weekly Audit", "(Manual Stock Adjustments / Total Stock Moves) x 100", "Healthy"),
    ]

    for idx, row in enumerate(metrics_data, start=4):
        ws14.row_dimensions[idx].height = 22
        for c_i, val in enumerate(row, start=1):
            cell = ws14.cell(row=idx, column=c_i, value=val)
            cell.font = font_regular
            cell.border = border_all
            cell.alignment = align_center if c_i in [1, 2, 4, 5, 6, 7, 9] else align_left
            if c_i in [3, 8]:
                cell.alignment = align_wrap_left
            if idx % 2 == 1:
                cell.fill = fill_zebra

    auto_fit_columns(ws14, min_width=12, max_width=45)
    ws14.column_dimensions["C"].width = 30
    ws14.column_dimensions["H"].width = 38

    # Save to disk
    output_path = r"d:\Retail project\RIMS\docs\RIMS_Agile_Workbook.xlsx"
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    wb.save(output_path)
    print(f"Workbook successfully saved to: {output_path}")

if __name__ == "__main__":
    create_rims_workbook()
