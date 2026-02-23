package com.pm.accountservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.Exception.AccountNotFoundException;
import com.pm.accountservice.Exception.CustomerNotFoundException;
import com.pm.accountservice.Service.accountService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest only loads the web layer - controller, filters, exception handlers.
// It does NOT load the full Spring context, no DB, no gRPC. Fast and focused.
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @MockBean registers a Mockito mock into the Spring test context.
    // The controller gets this mock injected instead of a real accountService.
    @MockBean
    private accountService accService;

    @Autowired
    private ObjectMapper objectMapper;

    private String testId;
    private AccResponseDTO responseDTO;
    private AccRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID().toString();

        responseDTO = new AccResponseDTO();
        responseDTO.setAccountId(testId);
        responseDTO.setAccountName("John's Savings");
        responseDTO.setAccountNumber("BOJ-0000000001");
        responseDTO.setAccountBalance("500.00");
        responseDTO.setCustomerId(UUID.randomUUID().toString());
        responseDTO.setAccountType("SAVINGS");
        responseDTO.setAccountStatus("ACTIVE");
        responseDTO.setInterestRate("0.045");
        responseDTO.setLastUpdate("2026-01-01T10:00:00");

        requestDTO = new AccRequestDTO();
        requestDTO.setAccountName("John's Savings");
        requestDTO.setCustomerId(UUID.randomUUID().toString());
        requestDTO.setBalance("500.00");
        requestDTO.setType(AccountType.SAVINGS);
        requestDTO.setStatus(AccountStatus.ACTIVE);
    }

    // --- GET /api/accounts ---

    @Test
    void getAllAccounts_shouldReturn200WithList() throws Exception {
        when(accService.getAllAccounts()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(testId))
                .andExpect(jsonPath("$[0].accountName").value("John's Savings"))
                .andExpect(jsonPath("$[0].accountType").value("SAVINGS"));
    }

    @Test
    void getAllAccounts_shouldReturn200WithEmptyList() throws Exception {
        when(accService.getAllAccounts()).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /api/accounts/{id} ---

    @Test
    void getAccount_shouldReturn200WhenFound() throws Exception {
        when(accService.getAccount(testId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/accounts/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(testId))
                .andExpect(jsonPath("$.accountName").value("John's Savings"))
                .andExpect(jsonPath("$.accountBalance").value("500.00"));
    }

    @Test
    void getAccount_shouldReturn404WhenNotFound() throws Exception {
        when(accService.getAccount(testId))
                .thenThrow(new AccountNotFoundException("this id is not associated with any account"));

        mockMvc.perform(get("/api/accounts/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"))
                .andExpect(jsonPath("$.message").value("this id is not associated with any account"));
    }

    // --- POST /api/accounts ---

    @Test
    void createAccount_shouldReturn200WhenValid() throws Exception {
        when(accService.createAccount(any(AccRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(testId))
                .andExpect(jsonPath("$.accountNumber").value("BOJ-0000000001"));
    }

    @Test
    void createAccount_shouldReturn400WhenValidationFails() throws Exception {
        // Empty account name should trigger @NotBlank validation
        requestDTO.setAccountName("");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.accountName").exists());
    }

    @Test
    void createAccount_shouldReturn404WhenCustomerNotFound() throws Exception {
        when(accService.createAccount(any(AccRequestDTO.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: " + requestDTO.getCustomerId()));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer Not Found"));
    }

    // --- PUT /api/accounts/{id} ---

    @Test
    void updateAccount_shouldReturn200WhenValid() throws Exception {
        when(accService.updateAccount(eq(testId), any(AccRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/accounts/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("John's Savings"));
    }

    @Test
    void updateAccount_shouldReturn404WhenNotFound() throws Exception {
        when(accService.updateAccount(eq(testId), any(AccRequestDTO.class)))
                .thenThrow(new AccountNotFoundException("this id is not associated with any account"));

        mockMvc.perform(put("/api/accounts/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"));
    }

    // --- DELETE /api/accounts/{id} ---

    @Test
    void deleteAccount_shouldReturn200WhenFound() throws Exception {
        doNothing().when(accService).deleteAccount(testId);

        mockMvc.perform(delete("/api/accounts/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().string("Account has been deleted!"));
    }

    @Test
    void deleteAccount_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new AccountNotFoundException("this id is not associated with any account"))
                .when(accService).deleteAccount(testId);

        mockMvc.perform(delete("/api/accounts/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"));
    }
}
