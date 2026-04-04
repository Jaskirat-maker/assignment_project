package com.finance.controller;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.enums.TransactionType;
import com.finance.service.CsvExportService;
import com.finance.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;
    private final CsvExportService csvExportService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FinancialRecordResponse> createRecord(@Valid @RequestBody FinancialRecordRequest request, Authentication authentication) {
        String username = authentication.getName();
        log.info("Creating financial record for user {}", username);
        FinancialRecordResponse response = financialRecordService.createRecord(request, username);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ANALYST','ADMIN')")
    public ResponseEntity<Page<FinancialRecordResponse>> getAllRecordsByUser(
            Authentication authentication,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        String username = authentication.getName();
        Page<FinancialRecordResponse> response = financialRecordService.getAllRecordsByUser(username, type, category, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ANALYST','ADMIN')")
    public ResponseEntity<FinancialRecordResponse> getRecordById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        FinancialRecordResponse response = financialRecordService.getRecordById(id, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FinancialRecordResponse> updateRecord(@PathVariable Long id, @Valid @RequestBody FinancialRecordRequest request, Authentication authentication) {
        String username = authentication.getName();
        FinancialRecordResponse response = financialRecordService.updateRecord(id, request, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        financialRecordService.deleteRecord(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyAuthority('ANALYST','ADMIN')")
    public ResponseEntity<Page<FinancialRecordResponse>> getRecordsByType(@PathVariable TransactionType type, Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<FinancialRecordResponse> response = financialRecordService.getRecordsByType(username, type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRecords(Authentication authentication) {
        String username = authentication.getName();
        byte[] csvData = csvExportService.exportUserRecords(username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "transactions.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

}