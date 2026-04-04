package com.finance.service;

import com.finance.dto.response.DashboardSummaryResponse;

public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary(String username);

}