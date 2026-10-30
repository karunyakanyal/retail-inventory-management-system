package com.hcl.rims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.rims.dto.*;
import com.hcl.rims.model.*;
import com.hcl.rims.repo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RimsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private StockLevelRepository stockLevelRepository;

    @Autowired
    private StockTransferRepository stockTransferRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Context Loads and Initial Seeding Verified")
    void contextLoadsAndSeeding() {
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(productRepository.count()).isGreaterThanOrEqualTo(30);
        assertThat(categoryRepository.count()).isEqualTo(5);
        assertThat(warehouseRepository.count()).isEqualTo(3);
        assertThat(storeRepository.count()).isEqualTo(5);
        assertThat(supplierRepository.count()).isEqualTo(5);

        String[] usernames = {"admin", "manager", "staff", "viewer"};
        for (String username : usernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            assertThat(userOpt).isPresent();
            assertThat(passwordEncoder.matches("Pass@123", userOpt.get().getPassword())).isTrue();
        }
    }

    @Test
    @DisplayName("Scan endpoint matches barcode and RFID tag")
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void testScanEndpoint() throws Exception {
        Product p = productRepository.findBySku("SKU-1001").orElseThrow();

        // Scan using barcode
        mockMvc.perform(get("/api/scan/" + p.getBarcode()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("SKU-1001"))
            .andExpect(jsonPath("$.barcode").value(p.getBarcode()));

        // Scan using RFID tag
        mockMvc.perform(get("/api/scan/" + p.getRfidTag()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("SKU-1001"))
            .andExpect(jsonPath("$.rfidTag").value(p.getRfidTag()));

        // Scan non-existent code returns 404
        mockMvc.perform(get("/api/scan/UNKNOWN_CODE_999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Search & Filter products with pagination")
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void testSearchAndFilterProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("name", "Headphones")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.content[0].name", containsString("Headphones")));

        // Filter by price range
        mockMvc.perform(get("/api/products")
                .param("minPrice", "100.00")
                .param("maxPrice", "250.00")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", not(empty())));
    }

    @Test
    @DisplayName("Category CRUD with MANAGER role and AuditLog entry")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testCategoryCrud() throws Exception {
        long initialLogs = auditLogRepository.count();

        CategoryDTO catDTO = new CategoryDTO(null, "Outdoor Gear " + System.currentTimeMillis(), "Tents, sleeping bags, and camping tools");
        String response = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(catDTO.getName()))
            .andReturn().getResponse().getContentAsString();

        CategoryDTO created = objectMapper.readValue(response, CategoryDTO.class);
        assertThat(created.getId()).isNotNull();

        // Update Category
        created.setDescription("Updated outdoor gear description");
        mockMvc.perform(put("/api/categories/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(created)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated outdoor gear description"));

        // Delete Category
        mockMvc.perform(delete("/api/categories/" + created.getId()))
            .andExpect(status().isNoContent());

        assertThat(auditLogRepository.count()).isGreaterThan(initialLogs);
    }

    @Test
    @DisplayName("Product CRUD with MANAGER role and AuditLog")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testProductCrud() throws Exception {
        String sku = "SKU-TEST-" + System.currentTimeMillis();
        ProductDTO newProd = new ProductDTO(
            null,
            sku,
            "Test High Precision Scale",
            "BAR-" + System.currentTimeMillis(),
            "RFID-" + System.currentTimeMillis(),
            "Digital scale for inventory weighing",
            new BigDecimal("79.50"),
            new BigDecimal("40.00"),
            1L,
            null,
            true
        );

        String createRes = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProd)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value(sku))
            .andReturn().getResponse().getContentAsString();

        ProductDTO created = objectMapper.readValue(createRes, ProductDTO.class);

        // Update product
        created.setPrice(new BigDecimal("84.99"));
        mockMvc.perform(put("/api/products/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(created)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(84.99));

        // Delete product
        mockMvc.perform(delete("/api/products/" + created.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("CSV Export and Bulk Import")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testCsvExportAndImport() throws Exception {
        // Export CSV
        mockMvc.perform(get("/api/products/export"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"))
            .andExpect(content().string(containsString("SKU-1001")));

        // Bulk Import CSV
        String csvContent = "SKU,Name,Barcode,RFID_Tag,Description,Price,Cost_Price,Category\n" +
            "SKU-CSV-100,CSV Imported Product 1,111222333444,RFID-CSV-1,Imported via test,45.00,20.00,Electronics\n" +
            "SKU-CSV-101,CSV Imported Product 2,111222333445,RFID-CSV-2,Imported via test 2,55.00,25.00,Electronics\n";

        MockMultipartFile file = new MockMultipartFile(
            "file", "products.csv", "text/csv", csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/products/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.importedCount").value(2));

        assertThat(productRepository.findBySku("SKU-CSV-100")).isPresent();
        assertThat(productRepository.findBySku("SKU-CSV-101")).isPresent();
    }

    @Test
    @DisplayName("Inventory Stock Adjust with Reason & AuditLog")
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testInventoryStockAdjust() throws Exception {
        Product p = productRepository.findBySku("SKU-1001").orElseThrow();
        String location = "WH-001";

        StockAdjustmentDTO adjustDTO = new StockAdjustmentDTO(
            p.getId(),
            location,
            15,
            "Cycle count discrepancy replenishment"
        );

        mockMvc.perform(post("/api/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.qty").isNumber());

        List<AuditLog> logs = auditLogRepository.findByEntityNameOrderByTimestampDesc("StockLevel");
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getDetails()).contains("Cycle count discrepancy replenishment");
    }

    @Test
    @DisplayName("Stock Transfer Between Locations (Valid and Insufficient Qty)")
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testStockTransfer() throws Exception {
        Product p = productRepository.findBySku("SKU-1002").orElseThrow();
        String source = "WH-002";
        String dest = "STR-102";

        // Seed initial source stock
        StockAdjustmentDTO setInitial = new StockAdjustmentDTO(p.getId(), source, 50, "Initial transfer buffer");
        mockMvc.perform(post("/api/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(setInitial)))
            .andExpect(status().isOk());

        // Valid transfer of 10 units
        StockTransferRequestDTO transferReq = new StockTransferRequestDTO(
            p.getId(), source, dest, 10, "Stock rebalance"
        );

        mockMvc.perform(post("/api/inventory/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.quantity").value(10));

        // Attempt transfer exceeding available stock -> 400 Bad Request / Insufficient Stock
        StockTransferRequestDTO excessiveReq = new StockTransferRequestDTO(
            p.getId(), source, dest, 99999, "Over limit transfer"
        );

        mockMvc.perform(post("/api/inventory/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(excessiveReq)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("Low-stock alert appears after adjustment")
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testLowStockAlertAppearsAfterAdjustment() throws Exception {
        Product p = productRepository.findBySku("SKU-1003").orElseThrow();
        String loc = "WH-003";

        StockLevel stock = stockLevelRepository.findByProductIdAndLocation(p.getId(), loc)
            .orElseGet(() -> stockLevelRepository.save(new StockLevel(p, loc, 50, 10, 5)));

        int currentQty = stock.getQty();
        // Adjust stock down so that qty <= reorderPoint
        int targetQty = 2;
        int delta = targetQty - currentQty;

        StockAdjustmentDTO adjustDto = new StockAdjustmentDTO(
            p.getId(),
            loc,
            delta,
            "Depleting stock below reorder point for alert test"
        );

        mockMvc.perform(post("/api/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.qty").value(2));

        // Low-stock alert appears in endpoint
        mockMvc.perform(get("/api/inventory/low-stock"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())))
            .andExpect(jsonPath("$[?(@.product.id == " + p.getId() + " && @.location == '" + loc + "')].qty")
                .value(hasItem(2)));
    }

    @Test
    @DisplayName("Order Lifecycle: Create, Allocate Stock, State Machine, Cancel, Return")
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testOrderLifecycle() throws Exception {
        Product p = productRepository.findBySku("SKU-1004").orElseThrow();
        String loc = "WH-001";

        // Seed stock at WH-001
        StockAdjustmentDTO adjust = new StockAdjustmentDTO(p.getId(), loc, 100, "Seed for order test");
        mockMvc.perform(post("/api/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjust)))
            .andExpect(status().isOk());

        // 1. Create order
        SalesOrderDTO orderDto = new SalesOrderDTO();
        orderDto.setOrderNumber("SO-TEST-" + System.currentTimeMillis());
        orderDto.setLocation(loc);
        OrderLineDTO line = new OrderLineDTO(null, p.getId(), p.getSku(), p.getName(), 5, p.getPrice(), null);
        orderDto.setLines(List.of(line));

        String createRes = mockMvc.perform(post("/api/orders/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("NEW"))
            .andReturn().getResponse().getContentAsString();

        SalesOrderDTO created = objectMapper.readValue(createRes, SalesOrderDTO.class);
        Long orderId = created.getId();

        // 2. Allocate stock
        mockMvc.perform(post("/api/orders/sales/" + orderId + "/allocate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ALLOCATED"))
            .andExpect(jsonPath("$.stockAllocated").value(true));

        // 3. Test invalid transition: ALLOCATED -> SHIPPED should fail
        OrderStatusUpdateDTO invalidTransition = new OrderStatusUpdateDTO(SalesOrder.OrderStatus.SHIPPED, "Skip steps");
        mockMvc.perform(put("/api/orders/sales/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTransition)))
            .andExpect(status().isBadRequest());

        // 4. Valid flow: ALLOCATED -> PICKED -> PACKED -> SHIPPED
        OrderStatusUpdateDTO pickDto = new OrderStatusUpdateDTO(SalesOrder.OrderStatus.PICKED, "Picked from bin");
        mockMvc.perform(put("/api/orders/sales/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pickDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PICKED"));

        OrderStatusUpdateDTO packDto = new OrderStatusUpdateDTO(SalesOrder.OrderStatus.PACKED, "Packed into box");
        mockMvc.perform(put("/api/orders/sales/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(packDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PACKED"));

        OrderStatusUpdateDTO shipDto = new OrderStatusUpdateDTO(SalesOrder.OrderStatus.SHIPPED, "Carrier dispatched");
        mockMvc.perform(put("/api/orders/sales/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shipDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SHIPPED"));

        // 5. Return order and verify restock
        int qtyBeforeReturn = stockLevelRepository.findByProductIdAndLocation(p.getId(), loc).orElseThrow().getQty();
        ReturnOrderDTO returnDto = new ReturnOrderDTO(loc, "Customer returned product", null);
        mockMvc.perform(post("/api/orders/sales/" + orderId + "/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RETURNED"));

        int qtyAfterReturn = stockLevelRepository.findByProductIdAndLocation(p.getId(), loc).orElseThrow().getQty();
        assertThat(qtyAfterReturn).isEqualTo(qtyBeforeReturn + 5);

        // 6. Test cancellation restoring allocated stock
        SalesOrderDTO cancelOrderDto = new SalesOrderDTO();
        cancelOrderDto.setOrderNumber("SO-CANCEL-" + System.currentTimeMillis());
        cancelOrderDto.setLocation(loc);
        cancelOrderDto.setLines(List.of(new OrderLineDTO(null, p.getId(), p.getSku(), p.getName(), 3, p.getPrice(), null)));

        String cancelRes = mockMvc.perform(post("/api/orders/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelOrderDto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        SalesOrderDTO toCancel = objectMapper.readValue(cancelRes, SalesOrderDTO.class);

        // Allocate
        mockMvc.perform(post("/api/orders/sales/" + toCancel.getId() + "/allocate"))
            .andExpect(status().isOk());

        // Cancel
        mockMvc.perform(post("/api/orders/sales/" + toCancel.getId() + "/cancel?reason=ChangedMind"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.stockAllocated").value(false));
    }

    @Test
    @DisplayName("Order allocation failing on insufficient stock")
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testOrderAllocationFailingOnInsufficientStock() throws Exception {
        Product p = productRepository.findBySku("SKU-1008").orElseThrow();
        String loc = "WH-001";

        // Create an order with excessive quantity
        SalesOrderDTO orderDto = new SalesOrderDTO();
        orderDto.setOrderNumber("SO-EXCESS-" + System.currentTimeMillis());
        orderDto.setLocation(loc);
        OrderLineDTO line = new OrderLineDTO(null, p.getId(), p.getSku(), p.getName(), 999999, p.getPrice(), null);
        orderDto.setLines(List.of(line));

        String res = mockMvc.perform(post("/api/orders/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        SalesOrderDTO created = objectMapper.readValue(res, SalesOrderDTO.class);

        // Attempt allocation which must fail due to insufficient stock
        mockMvc.perform(post("/api/orders/sales/" + created.getId() + "/allocate"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));

        SalesOrder orderInDb = salesOrderRepository.findById(created.getId()).orElseThrow();
        assertThat(orderInDb.getStatus()).isEqualTo(SalesOrder.OrderStatus.NEW);
        assertThat(orderInDb.isStockAllocated()).isFalse();
    }

    @Test
    @DisplayName("Suppliers CRUD, Create PO, Receive PO, and Supplier Performance")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testSuppliersAndPurchaseOrders() throws Exception {
        // 1. Supplier CRUD
        SupplierDTO newSup = new SupplierDTO(null, "Global Tech Hub " + System.currentTimeMillis(), "Alice Smith", "alice@globaltech.com", "555-8899", "77 Innovation Way", "Austin", "TX");
        String supRes = mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSup)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        SupplierDTO createdSup = objectMapper.readValue(supRes, SupplierDTO.class);
        Long supId = createdSup.getId();

        // 2. Create Purchase Order
        Product p = productRepository.findBySku("SKU-1005").orElseThrow();
        String poNumber = "PO-TEST-" + System.currentTimeMillis();
        PurchaseOrderDTO poDto = new PurchaseOrderDTO();
        poDto.setPoNumber(poNumber);
        poDto.setSupplierId(supId);
        poDto.setWarehouseLocation("WH-001");
        poDto.setExpectedDate(LocalDate.now().plusDays(5));
        POLineDTO poLine = new POLineDTO(null, p.getId(), p.getSku(), p.getName(), 25, p.getCostPrice(), null);
        poDto.setLines(List.of(poLine));

        String poRes = mockMvc.perform(post("/api/orders/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(poDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ORDERED"))
            .andReturn().getResponse().getContentAsString();

        PurchaseOrderDTO createdPo = objectMapper.readValue(poRes, PurchaseOrderDTO.class);

        // 3. Receive Purchase Order
        int qtyBefore = stockLevelRepository.findByProductIdAndLocation(p.getId(), "WH-001")
            .map(StockLevel::getQty).orElse(0);

        ReceivePODTO receiveDto = new ReceivePODTO("WH-001", LocalDate.now(), "Received in good condition");
        mockMvc.perform(post("/api/orders/purchase/" + createdPo.getId() + "/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiveDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECEIVED"))
            .andExpect(jsonPath("$.receivedDate").isNotEmpty());

        int qtyAfter = stockLevelRepository.findByProductIdAndLocation(p.getId(), "WH-001").orElseThrow().getQty();
        assertThat(qtyAfter).isEqualTo(qtyBefore + 25);

        // 4. Supplier Performance Endpoint
        mockMvc.perform(get("/api/suppliers/" + supId + "/performance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPOs").value(1))
            .andExpect(jsonPath("$.receivedPOs").value(1))
            .andExpect(jsonPath("$.onTimeDeliveryRate").value(100.0));
    }

    @Test
    @DisplayName("Reports: KPI Summary, Sales Trends, and CSV Exports")
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void testReports() throws Exception {
        // KPI Summary
        mockMvc.perform(get("/api/reports/kpi"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalStockValue").isNumber())
            .andExpect(jsonPath("$.totalStockItems").isNumber())
            .andExpect(jsonPath("$.orderFulfilmentRate").isNumber());

        // Sales Trends
        mockMvc.perform(get("/api/reports/sales-trends"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(List.class)));

        // Inventory CSV export
        mockMvc.perform(get("/api/reports/export/inventory"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"))
            .andExpect(content().string(containsString("Product_ID,SKU")));

        // Orders CSV export
        mockMvc.perform(get("/api/reports/export/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"))
            .andExpect(content().string(containsString("Order_Number,Customer")));
    }

    @Test
    @DisplayName("User Management: Admin Deactivate User and Assign Role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserDeactivateAndAssignRole() throws Exception {
        UserDTO newUser = new UserDTO(null, "tempstaff_" + System.currentTimeMillis(), "Temp Staff", "temp@rims.com", Role.ROLE_STAFF, true);
        newUser.setPassword("Pass@123");

        String res = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        UserDTO created = objectMapper.readValue(res, UserDTO.class);
        Long uid = created.getId();

        // Deactivate user
        mockMvc.perform(patch("/api/users/" + uid + "/deactivate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));

        // Assign role
        mockMvc.perform(patch("/api/users/" + uid + "/role?role=ROLE_MANAGER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ROLE_MANAGER"));
    }

    @Test
    @DisplayName("ERP Integration: Export and Import for Products, Stock, and Orders")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testErpIntegration() throws Exception {
        // 1. Products ERP
        mockMvc.perform(get("/api/erp/products/export?format=json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/erp/products/export?format=csv"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"));

        // 2. Stock ERP
        mockMvc.perform(get("/api/erp/stock/export?format=json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/erp/stock/export?format=csv"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"));

        // 3. Orders ERP
        mockMvc.perform(get("/api/erp/orders/export?format=json"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/erp/orders/export?format=csv"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv"));

        // 4. ERP Import JSON for stock
        Product p = productRepository.findBySku("SKU-1006").orElseThrow();
        StockLevelDTO stockDto = new StockLevelDTO(null, p.getId(), p.getSku(), p.getName(), "WH-001", 123, 15, 10, null);
        mockMvc.perform(post("/api/erp/stock/import/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(stockDto))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.importedCount").value(1));

        StockLevel updatedStock = stockLevelRepository.findByProductIdAndLocation(p.getId(), "WH-001").orElseThrow();
        assertThat(updatedStock.getQty()).isEqualTo(123);
    }

    @Test
    @DisplayName("Role Rules: VIEWER is read-only, cannot write (403 Forbidden)")
    @WithMockUser(username = "viewer", roles = {"VIEWER"})
    void testViewerRoleReadOnly() throws Exception {
        // GET allowed
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk());

        // POST denied (403 Forbidden)
        ProductDTO newProd = new ProductDTO(null, "SKU-FAIL", "Viewer Write", null, null, null, BigDecimal.TEN, BigDecimal.ONE, 1L, null, true);
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProd)))
            .andExpect(status().isForbidden());

        // Stock adjust denied for VIEWER
        StockAdjustmentDTO adjust = new StockAdjustmentDTO(1L, "WH-001", 5, "Viewer adjust");
        mockMvc.perform(post("/api/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjust)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Role Rules: STAFF can adjust/transfer, cannot manage catalog or users")
    @WithMockUser(username = "staff", roles = {"STAFF"})
    void testStaffRolePermissions() throws Exception {
        // STAFF cannot modify catalog
        ProductDTO newProd = new ProductDTO(null, "SKU-FAIL2", "Staff Catalog", null, null, null, BigDecimal.TEN, BigDecimal.ONE, 1L, null, true);
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProd)))
            .andExpect(status().isForbidden());

        // STAFF cannot access user management
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HTTP Basic Authentication: 401 without login, 200 with credentials, 401 with wrong credentials")
    void testHttpBasicAuth() throws Exception {
        // 401 without login
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isUnauthorized());

        // 200 with valid credentials
        mockMvc.perform(get("/api/products").with(httpBasic("viewer", "Pass@123")))
            .andExpect(status().isOk());

        // 401 with invalid credentials
        mockMvc.perform(get("/api/products").with(httpBasic("viewer", "WrongPass")))
            .andExpect(status().isUnauthorized());
    }
}
