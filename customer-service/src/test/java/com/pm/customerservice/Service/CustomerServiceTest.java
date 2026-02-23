package com.pm.customerservice.Service;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Exceptions.CustomerNotFoundException;
import com.pm.customerservice.Exceptions.EmailAlreadyExistException;
import com.pm.customerservice.Exceptions.EmailDoesNotExist;
import com.pm.customerservice.Repo.customerRepo;
import com.pm.customerservice.grpc.AccountServiceGrpcClient;
import com.pm.customerservice.model.customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private customerRepo repository;

    @Mock
    private AccountServiceGrpcClient accGrpcClient;

    @InjectMocks
    private CustomerService customerService;

    private UUID testId;
    private customer testCustomer;
    private customerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testCustomer = new customer();
        testCustomer.setId(testId);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setAddress("123 Main St");
        testCustomer.setBirthDate(LocalDate.of(1990, 5, 15));
        testCustomer.setRegisterDate(LocalDate.now());

        requestDTO = new customerRequestDTO();
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setAddress("123 Main St");
        requestDTO.setBirthDate("1990-05-15");
    }

    // --- getAllCustomers ---

    @Test
    void getAllCustomers_shouldReturnListOfDTOs() {
        when(repository.findAll()).thenReturn(List.of(testCustomer));

        List<customerResponseDTO> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
        assertThat(result.get(0).getId()).isEqualTo(testId.toString());
    }

    @Test
    void getAllCustomers_shouldReturnEmptyListWhenNoCustomers() {
        when(repository.findAll()).thenReturn(List.of());

        List<customerResponseDTO> result = customerService.getAllCustomers();

        assertThat(result).isEmpty();
    }

    // --- getCustomerById ---

    @Test
    void getCustomerById_shouldReturnDTOWhenFound() {
        when(repository.findById(testId)).thenReturn(Optional.of(testCustomer));

        customerResponseDTO result = customerService.getCustomerById(testId);

        assertThat(result.getId()).isEqualTo(testId.toString());
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getCustomerById_shouldThrowWhenNotFound() {
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(testId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(testId.toString());
    }

    // --- getCustomerByEmail ---

    @Test
    void getCustomerByEmail_shouldReturnIdWhenFound() {
        when(repository.existsByEmail("john@example.com")).thenReturn(true);
        when(repository.findByEmail("john@example.com")).thenReturn(testCustomer);

        String result = customerService.getCustomerByEmail("john@example.com");

        assertThat(result).isEqualTo(testId.toString());
    }

    @Test
    void getCustomerByEmail_shouldThrowWhenEmailNotFound() {
        when(repository.existsByEmail("unknown@example.com")).thenReturn(false);

        assertThatThrownBy(() -> customerService.getCustomerByEmail("unknown@example.com"))
                .isInstanceOf(EmailDoesNotExist.class)
                .hasMessage("This email is not associated with any customer");
    }

    // --- createCustomer ---

    @Test
    void createCustomer_shouldSaveAndReturnDTO() {
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(repository.save(any(customer.class))).thenAnswer(inv -> inv.getArgument(0));

        customerResponseDTO result = customerService.createCustomer(requestDTO);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(repository).save(any(customer.class));
    }

    @Test
    void createCustomer_shouldThrowWhenEmailAlreadyExists() {
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(requestDTO))
                .isInstanceOf(EmailAlreadyExistException.class)
                .hasMessage("A user with this email already exist");

        verify(repository, never()).save(any());
    }

    // --- updateCustomer ---

    @Test
    void updateCustomer_shouldUpdateAndReturnDTO() {
        when(repository.existsByEmailAndIdNot(requestDTO.getEmail(), testId)).thenReturn(false);
        when(repository.findById(testId)).thenReturn(Optional.of(testCustomer));
        when(repository.save(any(customer.class))).thenAnswer(inv -> inv.getArgument(0));

        customerResponseDTO result = customerService.updateCustomer(testId, requestDTO);

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(repository).save(any(customer.class));
    }

    @Test
    void updateCustomer_shouldThrowWhenEmailConflictsWithAnotherCustomer() {
        when(repository.existsByEmailAndIdNot(requestDTO.getEmail(), testId)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(testId, requestDTO))
                .isInstanceOf(EmailAlreadyExistException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void updateCustomer_shouldThrowWhenCustomerNotFound() {
        when(repository.existsByEmailAndIdNot(requestDTO.getEmail(), testId)).thenReturn(false);
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(testId, requestDTO))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    // --- deleteCustomer ---

    @Test
    void deleteCustomer_shouldDeleteWhenFound() {
        when(repository.findById(testId)).thenReturn(Optional.of(testCustomer));

        customerService.deleteCustomer(testId);

        verify(repository).deleteById(testId);
    }

    @Test
    void deleteCustomer_shouldThrowWhenNotFound() {
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(testId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(testId.toString());

        verify(repository, never()).deleteById(any());
    }
}
