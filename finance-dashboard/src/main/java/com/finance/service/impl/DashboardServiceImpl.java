package com.finance.service.impl;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.entity.enums.TransactionType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import com.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "dashboardSummary", key = "#username")
    public DashboardSummaryResponse getDashboardSummary(String username) {
        log.debug("Generating dashboard summary for user {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<FinancialRecord> records = financialRecordRepository.findByUserId(user.getId());

        BigDecimal totalIncome = records.stream()
                .filter(r -> r.getType() == TransactionType.INCOME)
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = records.stream()
                .filter(r -> r.getType() == TransactionType.EXPENSE)
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> categoryWiseTotals = new HashMap<>();
        List<Object[]> categoryTotals = financialRecordRepository.findCategoryTotalsByUserId(user.getId());
        for (Object[] row : categoryTotals) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            categoryWiseTotals.put(category, total);
        }

        Map<String, BigDecimal> monthlySummary = new HashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            List<FinancialRecord> monthlyRecords = financialRecordRepository
                    .findByUserIdAndTransactionDateBetween(user.getId(), monthStart, monthEnd);

            BigDecimal monthlyIncome = monthlyRecords.stream()
                    .filter(r -> r.getType() == TransactionType.INCOME)
                    .map(FinancialRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthlyExpense = monthlyRecords.stream()
                    .filter(r -> r.getType() == TransactionType.EXPENSE)
                    .map(FinancialRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);
            String monthKey = monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlySummary.put(monthKey, monthlyNet);
        }

        List<FinancialRecordResponse> recentTransactions = records.stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(10)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        Map<String, BigDecimal> weeklyTrends = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTransactionDate().getYear() + "-W" + r.getTransactionDate().get(weekFields.weekOfWeekBasedYear()),
                        Collectors.reducing(BigDecimal.ZERO, r -> {
                            BigDecimal value = r.getAmount();
                            return r.getType() == TransactionType.INCOME ? value : value.negate();
                        }, BigDecimal::add)
                ));

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .categoryWiseTotals(categoryWiseTotals)
                .monthlySummary(monthlySummary)
                .recentTransactions(recentTransactions)
                .weeklyTrends(weeklyTrends)
                .build();
    }

    private FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .title(record.getTitle())
                .description(record.getDescription())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .transactionDate(record.getTransactionDate())
                .createdAt(record.getCreatedAt())
                .build();
    }

}