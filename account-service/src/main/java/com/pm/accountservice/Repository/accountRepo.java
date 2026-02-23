package com.pm.accountservice.Repository;


import java.util.Optional;
import com.pm.accountservice.model.account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface accountRepo extends JpaRepository<account, UUID> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<account> findByCustomerId(UUID customerId);

}
