package com.finance.service.impl;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.entity.enums.TransactionType;
import com.finance.exception.BadRequestException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import com.finance.service.FinancialRecordService;
import com.finance.specification.FinancialRecordSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import com.finance.dto.response.UserSummary;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", key = "#username")
    public FinancialRecordResponse createRecord(FinancialRecordRequest request, String username) {
        log.info("Creating financial record for user: {} with title {}", username, request.getTitle());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FinancialRecord record = FinancialRecord.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .user(user)
                .createdBy(user)
                .updatedBy(user)
                .build();

        financialRecordRepository.save(record);
        return mapToResponse(record);
    }

    @Override
    public FinancialRecordResponse getRecordById(Long id, String username) {
        FinancialRecord record = financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        if (!record.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Access denied");
        }

        return mapToResponse(record);
    }

    @Override
    public Page<FinancialRecordResponse> getAllRecordsByUser(String username,
                                                             TransactionType type,
                                                             String category,
                                                             String search,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Specification<FinancialRecord> spec = Specification.where(FinancialRecordSpecification.isNotDeleted())
                .and(FinancialRecordSpecification.hasUserId(user.getId()));

        if (type != null) {
            spec = spec.and(FinancialRecordSpecification.hasType(type));
        }
        if (category != null) {
            spec = spec.and(FinancialRecordSpecification.hasCategory(category));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(FinancialRecordSpecification.hasSearchTerm(search));
        }
        if (startDate != null) {
            spec = spec.and(FinancialRecordSpecification.hasTransactionDateAfterOrEqual(startDate));
        }
        if (endDate != null) {
            spec = spec.and(FinancialRecordSpecification.hasTransactionDateBeforeOrEqual(endDate));
        }

        Page<FinancialRecord> records = financialRecordRepository.findAll(spec, pageable);
        return records.map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", key = "#username")
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request, String username) {
        log.info("Updating financial record id {} for user {}", id, username);
        FinancialRecord record = financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!record.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Access denied");
        }

        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setTransactionDate(request.getTransactionDate());
        record.setUpdatedBy(user);

        financialRecordRepository.save(record);
        return mapToResponse(record);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", key = "#username")
    public void deleteRecord(Long id, String username) {
        log.info("Deleting financial record id {} for user {}", id, username);
        FinancialRecord record = financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!record.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Access denied");
        }

        record.softDelete(user);
        financialRecordRepository.save(record);
    }

    @Override
    public Page<FinancialRecordResponse> getRecordsByType(String username, TransactionType type, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Specification<FinancialRecord> spec = Specification.where(FinancialRecordSpecification.isNotDeleted())
                .and(FinancialRecordSpecification.hasUserId(user.getId()))
                .and(FinancialRecordSpecification.hasType(type));

        Page<FinancialRecord> records = financialRecordRepository.findAll(spec, pageable);
        return records.map(this::mapToResponse);
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
                .updatedAt(record.getUpdatedAt())
                .createdBy(toUserSummary(record.getCreatedBy()))
                .updatedBy(toUserSummary(record.getUpdatedBy()))
                .build();
    }

    private UserSummary toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return UserSummary.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }

}