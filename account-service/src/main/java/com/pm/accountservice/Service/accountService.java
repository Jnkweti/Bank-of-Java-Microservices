package com.pm.accountservice.Service;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Exception.AccountNotFoundException;
import com.pm.accountservice.Exception.AccountNumberAlreadyExistException;
import com.pm.accountservice.Exception.CustomerNotFoundException;
import com.pm.accountservice.GRPC.CustomerServiceGrpcClient;
import com.pm.accountservice.Mapper.MapAcc;
import com.pm.accountservice.Repository.accountRepo;
import com.pm.accountservice.model.account;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
public class accountService {

    accountRepo repository;
    CustomerServiceGrpcClient customerServiceGrpcClient;

    public List<AccResponseDTO> getAllAccounts() {
        List<account> accs = repository.findAll();
        return accs.stream().map(MapAcc::toDTO).toList();
    }

    public AccResponseDTO getAccount(String accountId) {
        account acc = repository.findById(UUID.fromString(accountId))
                .orElseThrow(() -> new AccountNotFoundException("this id is not associated with any account"));
        return MapAcc.toDTO(acc);
    }

    public AccResponseDTO getAccountByCustomerId(String customerId) {
        account acc = repository.findByCustomerId(UUID.fromString(customerId))
                .orElseThrow(() -> new AccountNotFoundException("No account found for customer id: " + customerId));
        return MapAcc.toDTO(acc);
    }

    public AccResponseDTO updateAccount(String accountId, AccRequestDTO accRequestDTO) {
        account acc = repository.findById(UUID.fromString(accountId))
                .orElseThrow(() -> new AccountNotFoundException("this id is not associated with any account"));
        acc.setAccountName(accRequestDTO.getAccountName());
        acc.setStatus(accRequestDTO.getStatus());
        acc.setAccountType(accRequestDTO.getType());
        acc.setBalance(new BigDecimal(accRequestDTO.getBalance()));
        acc.setLastUpdated(LocalDateTime.now());
        repository.save(acc);
        return MapAcc.toDTO(acc);
    }

    public void deleteAccount(String accountId) {
        repository.findById(UUID.fromString(accountId))
                .orElseThrow(() -> new AccountNotFoundException("this id is not associated with any account"));
        repository.deleteById(UUID.fromString(accountId));
    }

    public AccResponseDTO createAccount(AccRequestDTO accRequestDTO) {
        // Validate the customer exists in customer-service before creating the account
        try {
            customerServiceGrpcClient.getCustomerById(accRequestDTO.getCustomerId());
        } catch (StatusRuntimeException e) {
            throw new CustomerNotFoundException("Customer not found with id: " + accRequestDTO.getCustomerId());
        }

        account acc = MapAcc.toEntity(accRequestDTO);
        String accNum = "BOJ-" + String.format("%010d", new Random().nextInt(1_000_000_000));
        if (repository.existsByAccountNumber(accNum))
            throw new AccountNumberAlreadyExistException("Account Number already exists!");
        acc.setAccountNumber(accNum);
        repository.save(acc);
        return MapAcc.toDTO(acc);
    }
}
