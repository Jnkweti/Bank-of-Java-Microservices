package com.pm.accountservice.DTO;

import lombok.Data;

@Data
public class accountResponseDTO {
    private String accountNumber;
    private String accountType;
    private String balance;
    private String status;
    private String lastUpdated;
    private String customerId;
}
