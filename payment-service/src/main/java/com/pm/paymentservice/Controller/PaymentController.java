package com.pm.paymentservice.Controller;

import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    // Returns all payments (sent or received) involving a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(paymentService.getPaymentsByAccount(accountId));
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
}
