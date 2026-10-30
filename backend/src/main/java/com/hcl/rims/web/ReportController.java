package com.hcl.rims.web;

import com.hcl.rims.dto.KpiSummaryDTO;
import com.hcl.rims.dto.SalesTrendDTO;
import com.hcl.rims.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/kpi")
    public ResponseEntity<KpiSummaryDTO> getKpiSummary() {
        return ResponseEntity.ok(reportService.getKpiSummary());
    }

    @GetMapping("/sales-trends")
    public ResponseEntity<List<SalesTrendDTO>> getSalesTrends() {
        return ResponseEntity.ok(reportService.getSalesTrends());
    }

    @GetMapping(value = "/export/inventory", produces = "text/csv")
    public ResponseEntity<String> exportInventoryCsv() {
        String csv = reportService.exportInventoryCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_report.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping(value = "/export/orders", produces = "text/csv")
    public ResponseEntity<String> exportOrdersCsv() {
        String csv = reportService.exportOrdersCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders_report.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }
}
