package com.pm.customerservice.Repo;

import com.pm.customerservice.model.customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface customerRepo extends JpaRepository<customer, UUID> {
    customer findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID uuid);
    boolean existsByEmail(String email);

    //List<customer> id(UUID id);
}
