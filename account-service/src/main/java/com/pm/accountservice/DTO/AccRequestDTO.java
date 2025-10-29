package com.pm.accountservice.DTO;

import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccRequestDTO {

    @NotBlank(message = "account name is required")
    @Size(max = 100, message="account name cannot exceed 100 characters")
    private String accountName;

    @Enumerated(EnumType.STRING)
    @NotBlank
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @NotBlank
    private AccountType type;

    @NotBlank
    private String balance;

    @NotBlank
    private String customerId;

    private String lastUpdated;
}
