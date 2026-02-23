package com.pm.customerservice.Service;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Exceptions.CustomerNotFoundException;
import com.pm.customerservice.Exceptions.EmailAlreadyExistException;
import com.pm.customerservice.Exceptions.EmailDoesNotExist;
import com.pm.customerservice.Repo.customerRepo;
import com.pm.customerservice.grpc.AccountServiceGrpcClient;
import com.pm.customerservice.mapper.Mapper;
import com.pm.customerservice.model.customer;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Validated
@AllArgsConstructor
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final customerRepo repository;
    private final AccountServiceGrpcClient accGrpcClient;

    public List<customerResponseDTO> getAllCustomers() {
        List<customer> list = repository.findAll();
        return list.stream().map(Mapper::toDTO).toList();
    }

    public customerResponseDTO getCustomerById(UUID id) {
        customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("profile Id not found: " + id));
        return Mapper.toDTO(customer);
    }

    public String getCustomerByEmail(String email) {
        if (!repository.existsByEmail(email)) {
            throw new EmailDoesNotExist("This email is not associated with any customer");
        }
        return repository.findByEmail(email).getId().toString();
    }

    public customerResponseDTO createCustomer(@Valid customerRequestDTO customerRequestDTO) {
        if (repository.existsByEmail(customerRequestDTO.getEmail())) {
            throw new EmailAlreadyExistException("A user with this email already exist");
        }

        // Save the customer first so we have an ID to pass to account-service
        customer customer = Mapper.toEntity(customerRequestDTO);
        repository.save(customer);

        // Auto-create a default savings account for the new customer via gRPC.
        // This is best-effort. if account-service is unavailable the customer
        // is still created. Accounts can always be created manually later.
        try {
            accGrpcClient.createAccount(
                    customerRequestDTO.getFirstName() + "'s Savings Account",
                    customer.getId().toString(),
                    "SAVINGS",
                    "ACTIVE",
                    "0.00"
            );
            log.info("Default savings account created for customer: {}", customer.getId());
        } catch (Exception e) {
            log.warn("Could not auto-create account for customer {}: {}", customer.getId(), e.getMessage());
        }

        return Mapper.toDTO(customer);
    }

    public customerResponseDTO updateCustomer(UUID id, @Valid customerRequestDTO customerRequest) {
        if (repository.existsByEmailAndIdNot(customerRequest.getEmail(), id)) {
            throw new EmailAlreadyExistException("A user with this email already exist: " + customerRequest.getEmail());
        }
        customer updateCust = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("profile Id not found: "));
        updateCust.setFirstName(customerRequest.getFirstName());
        updateCust.setLastName(customerRequest.getLastName());
        updateCust.setEmail(customerRequest.getEmail());
        updateCust.setAddress(customerRequest.getAddress());
        updateCust.setBirthDate(LocalDate.parse(customerRequest.getBirthDate()));
        repository.save(updateCust);
        return Mapper.toDTO(updateCust);
    }

    public void deleteCustomer(UUID id) {
        repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("profile Id not found: " + id));
        repository.deleteById(id);
    }
}
