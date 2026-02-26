package com.pm.apigateway.Controller;

import com.pm.proto.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerGatewayController {

    @GrpcClient("customer-service")
    private CustomerServiceGrpc.CustomerServiceBlockingStub stub;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> GetCustomerById(@PathVariable String id) {

        GetCustomerByIdRequest request = GetCustomerByIdRequest.newBuilder().setId(id).build();
        GetCustomerResponse response = stub.getCustomerById(request);

        return ResponseEntity.ok(Map.of(
                "id", response.getId(),
                "firstName", response.getFirstName(),
                "lastName", response.getLastName(),
                "email", response.getEmail(),
                "address", response.getAddress(),
                "birthDate", response.getBirthDate(),
                "registerDate", response.getRegisterDate()
        ));

    }

    @PostMapping
    public ResponseEntity<Map<String, String>> CreateCustomer(@RequestBody Map<String, String> body) {

        CreateCustomerRequest request = CreateCustomerRequest.newBuilder()
                .setFirstName(body.get("firstName"))
                .setLastName(body.get("lastName"))
                .setAddress(body.get("address"))
                .setEmail(body.get("email"))
                .setBirthDate(body.get("birthDate"))
                .build();

        CreateCustomerResponse response = stub.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", response.getId(),
                "firstName", response.getFirstName(),
                "lastName", response.getLastName(),
                "email", response.getEmail(),
                "address", response.getAddress(),
                "birthDate", response.getBirthDate(),
                "registerDate", response.getRegisterDate()
        ));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> UpdateCustomer(@RequestBody Map<String, String> body, @PathVariable String id) {

        UpdateCustomerRequest request = UpdateCustomerRequest.newBuilder().setId(id)
                .setFirstName(body.get("firstName"))
                .setLastName(body.get("lastName"))
                .setAddress(body.get("address"))
                .setEmail(body.get("email"))
                .setBirthDate(body.get("birthDate"))
                .build();

        UpdateCustomerResponse response = stub.updateCustomer(request);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "id", response.getId(),
                "firstName", response.getFirstName(),
                "lastName", response.getLastName(),
                "email", response.getEmail(),
                "address", response.getAddress(),
                "birthDate", response.getBirthDate(),
                "registerDate", response.getRegisterDate()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable String id) {
        DeleteCustomerRequest request = DeleteCustomerRequest.newBuilder().setId(id).build();
        DeleteCustomerResponse response = stub.deleteCustomer(request);
        return ResponseEntity.status(HttpStatus.OK).body(response.getMessage());
    }



}
