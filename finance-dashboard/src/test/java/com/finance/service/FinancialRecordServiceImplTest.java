package com.finance.service;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.entity.enums.TransactionType;
import com.finance.exception.BadRequestException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import com.finance.service.impl.FinancialRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceImplTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialRecordServiceImpl financialRecordService;

    private User user;
    private FinancialRecord record;
    private FinancialRecordRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        record = FinancialRecord.builder()
                .id(1L)
                .title("Test Record")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .user(user)
                .build();

        request = FinancialRecordRequest.builder()
                .title("Test Record")
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.INCOME)
                .category("Salary")
                .transactionDate(LocalDate.now())
                .build();
    }

    @Test
    void createRecord_ShouldReturnFinancialRecordResponse_WhenValidRequest() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(record);

        // When
        FinancialRecordResponse response = financialRecordService.createRecord(request, "testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Record");
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        verify(financialRecordRepository).save(any(FinancialRecord.class));
    }

    @Test
    void createRecord_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> financialRecordService.createRecord(request, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getRecordById_ShouldReturnRecord_WhenRecordBelongsToUser() {
        // Given
        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        // When
        FinancialRecordResponse response = financialRecordService.getRecordById(1L, "testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getRecordById_ShouldThrowBadRequestException_WhenRecordBelongsToDifferentUser() {
        // Given
        User differentUser = User.builder().username("otheruser").build();
        FinancialRecord differentRecord = FinancialRecord.builder().user(differentUser).build();
        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(differentRecord));

        // When & Then
        assertThatThrownBy(() -> financialRecordService.getRecordById(1L, "testuser"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Access denied");
    }

    @Test
    void getRecordById_ShouldThrowResourceNotFoundException_WhenRecordNotFound() {
        // Given
        when(financialRecordRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> financialRecordService.getRecordById(1L, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Financial record not found with id: 1");
    }

    @Test
    void deleteRecord_ShouldCallRepositoryDelete_WhenAuthorized() {
        // Given
        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        // When
        financialRecordService.deleteRecord(1L, "testuser");

        // Then
        verify(financialRecordRepository).delete(record);
    }

    @Test
    void deleteRecord_ShouldThrowBadRequestException_WhenUnauthorized() {
        // Given
        User differentUser = User.builder().username("otheruser").build();
        FinancialRecord differentRecord = FinancialRecord.builder().user(differentUser).build();
        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(differentRecord));

        // When & Then
        assertThatThrownBy(() -> financialRecordService.deleteRecord(1L, "testuser"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Access denied");
    }

    @Test
    void getAllRecordsByUser_ShouldReturnPagedRecords() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialRecord> page = new PageImpl<>(List.of(record), pageable, 1);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(financialRecordRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // When
        var response = financialRecordService.getAllRecordsByUser("testuser", null, null, null, null, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Test Record");
    }

}