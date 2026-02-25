package com.pm.apigateway.Controller;

import com.pm.proto.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentGatewayController {

    @GrpcClient("payment-service")
    private PaymentServiceGrpc.PaymentServiceBlockingStub stub;

    private Map<String, String> paymentToMap(Payment payment) {
        return Map.of(
                "id", payment.getId(),
                "fromAccountId", payment.getFromAccountId(),
                "toAccountId", payment.getToAccountId(),
                "amount", payment.getAmount(),
                "status", payment.getStatus().name(),
                "type", payment.getType().name(),
                "description", payment.getDescription(),
                "createdAt", payment.getCreatedAt()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> processPayment(@RequestBody Map<String, String> body) {
        ProcessPaymentRequest request = ProcessPaymentRequest.newBuilder()
                .setFromAccountId(body.get("fromAccountId"))
                .setToAccountId(body.get("toAccountId"))
                .setAmount(body.get("amount"))
                .setType(PaymentType.valueOf(body.get("type")))
                .setDescription(body.get("description"))
                .build();

        ProcessPaymentResponse response = stub.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentToMap(response.getPayment()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getPaymentById(@PathVariable String id) {
        GetPaymentByIdRequest request = GetPaymentByIdRequest.newBuilder().setId(id).build();
        GetPaymentByIdResponse response = stub.getPaymentById(request);
        return ResponseEntity.ok(paymentToMap(response.getPayment()));
    }

    // returns a list since one account can have many payments
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Map<String, String>>> getPaymentsByAccount(@PathVariable String accountId) {
        GetPaymentsByAccountRequest request = GetPaymentsByAccountRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        GetPaymentsByAccountResponse response = stub.getPaymentsByAccount(request);
        List<Map<String, String>> payments = response.getPaymentsList().stream()
                .map(this::paymentToMap)
                .toList();

        return ResponseEntity.ok(payments);
    }
}
