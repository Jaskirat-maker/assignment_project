package com.finance.dto.response;

import com.finance.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordResponse {

    private Long id;

    private String title;

    private String description;

    private BigDecimal amount;

    private TransactionType type;

    private String category;

    private LocalDate transactionDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UserSummary createdBy;

    private UserSummary updatedBy;

}