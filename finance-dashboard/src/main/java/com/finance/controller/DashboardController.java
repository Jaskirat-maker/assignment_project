package com.finance.controller;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        String username = authentication.getName();
        log.info("Loading dashboard summary for user {}", username);
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(username);
        return ResponseEntity.ok(response);
    }

}