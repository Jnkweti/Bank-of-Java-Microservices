package com.pm.accountservice.Mapper;


import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.model.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MapAcc {

    public static account toEntity(AccRequestDTO request){

        account acc = new account();
        acc.setCustomerId(UUID.fromString(request.getCustomerId()));
        acc.setAccountName(request.getAccountName());
        acc.setStatus(request.getStatus());
        acc.setAccountType(request.getType());
        acc.setBalance(new BigDecimal(request.getBalance()));
        acc.setLastUpdated(LocalDateTime.now());
        if(request.getType() == AccountType.SAVINGS){
            acc.setInterestRate(new BigDecimal(".045"));
        }
        else if(request.getType() == AccountType.LOAN || request.getType() == AccountType.CREDIT){
            acc.setInterestRate(new BigDecimal(".0045"));
        }
        else{
            acc.setInterestRate(new BigDecimal("0.0"));
        }
        return acc;
    }
    public static AccResponseDTO toDTO(account acc){

        AccResponseDTO Acc = new AccResponseDTO();
        Acc.setAccountId(acc.getId().toString());
        Acc.setAccountName(acc.getAccountName());
        Acc.setAccountNumber(acc.getAccountNumber());
        Acc.setAccountBalance(acc.getBalance().toString());
        Acc.setCustomerId(acc.getCustomerId().toString());
        Acc.setAccountType(String.valueOf(acc.getAccountType()));
        Acc.setAccountStatus(String.valueOf(acc.getStatus()));
        Acc.setInterestRate(acc.getInterestRate() != null ? acc.getInterestRate().toString() : "0.0");
        Acc.setOpenedDate(acc.getOpenedDate() != null ? acc.getOpenedDate().toString() : "");
        Acc.setLastUpdate(acc.getLastUpdated() != null ? acc.getLastUpdated().toString() : "");
        return Acc;
    }

}
