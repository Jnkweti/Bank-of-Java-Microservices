package com.pm.accountservice.model;

import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.Repository.accountRepo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Entity
@Data
public class account {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @NotNull
    @PositiveOrZero
    private BigDecimal balance;

    @NotNull
    @Column(nullable = false)
    private UUID customerId;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2, unique = true)
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


    //used to generate account number
    @PrePersist
    public void onCreate() {
        if (accountNumber == null) {
            // Generate a 10-digit sequential number starting from 1000000000
            accountNumber = generateNextAccountNumber();
        }
    }

    private static accountRepo accountRepository;
    private static final Random RANDOM = new Random();

    private String generateNextAccountNumber() {
        String accNum;
        do {
            StringBuilder sb = new StringBuilder("BOJ-");
            for (int i = 0; i < 10; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            accNum = sb.toString();
        } while (accountRepository.existsByAccountNumber(accNum)); // DB uniqueness check
        return accNum;
    }


}
