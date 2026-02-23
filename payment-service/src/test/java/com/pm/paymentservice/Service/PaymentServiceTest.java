package com.pm.paymentservice.Service;

import com.pm.paymentservice.DTO.PaymentEventDTO;
import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Enum.PaymentStatus;
import com.pm.paymentservice.Enum.PaymentType;
import com.pm.paymentservice.Exception.AccountNotActiveException;
import com.pm.paymentservice.Exception.InsufficientFundsException;
import com.pm.paymentservice.Exception.PaymentNotFoundException;
import com.pm.paymentservice.GRPC.AccountServiceGrpcClient;
import com.pm.paymentservice.Kafka.PaymentEventProducer;
import com.pm.paymentservice.Repository.paymentRepo;
import com.pm.paymentservice.model.payment;
import com.pm.proto.AccStatus;
import com.pm.proto.Account;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private paymentRepo repository;
    @Mock private AccountServiceGrpcClient accountServiceGrpcClient;
    @Mock private PaymentEventProducer eventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private String fromAccountId;
    private String toAccountId;
    private Account activeFromAccount;
    private Account activeToAccount;
    private PaymentRequestDTO validRequest;
    private payment testPayment;

    @BeforeEach
    void setUp() {
        fromAccountId = UUID.randomUUID().toString();
        toAccountId   = UUID.randomUUID().toString();

        // Proto Account with ACTIVE status and sufficient balance
        activeFromAccount = Account.newBuilder()
                .setId(fromAccountId)
                .setStatus(AccStatus.ACTIVE)
                .setBalance("500.00")
                .build();

        activeToAccount = Account.newBuilder()
                .setId(toAccountId)
                .setStatus(AccStatus.ACTIVE)
                .setBalance("100.00")
                .build();

        validRequest = new PaymentRequestDTO();
        validRequest.setFromAccountId(fromAccountId);
        validRequest.setToAccountId(toAccountId);
        validRequest.setAmount("100.00");
        validRequest.setType(PaymentType.TRANSFER);
        validRequest.setDescription("Test payment");

        // A fully populated entity used in read-path tests
        testPayment = new payment();
        testPayment.setId(UUID.randomUUID());
        testPayment.setFromAccountId(fromAccountId);
        testPayment.setToAccountId(toAccountId);
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setStatus(PaymentStatus.COMPLETED);
        testPayment.setType(PaymentType.TRANSFER);
        testPayment.setDescription("Test payment");
        testPayment.setCreatedAt(LocalDateTime.now());
        testPayment.setUpdatedAt(LocalDateTime.now());
    }

    // Helper: configures repository.save() to simulate what JPA would do.
    // @PrePersist / @PreUpdate only fire with a real JPA context.
    // In unit tests we have to set id, createdAt, and updatedAt ourselves
    // so buildEvent() and PaymentMapper.toDTO() do not NPE.
    private void mockSaveWithJpaLifecycle() {
        when(repository.save(any(payment.class))).thenAnswer(inv -> {
            payment p = inv.getArgument(0);
            if (p.getId() == null)        p.setId(UUID.randomUUID());
            if (p.getCreatedAt() == null) p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
    }

    // --- getAllPayments ---

    @Test
    void getAllPayments_shouldReturnMappedList() {
        when(repository.findAll()).thenReturn(List.of(testPayment));

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromAccountId()).isEqualTo(fromAccountId);
        assertThat(result.get(0).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void getAllPayments_shouldReturnEmptyList_whenNoPayments() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(paymentService.getAllPayments()).isEmpty();
    }

    // --- getPayment ---

    @Test
    void getPayment_shouldReturnDTO_whenFound() {
        when(repository.findById(testPayment.getId())).thenReturn(Optional.of(testPayment));

        PaymentResponseDTO result = paymentService.getPayment(testPayment.getId().toString());

        assertThat(result.getPaymentId()).isEqualTo(testPayment.getId().toString());
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void getPayment_shouldThrow_whenNotFound() {
        UUID randomId = UUID.randomUUID();
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(randomId.toString()))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(randomId.toString());
    }

    // --- getPaymentsByAccount ---

    @Test
    void getPaymentsByAccount_shouldReturnPaymentsForAccount() {
        when(repository.findByFromAccountIdOrToAccountId(fromAccountId, fromAccountId))
                .thenReturn(List.of(testPayment));

        List<PaymentResponseDTO> result = paymentService.getPaymentsByAccount(fromAccountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromAccountId()).isEqualTo(fromAccountId);
    }

    // --- processPayment (happy path) ---

    @Test
    void processPayment_shouldCompleteAndPublishEvent_whenValid() {
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(activeFromAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(activeToAccount);
        mockSaveWithJpaLifecycle();

        PaymentResponseDTO result = paymentService.processPayment(validRequest);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        // Event must be published exactly once after COMPLETED
        verify(eventProducer, times(1)).publishPaymentEvent(any(PaymentEventDTO.class));
    }

    @Test
    void processPayment_shouldUpdateBothBalancesViaGrpc() {
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(activeFromAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(activeToAccount);
        mockSaveWithJpaLifecycle();

        paymentService.processPayment(validRequest);

        // Debit and credit are two separate gRPC calls
        verify(accountServiceGrpcClient).updateAccountBalance(eq(activeFromAccount), eq("400.00"));
        verify(accountServiceGrpcClient).updateAccountBalance(eq(activeToAccount), eq("200.00"));
    }

    // --- processPayment (business rule violations) ---

    @Test
    void processPayment_shouldThrow_whenFromAccountIsNotActive() {
        Account frozenAccount = Account.newBuilder()
                .setId(fromAccountId)
                .setStatus(AccStatus.FROZEN)
                .setBalance("500.00")
                .build();
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(frozenAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(activeToAccount);

        assertThatThrownBy(() -> paymentService.processPayment(validRequest))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining(fromAccountId);

        verify(repository, never()).save(any());
        verify(eventProducer, never()).publishPaymentEvent(any());
    }

    @Test
    void processPayment_shouldThrow_whenToAccountIsNotActive() {
        Account frozenAccount = Account.newBuilder()
                .setId(toAccountId)
                .setStatus(AccStatus.FROZEN)
                .setBalance("100.00")
                .build();
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(activeFromAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(frozenAccount);

        assertThatThrownBy(() -> paymentService.processPayment(validRequest))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining(toAccountId);

        verify(repository, never()).save(any());
    }

    @Test
    void processPayment_shouldThrow_whenInsufficientFunds() {
        // Amount is 100.00 but from account only has 50.00
        validRequest.setAmount("600.00");
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(activeFromAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(activeToAccount);

        assertThatThrownBy(() -> paymentService.processPayment(validRequest))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

        verify(repository, never()).save(any());
        verify(eventProducer, never()).publishPaymentEvent(any());
    }

    // --- processPayment (failure / compensating transaction) ---

    @Test
    void processPayment_shouldMarkFailed_andPublishEvent_whenBalanceUpdateFails() {
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(activeFromAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(activeToAccount);
        mockSaveWithJpaLifecycle();
        // Simulate gRPC call failing mid-flight (e.g. account-service is down)
        doThrow(new RuntimeException("gRPC connection refused"))
                .when(accountServiceGrpcClient).updateAccountBalance(any(), any());

        PaymentResponseDTO result = paymentService.processPayment(validRequest);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        // Event must still be published — downstream services need to know about the failure
        verify(eventProducer, times(1)).publishPaymentEvent(any(PaymentEventDTO.class));
    }

    @Test
    void processPayment_shouldAttemptDebitReversal_whenCreditFails() {
        when(accountServiceGrpcClient.getAccountById(fromAccountId)).thenReturn(activeFromAccount);
        when(accountServiceGrpcClient.getAccountById(toAccountId)).thenReturn(activeToAccount);
        mockSaveWithJpaLifecycle();
        // First updateAccountBalance call (debit) succeeds, second (credit) fails
        doNothing()
                .doThrow(new RuntimeException("Credit failed"))
                .when(accountServiceGrpcClient).updateAccountBalance(any(), any());

        PaymentResponseDTO result = paymentService.processPayment(validRequest);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        // 3 calls: debit, credit (throws), reversal attempt
        verify(accountServiceGrpcClient, times(3)).updateAccountBalance(any(), any());
    }
}
