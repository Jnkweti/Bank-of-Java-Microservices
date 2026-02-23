package com.pm.paymentservice.Repository;

import com.pm.paymentservice.model.payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface paymentRepo extends JpaRepository<payment, UUID> {

    // Returns all payments where the given account was either the sender or receiver.
    // Spring Data JPA generates: WHERE from_account_id = ? OR to_account_id = ?
    // The repeated parameter is intentional - both sides of the OR use the same value.
    List<payment> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId);
}
