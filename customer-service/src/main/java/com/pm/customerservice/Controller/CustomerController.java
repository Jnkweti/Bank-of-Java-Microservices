package com.pm.customerservice.Controller;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Service.CustomerService;
import com.pm.customerservice.mapper.Mapper;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/Profile")
@AllArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<customerResponseDTO> getProfile(@PathVariable UUID id){
        return ResponseEntity.ok().body(customerService.getCustomerById(id));
    }
    @PostMapping
    public ResponseEntity<customerResponseDTO> createProfile( @RequestBody customerRequestDTO customer){
        return ResponseEntity.ok().body(customerService.createCustomer(customer));
    }
    @PutMapping("/{id}")
    public ResponseEntity<customerResponseDTO> updateProfile(@PathVariable UUID id, @RequestBody customerRequestDTO customer){
        return ResponseEntity.ok().body(customerService.updateCustomer(id, customer));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProfile(@PathVariable UUID id){
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().body("Profile has been deleted!");
    }


}
