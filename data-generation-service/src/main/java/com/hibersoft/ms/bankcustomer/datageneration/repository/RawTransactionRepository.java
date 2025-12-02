package com.hibersoft.ms.bankcustomer.datageneration.repository;

import com.hibersoft.ms.bankcustomer.datageneration.model.RawTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawTransactionRepository extends JpaRepository<RawTransactionEntity, String> {
    // Standard CRUD operations are automatically available
}
