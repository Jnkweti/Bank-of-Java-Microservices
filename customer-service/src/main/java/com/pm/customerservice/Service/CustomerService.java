package com.pm.customerservice.Service;

//import com.pm.customerservice.DTO.customerResponseDTO;
//import com.pm.customerservice.DTO.customerRequestDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Validated
public class CustomerService {
    @Autowired
    private  customerRepo repository;
    private AccountServiceGrpcClient AccGrpcClient;

    public List<customerResponseDTO> getAllCustomers(){
        List<customer> list =  repository.findAll();

        return list.stream()
                .map(customer -> Mapper.toDTO(customer)).toList();
    }

    public customerResponseDTO getCustomerById(UUID id){
        customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("profile Id not found: " + id));
        return Mapper.toDTO(customer);
    }
    public String getCustomerByEmail(String email){
        if(!repository.existsByEmail(email)){
            throw new EmailDoesNotExist("This email is not associated with any customer");
        }
         return repository.findByEmail(email).getId().toString();
    }

    public customerResponseDTO createCustomer(@Valid customerRequestDTO customerRequestDTO){
        if(repository.existsByEmail(customerRequestDTO.getEmail())){
            throw new EmailAlreadyExistException("A user with this email already exist");
        }

        customer customer = Mapper.toEntity(customerRequestDTO);
        repository.save(customer);
        return Mapper.toDTO(customer);
    }

    public customerResponseDTO updateCustomer(UUID id, @Valid customerRequestDTO customer){
        if(repository.existsByEmailAndIdNot((customer.getEmail()), id)) {
            throw new EmailAlreadyExistException("A user with this email already exist: " + customer.getEmail());
        }
        customer updateCust = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("profile Id not found: "));
        updateCust.setFirstName(customer.getFirstName());
        updateCust.setLastName(customer.getLastName());
        updateCust.setEmail(customer.getEmail());
        updateCust.setAddress(customer.getAddress());
        updateCust.setBirthDate(LocalDate.parse(customer.getBirthDate()));
        repository.save(updateCust);

        return Mapper.toDTO(updateCust);
    }

    public void deleteCustomer(UUID id){
        repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("profile Id not found: " + id));
        repository.deleteById(id);
    }

}
