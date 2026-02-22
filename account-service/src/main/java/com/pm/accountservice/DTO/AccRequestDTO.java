package com.pm.accountservice.DTO;

import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccRequestDTO {

    @NotBlank(message = "account name is required")
    @Size(max = 100, message="account name cannot exceed 100 characters")
    private String accountName;

    @NotNull
    private AccountStatus status;

    @NotNull
    private AccountType type;

    @NotBlank
    private String balance;

    @NotBlank
    private String customerId;

    private String lastUpdated;
}
