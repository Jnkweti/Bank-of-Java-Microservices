package com.pm.accountservice.DTO;

import lombok.Data;

@Data
public class AccResponseDTO {

    private String accountId;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private String accountBalance;
    private String accountStatus;
    private String interestRate;
    private String customerId;
    private String openedDate;
    private String lastUpdate;
}
