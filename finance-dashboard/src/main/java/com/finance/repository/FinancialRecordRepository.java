package com.finance.repository;

import com.finance.entity.FinancialRecord;
import com.finance.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    List<FinancialRecord> findByUserIdAndDeletedFalse(Long userId);

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    List<FinancialRecord> findByUserIdAndType(Long userId, TransactionType type);

    List<FinancialRecord> findByUserIdAndCategory(Long userId, String category);

    @Query("SELECT SUM(fr.amount) FROM FinancialRecord fr WHERE fr.user.id = :userId AND fr.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    List<FinancialRecord> findByUserIdAndTransactionDateBetweenAndDeletedFalse(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT fr.category, SUM(CASE WHEN fr.type = 'INCOME' THEN fr.amount ELSE -fr.amount END) " +
           "FROM FinancialRecord fr WHERE fr.user.id = :userId AND fr.deleted = false GROUP BY fr.category")
    List<Object[]> findCategoryTotalsByUserId(@Param("userId") Long userId);

}