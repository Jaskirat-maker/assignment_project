package com.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netBalance;

    private Map<String, BigDecimal> categoryWiseTotals;

    private Map<String, BigDecimal> monthlySummary;

    private List<FinancialRecordResponse> recentTransactions;

    private Map<String, BigDecimal> weeklyTrends;

}