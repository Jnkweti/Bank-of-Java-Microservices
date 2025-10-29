package com.pm.accountservice.Service;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Mapper.MapAcc;
import com.pm.accountservice.Repository.accountRepo;
import com.pm.accountservice.model.account;
import lombok.AllArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class accountService {

    accountRepo repository;

//    public List<AccResponseDTO> getAllAccounts(){
//
//        List<account> accs = repository.findAll();
//
//        return accs.stream()
//                .map(account -> accountMapper.toDTO(account)).toList();
//    }
//    public AccResponseDTO getAccount(String accountId){
//        account acc = repository.findById(UUID.fromString(accountId))
//                .orElseThrow(() -> new AccountNotFoundException("this id is not associated with any account"));
//        return accountMapper.toDTO(acc);
//
//    }
    public AccResponseDTO createAccount(AccRequestDTO accRequestDTO) {
        account acc = MapAcc.toEntity(accRequestDTO);
        acc.
    //must check to ensure customer id exist!!

        repository.save(acc);


        //Placeholder
        AccResponseDTO accResponseDTO = new AccResponseDTO();
        return accResponseDTO;
    }
}
