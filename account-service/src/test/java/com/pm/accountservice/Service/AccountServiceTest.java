package com.pm.accountservice.Service;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.Exception.AccountNotFoundException;
import com.pm.accountservice.Exception.CustomerNotFoundException;
import com.pm.accountservice.GRPC.CustomerServiceGrpcClient;
import com.pm.accountservice.Repository.accountRepo;
import com.pm.accountservice.model.account;
import com.pm.proto.GetCustomerResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) tells JUnit to use Mockito to manage
// the @Mock and @InjectMocks annotations below. Without this, they'd be ignored.
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private accountRepo repository;

    // We mock the gRPC client because we don't want real network calls in unit tests.
    // Tests should be fast, isolated, and not depend on other services being up.
    @Mock
    private CustomerServiceGrpcClient customerServiceGrpcClient;

    // @InjectMocks creates a real accountService instance and injects
    // the @Mock fields into it via the @AllArgsConstructor constructor.
    @InjectMocks
    private accountService accountService;

    private UUID testId;
    private account testAccount;
    private AccRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testAccount = new account();
        testAccount.setId(testId);
        testAccount.setAccountName("John's Savings");
        testAccount.setAccountNumber("BOJ-0000000001");
        testAccount.setCustomerId(UUID.randomUUID());
        testAccount.setBalance(new BigDecimal("500.00"));
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setInterestRate(new BigDecimal("0.045"));
        testAccount.setLastUpdated(LocalDateTime.now());

        requestDTO = new AccRequestDTO();
        requestDTO.setAccountName("John's Savings");
        requestDTO.setCustomerId(UUID.randomUUID().toString());
        requestDTO.setBalance("500.00");
        requestDTO.setType(AccountType.SAVINGS);
        requestDTO.setStatus(AccountStatus.ACTIVE);
    }

    // --- getAllAccounts ---

    @Test
    void getAllAccounts_shouldReturnMappedList() {
        when(repository.findAll()).thenReturn(List.of(testAccount));

        List<AccResponseDTO> result = accountService.getAllAccounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountName()).isEqualTo("John's Savings");
    }

    @Test
    void getAllAccounts_shouldReturnEmptyListWhenNoAccounts() {
        when(repository.findAll()).thenReturn(List.of());

        List<AccResponseDTO> result = accountService.getAllAccounts();

        assertThat(result).isEmpty();
    }

    // --- getAccount ---

    @Test
    void getAccount_shouldReturnDTOWhenFound() {
        when(repository.findById(testId)).thenReturn(Optional.of(testAccount));

        AccResponseDTO result = accountService.getAccount(testId.toString());

        assertThat(result.getAccountId()).isEqualTo(testId.toString());
        assertThat(result.getAccountName()).isEqualTo("John's Savings");
    }

    @Test
    void getAccount_shouldThrowWhenNotFound() {
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(testId.toString()))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("not associated with any account");
    }

    // --- getAccountByCustomerId ---

    @Test
    void getAccountByCustomerId_shouldReturnListWhenFound() {
        when(repository.findByCustomerId(testAccount.getCustomerId()))
                .thenReturn(List.of(testAccount));

        List<AccResponseDTO> result = accountService.getAccountByCustomerId(testAccount.getCustomerId().toString());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountName()).isEqualTo("John's Savings");
    }

    @Test
    void getAccountByCustomerId_shouldThrowWhenNotFound() {
        UUID customerId = UUID.randomUUID();
        when(repository.findByCustomerId(customerId)).thenReturn(List.of());

        assertThatThrownBy(() -> accountService.getAccountByCustomerId(customerId.toString()))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("No account found for customer id");
    }

    // --- createAccount ---

    @Test
    void createAccount_shouldValidateCustomerAndSave() {
        // Simulate customer-service returning a valid customer
        when(customerServiceGrpcClient.getCustomerById(requestDTO.getCustomerId()))
                .thenReturn(GetCustomerResponse.newBuilder().setFirstName("John").build());
        when(repository.existsByAccountNumber(any())).thenReturn(false);
        // Simulate JPA assigning a UUID on save - @GeneratedValue only fires with a real DB
        when(repository.save(any(account.class))).thenAnswer(inv -> {
            account a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AccResponseDTO result = accountService.createAccount(requestDTO);

        assertThat(result.getAccountName()).isEqualTo("John's Savings");
        // Verify save was actually called - not just the mapper
        verify(repository).save(any(account.class));
    }

    @Test
    void createAccount_shouldThrowWhenCustomerDoesNotExist() {
        // Simulate customer-service returning NOT_FOUND - gRPC throws StatusRuntimeException
        when(customerServiceGrpcClient.getCustomerById(requestDTO.getCustomerId()))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        assertThatThrownBy(() -> accountService.createAccount(requestDTO))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found");

        // Account should never be saved if customer doesn't exist
        verify(repository, never()).save(any());
    }

    // --- updateAccount ---

    @Test
    void updateAccount_shouldUpdateAndReturnDTO() {
        when(repository.findById(testId)).thenReturn(Optional.of(testAccount));
        when(repository.save(any(account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccResponseDTO result = accountService.updateAccount(testId.toString(), requestDTO);

        assertThat(result.getAccountName()).isEqualTo("John's Savings");
        verify(repository).save(any(account.class));
    }

    @Test
    void updateAccount_shouldThrowWhenAccountNotFound() {
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(testId.toString(), requestDTO))
                .isInstanceOf(AccountNotFoundException.class);

        verify(repository, never()).save(any());
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_shouldDeleteWhenFound() {
        when(repository.findById(testId)).thenReturn(Optional.of(testAccount));

        accountService.deleteAccount(testId.toString());

        verify(repository).deleteById(testId);
    }

    @Test
    void deleteAccount_shouldThrowWhenNotFound() {
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount(testId.toString()))
                .isInstanceOf(AccountNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
