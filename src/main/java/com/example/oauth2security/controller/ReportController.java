package com.example.oauth2security.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public Map<String, Object> getSalesReport() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Getting sales report");
        response.put("requiredPermission", "REPORT_READ");
        return response;
    }

    @GetMapping("/financial")
    @PreAuthorize("hasAuthority('REPORT_READ') and hasRole('ADMIN')")
    public Map<String, Object> getFinancialReport() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Getting financial report (ADMIN only)");
        response.put("requiredPermission", "REPORT_READ + ROLE_ADMIN");
        return response;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('REPORT_CREATE')")
    public Map<String, Object> generateReport(@RequestBody Map<String, Object> reportData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Generating new report");
        response.put("requiredPermission", "REPORT_CREATE");
        response.put("reportData", reportData);
        return response;
    }
}
