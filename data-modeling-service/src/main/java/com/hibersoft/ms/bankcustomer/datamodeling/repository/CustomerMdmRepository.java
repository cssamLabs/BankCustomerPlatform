package com.hibersoft.ms.bankcustomer.datamodeling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hibersoft.ms.bankcustomer.datamodeling.model.CustomerMdmEntity; 

import java.util.Optional;

@Repository
public interface CustomerMdmRepository extends JpaRepository<CustomerMdmEntity, Long> {
    // Custom method to find the unified Customer ID based on the bank's specific account ID
    Optional<CustomerMdmEntity> findByBankSpecificAccountId(String bankSpecificAccountId);
}
