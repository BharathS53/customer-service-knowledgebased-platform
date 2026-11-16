package com.example.customerservice.controller;

import com.example.customerservice.dto.DashboardStatsDto;
import com.example.customerservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for Phase 4 verification.
 * Injects DashboardService to test full stack connectivity: Controller -> Service -> Repository -> MySQL.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthCheckController {

    private final DashboardService dashboardService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        DashboardStatsDto stats = dashboardService.getDashboardStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Customer Service Knowledge-Based Resolution Platform");
        response.put("phase", "Phase 10: Smart Search and Article Recommendations");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("recommendationFeatures", new String[]{
            "AI & Keyword-Based Matching Algorithm (Category Alignment, Keyword Overlap, Tag Weighting)",
            "Staff Ticket Solution Recommendations (/api/recommendations/ticket/{id} & UI 1-Click Link)",
            "Real-Time Customer Ticket Deflection (/api/recommendations/search & Live Suggestions on Create Form)",
            "Dynamic Tag Cloud & Multi-Facet Filtering (/customer/knowledge, /agent/knowledge)",
            "Direct Recommendation API (/api/articles/recommend?ticketId={id})"
        });
        response.put("liveStats", stats);
        response.put("message", "Smart Search and Recommendation Engine active! Ready for Phase 11 (Analytics and Reporting).");
        return ResponseEntity.ok(response);
    }
}
