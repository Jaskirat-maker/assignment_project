package com.finance.controller;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.enums.TransactionType;
import com.finance.service.CsvExportService;
import com.finance.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Financial Records", description = "Manage financial records, filtering, and export")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;
    private final CsvExportService csvExportService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create financial record", description = "Creates a financial record for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<FinancialRecordResponse> createRecord(@Valid @RequestBody FinancialRecordRequest request, Authentication authentication) {
        String username = authentication.getName();
        log.info("Creating financial record for user {}", username);
        FinancialRecordResponse response = financialRecordService.createRecord(request, username);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ANALYST','ADMIN')")
    @Operation(summary = "List records", description = "Returns paginated records with optional filters and text search.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Records returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<FinancialRecordResponse>> getAllRecordsByUser(
            Authentication authentication,
            @Parameter(description = "Filter by type", example = "INCOME")
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by category", example = "Salary")
            @RequestParam(required = false) String category,
            @Parameter(description = "Search in title, description, category", example = "rent")
            @RequestParam(required = false) String search,
            @Parameter(description = "Start date (inclusive), yyyy-MM-dd", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive), yyyy-MM-dd", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        String username = authentication.getName();
        Page<FinancialRecordResponse> response = financialRecordService
                .getAllRecordsByUser(username, type, category, search, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ANALYST','ADMIN')")
    @Operation(summary = "Get record by id")
    public ResponseEntity<FinancialRecordResponse> getRecordById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        FinancialRecordResponse response = financialRecordService.getRecordById(id, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update record by id")
    public ResponseEntity<FinancialRecordResponse> updateRecord(@PathVariable Long id, @Valid @RequestBody FinancialRecordRequest request, Authentication authentication) {
        String username = authentication.getName();
        FinancialRecordResponse response = financialRecordService.updateRecord(id, request, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Soft-delete record by id")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        financialRecordService.deleteRecord(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyAuthority('ANALYST','ADMIN')")
    @Operation(summary = "List records by type")
    public ResponseEntity<Page<FinancialRecordResponse>> getRecordsByType(@PathVariable TransactionType type, Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<FinancialRecordResponse> response = financialRecordService.getRecordsByType(username, type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @Operation(summary = "Export records as CSV")
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