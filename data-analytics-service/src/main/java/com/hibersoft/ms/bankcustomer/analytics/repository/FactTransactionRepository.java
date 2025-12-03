package com.hibersoft.ms.bankcustomer.analytics.repository;

import com.hibersoft.ms.bankcustomer.analytics.model.FactTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FactTransactionRepository extends JpaRepository<FactTransactionEntity, Long> {
    // Custom query to find top customers by spending in a time range
    @Query("SELECT f.customerId, SUM(f.amountStandard) as total_spent FROM FactTransactionEntity f " +
           "WHERE f.transactionTime BETWEEN :startDate AND :endDate " +
           "GROUP BY f.customerId ORDER BY total_spent DESC")
    List<Object[]> findTopSpendingCustomers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Custom query to count transactions by location
    @Query("SELECT f.locationCode, COUNT(f) FROM FactTransactionEntity f GROUP BY f.locationCode")
    List<Object[]> countTransactionsByLocation();
}
