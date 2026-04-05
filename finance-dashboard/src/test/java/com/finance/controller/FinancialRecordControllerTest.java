package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.enums.TransactionType;
import com.finance.service.CsvExportService;
import com.finance.service.FinancialRecordService;
import com.finance.security.JwtTokenProvider;
import com.finance.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialRecordController.class)
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialRecordService financialRecordService;

    @MockBean
    private CsvExportService csvExportService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void createRecord_ShouldReturn201_WhenValidRequest() throws Exception {
        // Given
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .title("Test Record")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .build();

        FinancialRecordResponse response = FinancialRecordResponse.builder()
                .id(1L)
                .title("Test Record")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .build();

        when(financialRecordService.createRecord(any(FinancialRecordRequest.class), eq("testuser")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Record"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createRecord_ShouldReturn400_WhenAmountNegative() throws Exception {
        // Given
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .title("Test Record")
                .amount(BigDecimal.valueOf(-100.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createRecord_ShouldReturn400_WhenTitleBlank() throws Exception {
        // Given
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .title("")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllRecordsByUser_ShouldReturn200_WithPagedResults() throws Exception {
        // Given
        FinancialRecordResponse record = FinancialRecordResponse.builder()
                .id(1L)
                .title("Test Record")
                .build();

        Page<FinancialRecordResponse> page = new PageImpl<>(List.of(record), PageRequest.of(0, 10), 1);

        when(financialRecordService.getAllRecordsByUser(eq("testuser"), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/records")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteRecord_ShouldReturn204_WhenSuccessful() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/records/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

}