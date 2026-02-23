package com.pm.customerservice.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Exceptions.CustomerNotFoundException;
import com.pm.customerservice.Exceptions.EmailAlreadyExistException;
import com.pm.customerservice.Service.CustomerService;
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

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testId;
    private customerResponseDTO responseDTO;
    private customerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        responseDTO = new customerResponseDTO();
        responseDTO.setId(testId.toString());
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setEmail("john@example.com");
        responseDTO.setAddress("123 Main St");
        responseDTO.setBirthDate("1990-05-15");

        requestDTO = new customerRequestDTO();
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setAddress("123 Main St");
        requestDTO.setBirthDate("1990-05-15");
    }

    // --- GET /Profile ---

    @Test
    void getAllCustomers_shouldReturn200WithList() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/Profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void getAllCustomers_shouldReturn200WithEmptyList() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/Profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /Profile/{id} ---

    @Test
    void getProfile_shouldReturn200WhenFound() throws Exception {
        when(customerService.getCustomerById(testId)).thenReturn(responseDTO);

        mockMvc.perform(get("/Profile/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.id").value(testId.toString()));
    }

    @Test
    void getProfile_shouldReturn404WhenNotFound() throws Exception {
        when(customerService.getCustomerById(testId))
                .thenThrow(new CustomerNotFoundException("profile Id not found: " + testId));

        mockMvc.perform(get("/Profile/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Profile Not Found"))
                .andExpect(jsonPath("$.message").value("profile Id not found: " + testId));
    }

    // --- POST /Profile ---

    @Test
    void createProfile_shouldReturn200WhenValid() throws Exception {
        when(customerService.createCustomer(any(customerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/Profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.id").value(testId.toString()));
    }

    @Test
    void createProfile_shouldReturn400WhenValidationFails() throws Exception {
        requestDTO.setEmail("not-an-email");
        requestDTO.setFirstName("");

        mockMvc.perform(post("/Profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createProfile_shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(customerService.createCustomer(any(customerRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistException("A user with this email already exist"));

        mockMvc.perform(post("/Profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email Already Exists"));
    }

    // --- PUT /Profile/{id} ---

    @Test
    void updateProfile_shouldReturn200WhenValid() throws Exception {
        when(customerService.updateCustomer(eq(testId), any(customerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/Profile/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void updateProfile_shouldReturn404WhenNotFound() throws Exception {
        when(customerService.updateCustomer(eq(testId), any(customerRequestDTO.class)))
                .thenThrow(new CustomerNotFoundException("profile Id not found: "));

        mockMvc.perform(put("/Profile/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Profile Not Found"));
    }

    @Test
    void updateProfile_shouldReturn409WhenEmailConflict() throws Exception {
        when(customerService.updateCustomer(eq(testId), any(customerRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistException("A user with this email already exist"));

        mockMvc.perform(put("/Profile/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email Already Exists"));
    }

    // --- DELETE /Profile/{id} ---

    @Test
    void deleteProfile_shouldReturn200WhenFound() throws Exception {
        doNothing().when(customerService).deleteCustomer(testId);

        mockMvc.perform(delete("/Profile/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile has been deleted!"));
    }

    @Test
    void deleteProfile_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new CustomerNotFoundException("profile Id not found: " + testId))
                .when(customerService).deleteCustomer(testId);

        mockMvc.perform(delete("/Profile/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Profile Not Found"));
    }
}
