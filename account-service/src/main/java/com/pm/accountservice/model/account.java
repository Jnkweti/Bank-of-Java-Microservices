package com.pm.accountservice.model;

import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @PositiveOrZero
    private BigDecimal balance;

    @NotNull
    @Column(nullable = false)
    private UUID customerId;

    @NotNull
    @Column(nullable = false, unique = true)
    private String accountNumber;

    @NotNull
    @Column(nullable = false)
    private String accountName;

    // Enum for account type (SAVINGS, CHECKING, BUSINESS, CREDIT, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;

    // Whether the account is active, frozen, or closed
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AccountStatus status;

    // Interest rate (for savings/credit accounts)
    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    // Timestamp when the account was opened
    @Column(nullable = false)
    private LocalDateTime openedDate;

    // Timestamp for last transaction or update
    private LocalDateTime lastUpdated;

    @PrePersist
    public void onCreate() {
        openedDate = LocalDateTime.now();
    }
}