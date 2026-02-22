package com.pm.accountservice.Service;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Exception.AccountNumberAlreadyExistException;
import com.pm.accountservice.Mapper.MapAcc;
import com.pm.accountservice.Repository.accountRepo;
import com.pm.accountservice.model.account;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Random;
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
        String accNum = "BOJ-" + String.format("%010d", new Random().nextInt(1_000_000_000));
        if(repository.existsByAccountNumber(accNum)) throw new AccountNumberAlreadyExistException("Account Number already exists!");
        acc.setAccountNumber(accNum);
        repository.save(acc);

        return  MapAcc.toDTO(acc);

    }
}
