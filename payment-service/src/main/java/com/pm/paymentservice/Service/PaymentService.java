package com.pm.paymentservice.Service;

import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Enum.PaymentStatus;
import com.pm.paymentservice.Exception.AccountNotActiveException;
import com.pm.paymentservice.Exception.InsufficientFundsException;
import com.pm.paymentservice.Exception.PaymentNotFoundException;
import com.pm.paymentservice.GRPC.AccountServiceGrpcClient;
import com.pm.paymentservice.Mapper.PaymentMapper;
import com.pm.paymentservice.Repository.paymentRepo;
import com.pm.paymentservice.model.payment;
import com.pm.proto.Account;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final paymentRepo repository;
    private final AccountServiceGrpcClient accountServiceGrpcClient;

    public List<PaymentResponseDTO> getAllPayments() {
        return repository.findAll().stream()
                .map(PaymentMapper::toDTO)
                .toList();
    }

    public PaymentResponseDTO getPayment(String paymentId) {
        payment p = repository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return PaymentMapper.toDTO(p);
    }

    public List<PaymentResponseDTO> getPaymentsByAccount(String accountId) {
        // Fetches all payments where this account was the sender or the receiver.
        // The same value passed twice bc the repo query uses OR across two columns
        return repository.findByFromAccountIdOrToAccountId(accountId, accountId)
                .stream()
                .map(PaymentMapper::toDTO)
                .toList();
    }

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        // Fetch both accounts via gRPC
        // if either account doesn't exist, StatusRuntimeException is thrown here
        // and propagates up to the GlobalExceptionHandler as a 500.
        Account fromAccount;
        Account toAccount;
        try {
            fromAccount = accountServiceGrpcClient.getAccountById(request.getFromAccountId());
            toAccount = accountServiceGrpcClient.getAccountById(request.getToAccountId());
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Could not retrieve account details: " + e.getStatus().getDescription());
        }

        //Validate both accounts are ACTIVE
        // fROZEN or CLOSED accounts cannot send or receive payments.
        if (!fromAccount.getStatus().name().equals("ACTIVE")) {
            throw new AccountNotActiveException("Source account is not active: " + request.getFromAccountId());
        }
        if (!toAccount.getStatus().name().equals("ACTIVE")) {
            throw new AccountNotActiveException("Destination account is not active: " + request.getToAccountId());
        }

        // Validate sufficient balance
        BigDecimal amount = new BigDecimal(request.getAmount());
        BigDecimal fromBalance = new BigDecimal(fromAccount.getBalance());
        if (fromBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + fromBalance + ", Required: " + amount);
        }

        // Save payment as PENDING
        // record the payment first in case anything fails after this point,
        // we update status to failed so there's always an audit trail.
        payment p = PaymentMapper.toEntity(request);
        repository.save(p);
        log.info("Payment {} saved as PENDING", p.getId());

        // Debit source, Credit destination
        // This is a compensating transaction pattern:
        // If the credit fails after the debit succeeded, we reverse the debit.
        // This is a simplified Saga, in production you'd use a proper Saga orchestrator
        // or Kafka-based eventual consistency.
        try {
            BigDecimal newFromBalance = fromBalance.subtract(amount);
            accountServiceGrpcClient.updateAccountBalance(fromAccount, newFromBalance.toPlainString());

            BigDecimal newToBalance = new BigDecimal(toAccount.getBalance()).add(amount);
            accountServiceGrpcClient.updateAccountBalance(toAccount, newToBalance.toPlainString());
        } catch (Exception e) {
            log.error("Payment {} failed during account update: {}", p.getId(), e.getMessage());

            // Attempt to reverse the debit if it already happened
            try {
                accountServiceGrpcClient.updateAccountBalance(fromAccount, fromBalance.toPlainString());
                log.info("Debit reversed for payment {}", p.getId());
            } catch (Exception reverseEx) {
                log.error("CRITICAL: Could not reverse debit for payment {}. Manual intervention required.", p.getId());
            }

            p.setStatus(PaymentStatus.FAILED);
            repository.save(p);
            return PaymentMapper.toDTO(p);
        }

        // --- Step 6: Mark payment as COMPLETED ---
        p.setStatus(PaymentStatus.COMPLETED);
        repository.save(p);
        log.info("Payment {} completed successfully", p.getId());

        return PaymentMapper.toDTO(p);
    }
}
