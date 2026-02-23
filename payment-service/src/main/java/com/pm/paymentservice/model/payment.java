package com.pm.paymentservice.model;

import com.pm.paymentservice.Enum.PaymentStatus;
import com.pm.paymentservice.Enum.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payments")
public class payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The account being debited
    @NotNull
    @Column(nullable = false)
    private String fromAccountId;

    // The account being credited
    @NotNull
    @Column(nullable = false)
    private String toAccountId;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // Persists the enum as its string name (e.g. "COMPLETED") rather than
    // an integer index. String is safer - adding new values won't break existing data.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // JPA lifecycle hook - fires automatically just before the first INSERT.
    // Guarantees createdAt is always set, even if the service layer forgets.
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Fires automatically before every UPDATE.
    // Keeps updatedAt in sync without manual tracking.
    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
