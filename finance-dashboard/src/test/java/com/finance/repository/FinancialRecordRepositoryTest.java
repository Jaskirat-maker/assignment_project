package com.finance.repository;

import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.entity.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FinancialRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);
    }

    @Test
    void sumAmountByUserIdAndType_ShouldReturnCorrectSum_ForIncome() {
        // Given
        FinancialRecord record1 = FinancialRecord.builder()
                .title("Salary")
                .amount(BigDecimal.valueOf(1000.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        FinancialRecord record2 = FinancialRecord.builder()
                .title("Bonus")
                .amount(BigDecimal.valueOf(500.00))
                .type(TransactionType.INCOME)
                .category("Bonus")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        FinancialRecord record3 = FinancialRecord.builder()
                .title("Expense")
                .amount(BigDecimal.valueOf(200.00))
                .type(TransactionType.EXPENSE)
                .category("Food")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        entityManager.persist(record1);
        entityManager.persist(record2);
        entityManager.persist(record3);
        entityManager.flush();

        // When
        BigDecimal sum = financialRecordRepository.sumAmountByUserIdAndType(user.getId(), TransactionType.INCOME);

        // Then
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
    }

    @Test
    void sumAmountByUserIdAndType_ShouldReturnCorrectSum_ForExpense() {
        // Given
        FinancialRecord record1 = FinancialRecord.builder()
                .title("Food")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category("Food")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        FinancialRecord record2 = FinancialRecord.builder()
                .title("Transport")
                .amount(BigDecimal.valueOf(50.00))
                .type(TransactionType.EXPENSE)
                .category("Transport")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        entityManager.persist(record1);
        entityManager.persist(record2);
        entityManager.flush();

        // When
        BigDecimal sum = financialRecordRepository.sumAmountByUserIdAndType(user.getId(), TransactionType.EXPENSE);

        // Then
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(150.00));
    }

    @Test
    void findByUserIdAndType_ShouldReturnOnlyMatchingType() {
        // Given
        FinancialRecord incomeRecord = FinancialRecord.builder()
                .title("Salary")
                .amount(BigDecimal.valueOf(1000.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        FinancialRecord expenseRecord = FinancialRecord.builder()
                .title("Food")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category("Food")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        entityManager.persist(incomeRecord);
        entityManager.persist(expenseRecord);
        entityManager.flush();

        // When
        var incomeRecords = financialRecordRepository.findByUserIdAndType(user.getId(), TransactionType.INCOME);
        var expenseRecords = financialRecordRepository.findByUserIdAndType(user.getId(), TransactionType.EXPENSE);

        // Then
        assertThat(incomeRecords).hasSize(1);
        assertThat(incomeRecords.get(0).getType()).isEqualTo(TransactionType.INCOME);
        assertThat(expenseRecords).hasSize(1);
        assertThat(expenseRecords.get(0).getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void findByUserIdAndTransactionDateBetween_ShouldReturnRecordsInDateRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        LocalDate inRangeDate = LocalDate.of(2024, 6, 15);
        LocalDate outOfRangeDate = LocalDate.of(2025, 1, 1);

        FinancialRecord inRangeRecord = FinancialRecord.builder()
                .title("In Range")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.INCOME)
                .category("Test")
                .transactionDate(inRangeDate)
                .user(user)
                .build();

        FinancialRecord outOfRangeRecord = FinancialRecord.builder()
                .title("Out of Range")
                .amount(BigDecimal.valueOf(200.00))
                .type(TransactionType.INCOME)
                .category("Test")
                .transactionDate(outOfRangeDate)
                .user(user)
                .build();

        entityManager.persist(inRangeRecord);
        entityManager.persist(outOfRangeRecord);
        entityManager.flush();

        // When
        var records = financialRecordRepository.findByUserIdAndTransactionDateBetween(user.getId(), startDate, endDate);

        // Then
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getTransactionDate()).isEqualTo(inRangeDate);
    }

}