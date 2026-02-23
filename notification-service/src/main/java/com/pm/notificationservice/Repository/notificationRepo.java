package com.pm.notificationservice.Repository;

import com.pm.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface notificationRepo extends JpaRepository<Notification, UUID> {

    // Deduplication check before saving a new notification.
    boolean existsByPaymentId(String paymentId);

    // Returns all notifications where the given account was the sender or receiver.
    List<Notification> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId);
}
