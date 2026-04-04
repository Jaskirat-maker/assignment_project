package com.finance.service;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordService {

    FinancialRecordResponse createRecord(FinancialRecordRequest request, String username);

    FinancialRecordResponse getRecordById(Long id, String username);

    Page<FinancialRecordResponse> getAllRecordsByUser(String username, TransactionType type, String category, LocalDate startDate, LocalDate endDate, Pageable pageable);

    FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request, String username);

    void deleteRecord(Long id, String username);

    Page<FinancialRecordResponse> getRecordsByType(String username, TransactionType type, Pageable pageable);

}