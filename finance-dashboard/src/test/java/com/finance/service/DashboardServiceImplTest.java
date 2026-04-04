package com.finance.service;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.entity.enums.TransactionType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import com.finance.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;
    private List<FinancialRecord> records;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        FinancialRecord incomeRecord = FinancialRecord.builder()
                .amount(BigDecimal.valueOf(1000.00))
                .type(TransactionType.INCOME)
                .build();

        FinancialRecord expenseRecord = FinancialRecord.builder()
                .amount(BigDecimal.valueOf(500.00))
                .type(TransactionType.EXPENSE)
                .build();

        records = List.of(incomeRecord, expenseRecord);
    }

    @Test
    void getDashboardSummary_ShouldReturnCorrectSummary() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));
        when(financialRecordRepository.findByUserId(1L)).thenReturn(records);

        // When
        DashboardSummaryResponse response = dashboardService.getDashboardSummary("testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(response.getTotalExpense()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(response.getNetBalance()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    void getDashboardSummary_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dashboardService.getDashboardSummary("testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

}