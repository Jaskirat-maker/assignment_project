package com.finance.specification;

import com.finance.entity.FinancialRecord;
import com.finance.entity.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class FinancialRecordSpecification {

    public static Specification<FinancialRecord> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("deleted"));
    }

    public static Specification<FinancialRecord> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<FinancialRecord> hasType(TransactionType type) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> hasCategory(String category) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<FinancialRecord> hasSearchTerm(String search) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
            Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
            Predicate categoryMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), pattern);
            return criteriaBuilder.or(titleMatch, descriptionMatch, categoryMatch);
        };
    }

    public static Specification<FinancialRecord> hasTransactionDateAfterOrEqual(LocalDate startDate) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate);
    }

    public static Specification<FinancialRecord> hasTransactionDateBeforeOrEqual(LocalDate endDate) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate);
    }

}