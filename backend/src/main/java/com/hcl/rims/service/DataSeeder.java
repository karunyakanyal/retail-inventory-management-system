package com.hcl.rims.service;

import com.hcl.rims.model.*;
import com.hcl.rims.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransferRepository stockTransferRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            StoreRepository storeRepository,
            StockLevelRepository stockLevelRepository,
            StockTransferRepository stockTransferRepository,
            CustomerRepository customerRepository,
            SalesOrderRepository salesOrderRepository,
            SupplierRepository supplierRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.storeRepository = storeRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.stockTransferRepository = stockTransferRepository;
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        seedUsers();
        List<Category> categories = seedCategories();
        List<Warehouse> warehouses = seedWarehouses();
        List<Store> stores = seedStores();
        List<Supplier> suppliers = seedSuppliers();
        List<Product> products = seedProducts(categories);
        seedStockLevels(products, warehouses, stores);
        List<Customer> customers = seedCustomers();
        seedOrders(customers, products, suppliers);
        seedTransfers(products, warehouses, stores);
        seedAuditLogs();
    }

    private void seedUsers() {
        String encodedPass = passwordEncoder.encode("Pass@123");
        userRepository.save(new User("admin", encodedPass, "Administrator", "admin@rims.com", Role.ROLE_ADMIN));
        userRepository.save(new User("manager", encodedPass, "Operations Manager", "manager@rims.com", Role.ROLE_MANAGER));
        userRepository.save(new User("staff", encodedPass, "Inventory Staff", "staff@rims.com", Role.ROLE_STAFF));
        userRepository.save(new User("viewer", encodedPass, "System Auditor", "viewer@rims.com", Role.ROLE_VIEWER));
    }

    private List<Category> seedCategories() {
        List<Category> list = List.of(
            new Category("Electronics", "Smart gadgets, computers, and electronic accessories"),
            new Category("Apparel", "Men and Women casual and formal clothing"),
            new Category("Home & Kitchen", "Appliances, cookware, and home decoration"),
            new Category("Groceries", "Packaged foods, beverages, and pantry items"),
            new Category("Sports & Fitness", "Gym equipment, outdoor gear, and athletic wear")
        );
        return categoryRepository.saveAll(list);
    }

    private List<Warehouse> seedWarehouses() {
        List<Warehouse> list = List.of(
            new Warehouse("WH-001", "Central Distribution Center", "100 Logistics Blvd", "Dallas", "TX", "75001", 100000),
            new Warehouse("WH-002", "East Coast Regional Hub", "250 Harbor Way", "Newark", "NJ", "07101", 75000),
            new Warehouse("WH-003", "West Coast Logistics Center", "450 Pacific Coast Hwy", "Long Beach", "CA", "90802", 85000)
        );
        return warehouseRepository.saveAll(list);
    }

    private List<Store> seedStores() {
        List<Store> list = List.of(
            new Store("STR-101", "Downtown Flagship", "500 5th Avenue", "New York", "NY", "10018", "212-555-0101"),
            new Store("STR-102", "Suburban Plaza", "1200 Galleria Mall Rd", "Houston", "TX", "77056", "713-555-0102"),
            new Store("STR-103", "Metro Center", "750 Market Street", "San Francisco", "CA", "94103", "415-555-0103"),
            new Store("STR-104", "Lakeside Mall", "300 Michigan Ave", "Chicago", "IL", "60601", "312-555-0104"),
            new Store("STR-105", "Sunnyvale Galleria", "880 El Camino Real", "Sunnyvale", "CA", "94087", "408-555-0105")
        );
        return storeRepository.saveAll(list);
    }

    private List<Supplier> seedSuppliers() {
        List<Supplier> list = List.of(
            new Supplier("Apex Tech Supplies", "John Doe", "contact@apextech.com", "800-555-1101", "100 Silicon Way", "San Jose", "CA"),
            new Supplier("Prime Apparel Global", "Sarah Connor", "orders@primeapparel.com", "800-555-1102", "200 Fashion Ave", "New York", "NY"),
            new Supplier("Nordic Living Goods", "Emma Watson", "support@nordicliving.com", "800-555-1103", "400 Timber Ln", "Portland", "OR"),
            new Supplier("Harvest Foods Group", "Robert Smith", "sales@harvestfoods.com", "800-555-1104", "300 Orchard Rd", "Yakima", "WA"),
            new Supplier("Summit Athletics Co", "Marcus Vance", "team@summitathletics.com", "800-555-1105", "500 Mountain Pass", "Denver", "CO")
        );
        return supplierRepository.saveAll(list);
    }

    private List<Product> seedProducts(List<Category> categories) {
        Category elec = categories.get(0);
        Category appar = categories.get(1);
        Category home = categories.get(2);
        Category groc = categories.get(3);
        Category sport = categories.get(4);

        List<Product> list = new ArrayList<>();
        // 30 distinct products
        String[][] data = {
            {"Wireless Noise-Cancelling Headphones", "199.99", "120.00", "0"},
            {"Smart Watch Series 5", "299.99", "180.00", "0"},
            {"Ultra HD 4K Monitor 27-inch", "349.99", "220.00", "0"},
            {"Ergonomic Mechanical Keyboard", "89.99", "50.00", "0"},
            {"Wireless Optical Gaming Mouse", "49.99", "25.00", "0"},
            {"USB-C Fast Charging Hub", "39.99", "18.00", "0"},

            {"Classic Denim Jacket", "79.99", "40.00", "1"},
            {"Slim Fit Cotton Chinos", "49.99", "22.00", "1"},
            {"Merino Wool Crewneck Sweater", "89.99", "45.00", "1"},
            {"Waterproof Hooded Raincoat", "119.99", "60.00", "1"},
            {"Breathable Running Shoes", "99.99", "55.00", "1"},
            {"Polarized Aviator Sunglasses", "59.99", "25.00", "1"},

            {"Stainless Steel Cookware Set", "149.99", "85.00", "2"},
            {"Smart Air Purifier HEPA", "129.99", "70.00", "2"},
            {"Compact Espresso Machine", "179.99", "100.00", "2"},
            {"Cast Iron Skillet 12-inch", "39.99", "20.00", "2"},
            {"Memory Foam Ergonomic Pillow", "49.99", "22.00", "2"},
            {"Ceramic Dinnerware 16-piece", "69.99", "35.00", "2"},

            {"Organic Single-Origin Coffee Beans 1kg", "24.99", "12.00", "3"},
            {"Artisan Olive Oil Extra Virgin 500ml", "18.99", "9.00", "3"},
            {"Wildflower Pure Honey 500g", "12.99", "6.50", "3"},
            {"Whole Grain Granola Clusters", "8.99", "4.00", "3"},
            {"Sparkling Mineral Water 12-pack", "15.99", "7.50", "3"},
            {"Dark Chocolate 85% Bar 100g", "4.99", "2.00", "3"},

            {"Yoga Mat Non-Slip 6mm", "34.99", "16.00", "4"},
            {"Adjustable Dumbbell Set 50lbs", "199.99", "110.00", "4"},
            {"Resistance Exercise Bands 5-pack", "19.99", "8.00", "4"},
            {"Stainless Steel Insulated Water Bottle", "29.99", "12.00", "4"},
            {"Trail Running Hydration Backpack", "64.99", "32.00", "4"},
            {"Folding Camping Chair Lightweight", "44.99", "20.00", "4"}
        };

        Category[] catMap = {elec, appar, home, groc, sport};

        for (int i = 0; i < data.length; i++) {
            int num = i + 1;
            String sku = String.format("SKU-%04d", 1000 + num);
            String barcode = String.format("89012345%04d", 1000 + num);
            String rfid = String.format("RFID-E20000%04d", 1000 + num);
            String name = data[i][0];
            BigDecimal price = new BigDecimal(data[i][1]);
            BigDecimal cost = new BigDecimal(data[i][2]);
            int catIdx = Integer.parseInt(data[i][3]);
            Category category = catMap[catIdx];

            Product p = new Product(sku, name, barcode, rfid, name + " - high quality retail inventory item.", price, cost, category);
            list.add(p);
        }

        return productRepository.saveAll(list);
    }

    private void seedStockLevels(List<Product> products, List<Warehouse> warehouses, List<Store> stores) {
        List<StockLevel> levels = new ArrayList<>();
        int count = 0;
        for (Product product : products) {
            // Stock in primary warehouse
            Warehouse wh = warehouses.get(count % warehouses.size());
            levels.add(new StockLevel(product, wh.getCode(), 150 + (count * 5), 25, 15));

            // Stock in primary store
            Store st = stores.get(count % stores.size());
            levels.add(new StockLevel(product, st.getCode(), 35 + (count * 2), 10, 5));
            count++;
        }
        stockLevelRepository.saveAll(levels);
    }

    private List<Customer> seedCustomers() {
        List<Customer> list = List.of(
            new Customer("Alice Johnson", "alice@example.com", "555-0111", "12 Oak Street", "Austin", "TX", "78701"),
            new Customer("Bob Martinez", "bob.m@example.com", "555-0122", "34 Maple Ave", "Seattle", "WA", "98101"),
            new Customer("Charlie Davis", "charlie.d@example.com", "555-0133", "78 Pine Lane", "Boston", "MA", "02108"),
            new Customer("Diana Prince", "diana.p@example.com", "555-0144", "99 Cedar Blvd", "Denver", "CO", "80202"),
            new Customer("Ethan Hunt", "ethan.h@example.com", "555-0155", "101 Elm Court", "Atlanta", "GA", "30303")
        );
        return customerRepository.saveAll(list);
    }

    private void seedOrders(List<Customer> customers, List<Product> products, List<Supplier> suppliers) {
        // Sales Orders
        SalesOrder so1 = new SalesOrder("SO-2026-001", customers.get(0), SalesOrder.OrderStatus.NEW);
        so1.addLine(new OrderLine(so1, products.get(0), 2, products.get(0).getPrice()));
        so1.addLine(new OrderLine(so1, products.get(3), 1, products.get(3).getPrice()));
        salesOrderRepository.save(so1);

        SalesOrder so2 = new SalesOrder("SO-2026-002", customers.get(1), SalesOrder.OrderStatus.SHIPPED);
        so2.addLine(new OrderLine(so2, products.get(6), 3, products.get(6).getPrice()));
        so2.addLine(new OrderLine(so2, products.get(7), 2, products.get(7).getPrice()));
        salesOrderRepository.save(so2);

        // Purchase Orders
        PurchaseOrder po1 = new PurchaseOrder("PO-2026-001", suppliers.get(0), LocalDate.now().plusDays(7), PurchaseOrder.POStatus.ORDERED);
        po1.addLine(new POLine(po1, products.get(0), 50, products.get(0).getCostPrice()));
        po1.addLine(new POLine(po1, products.get(1), 30, products.get(1).getCostPrice()));
        purchaseOrderRepository.save(po1);

        PurchaseOrder po2 = new PurchaseOrder("PO-2026-002", suppliers.get(1), LocalDate.now().plusDays(10), PurchaseOrder.POStatus.SUBMITTED);
        po2.addLine(new POLine(po2, products.get(6), 100, products.get(6).getCostPrice()));
        purchaseOrderRepository.save(po2);
    }

    private void seedTransfers(List<Product> products, List<Warehouse> warehouses, List<Store> stores) {
        StockTransfer t1 = new StockTransfer(
            "TRF-2026-001",
            products.get(0),
            warehouses.get(0).getCode(),
            stores.get(0).getCode(),
            20,
            StockTransfer.TransferStatus.IN_TRANSIT,
            "Replenishment for Downtown Flagship store"
        );
        stockTransferRepository.save(t1);
    }

    private void seedAuditLogs() {
        auditLogRepository.save(new AuditLog(
            "SEED_INITIAL_DATA",
            "System",
            1L,
            "admin",
            "Database seeded with users, 30 products, warehouses, stores, suppliers, stock levels and initial orders."
        ));
    }
}
