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
        else if(request.getType() == AccountType.LOAN || acc.getAccountType() == AccountType.CREDIT){
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
        Acc.setAccountName(acc.getAccountName());
        Acc.setAccountType(String.valueOf(acc.getAccountType()));
        Acc.setAccountStatus(String.valueOf(acc.getStatus()));
        Acc.setInterestRate(acc.getInterestRate().toString());
        Acc.setLastUpdate(LocalDateTime.now().toString());
        return Acc;
    }

}
