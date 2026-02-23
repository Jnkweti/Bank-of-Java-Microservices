package com.pm.paymentservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Enum.PaymentType;
import com.pm.paymentservice.Exception.InsufficientFundsException;
import com.pm.paymentservice.Exception.AccountNotActiveException;
import com.pm.paymentservice.Exception.PaymentNotFoundException;
import com.pm.paymentservice.Service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest spins up only the web layer: controller, filters, and @RestControllerAdvice.
// No Spring Data, no gRPC beans. The PaymentService is replaced entirely by a @MockBean.
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PaymentService paymentService;

    private String testPaymentId;
    private String testAccountId;
    private PaymentResponseDTO responseDTO;
    private PaymentRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID().toString();
        testAccountId = UUID.randomUUID().toString();

        responseDTO = new PaymentResponseDTO();
        responseDTO.setPaymentId(testPaymentId);
        responseDTO.setFromAccountId(testAccountId);
        responseDTO.setToAccountId(UUID.randomUUID().toString());
        responseDTO.setAmount("100.00");
        responseDTO.setStatus("COMPLETED");
        responseDTO.setType("TRANSFER");
        responseDTO.setDescription("Test payment");
        responseDTO.setCreatedAt("2025-01-01T10:00:00");
        responseDTO.setUpdatedAt("2025-01-01T10:00:05");

        validRequest = new PaymentRequestDTO();
        validRequest.setFromAccountId(testAccountId);
        validRequest.setToAccountId(UUID.randomUUID().toString());
        validRequest.setAmount("100.00");
        validRequest.setType(PaymentType.TRANSFER);
        validRequest.setDescription("Test payment");
    }

    // --- GET /api/payments ---

    @Test
    void getAllPayments_shouldReturn200WithList() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(testPaymentId))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].amount").value("100.00"));
    }

    @Test
    void getAllPayments_shouldReturn200WithEmptyList() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of());

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /api/payments/{id} ---

    @Test
    void getPayment_shouldReturn200WhenFound() throws Exception {
        when(paymentService.getPayment(testPaymentId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/payments/{id}", testPaymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(testPaymentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getPayment_shouldReturn404WhenNotFound() throws Exception {
        when(paymentService.getPayment(testPaymentId))
                .thenThrow(new PaymentNotFoundException("Payment not found with id: " + testPaymentId));

        mockMvc.perform(get("/api/payments/{id}", testPaymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payment Not Found"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: " + testPaymentId));
    }

    // --- GET /api/payments/account/{accountId} ---

    @Test
    void getPaymentsByAccount_shouldReturn200WithList() throws Exception {
        when(paymentService.getPaymentsByAccount(testAccountId)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/payments/account/{accountId}", testAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromAccountId").value(testAccountId));
    }

    // --- POST /api/payments ---

    @Test
    void processPayment_shouldReturn200WhenValid() throws Exception {
        when(paymentService.processPayment(any(PaymentRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(testPaymentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void processPayment_shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        // Missing fromAccountId triggers @NotBlank validation
        validRequest.setFromAccountId("");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.fromAccountId").exists());
    }

    @Test
    void processPayment_shouldReturn400WhenPaymentTypeIsNull() throws Exception {
        validRequest.setType(null);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.type").exists());
    }

    @Test
    void processPayment_shouldReturn422WhenInsufficientFunds() throws Exception {
        when(paymentService.processPayment(any(PaymentRequestDTO.class)))
                .thenThrow(new InsufficientFundsException(
                        "Insufficient funds. Available: 50.00, Required: 100.00"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Insufficient Funds"))
                .andExpect(jsonPath("$.message").value("Insufficient funds. Available: 50.00, Required: 100.00"));
    }

    @Test
    void processPayment_shouldReturn422WhenAccountNotActive() throws Exception {
        when(paymentService.processPayment(any(PaymentRequestDTO.class)))
                .thenThrow(new AccountNotActiveException(
                        "Source account is not active: " + testAccountId));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Account Not Active"))
                .andExpect(jsonPath("$.message").value("Source account is not active: " + testAccountId));
    }
}
