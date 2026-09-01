package com.warrantyportal.controller;

import com.warrantyportal.dto.DashboardResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing customer dashboard telemetry and activity aggregation.
 * Phase 12 — Dashboard & Notifications
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves aggregated customer dashboard metrics and real recent activity.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal User currentUser) {
        DashboardResponse response = dashboardService.getCustomerDashboard(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
