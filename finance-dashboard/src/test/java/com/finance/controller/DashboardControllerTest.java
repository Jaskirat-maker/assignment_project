package com.finance.controller;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.security.CustomUserDetailsService;
import com.finance.security.JwtTokenProvider;
import com.finance.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(com.finance.security.SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "testuser", authorities = {"VIEWER"})
    void getDashboardSummary_ShouldReturn200_ForAuthorizedUser() throws Exception {
        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .totalIncome(BigDecimal.valueOf(1000))
                .totalExpense(BigDecimal.valueOf(500))
                .netBalance(BigDecimal.valueOf(500))
                .categoryWiseTotals(Map.of("Salary", BigDecimal.valueOf(100)))
                .recentTransactions(Collections.emptyList())
                .weeklyTrends(Map.of("Week 1", BigDecimal.valueOf(250)))
                .build();

        when(dashboardService.getDashboardSummary("testuser")).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000))
                .andExpect(jsonPath("$.netBalance").value(500))
                .andExpect(jsonPath("$.categoryWiseTotals.Salary").value(100))
                .andExpect(jsonPath("$.weeklyTrends['Week 1']").value(250))
                .andExpect(jsonPath("$.recentTransactions").isArray());
    }

    @Test
    void getDashboardSummary_ShouldReturn401_ForUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
