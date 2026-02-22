package com.pm.customerservice.grpc;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Exceptions.Validation.GrpcValidation;
import com.pm.customerservice.Service.CustomerService;
import com.pm.proto.*;
import com.pm.proto.CustomerServiceGrpc.CustomerServiceImplBase;
import io.grpc.stub.StreamObserver;

import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.UUID;

@GrpcService
@Component
public class customerGrpcService extends CustomerServiceImplBase{

    @Autowired
    private CustomerService custService;
    @Autowired
    GrpcValidation grpcValidation;

    private static final Logger log = LoggerFactory.getLogger(customerGrpcService.class);

    @Override
    public void createCustomer(CreateCustomerRequest customerRequest,
    StreamObserver<CreateCustomerResponse> responseObserver){
        try {
            log.info("createProfile Request received {}", customerRequest.toString());

//          use base class validation checks fields passed
            grpcValidation.validate(customerRequest, responseObserver);

            // map gRPC → DTO
            customerRequestDTO dto = new customerRequestDTO();
            dto.setFirstName(customerRequest.getFirstName());
            dto.setLastName(customerRequest.getLastName());
            dto.setEmail(customerRequest.getEmail());
            dto.setAddress(customerRequest.getAddress());
            dto.setBirthDate(customerRequest.getBirthDate());

            // use base class validation checks fields passed
            grpcValidation.validate(dto, responseObserver);

            custService.createCustomer(dto);

            CreateCustomerResponse response = CreateCustomerResponse.newBuilder()
                    .setFirstName(dto.getFirstName())
                    .setLastName(dto.getLastName())
                    .setStatus("Profile has been created!")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
//            grpcValidation.handleException(e, responseObserver);
            throw new RuntimeException(e.getMessage());
        }
    }
    @Override
    public void deleteCustomer(DeleteCustomerRequest request, StreamObserver<DeleteCustomerResponse> responseObserver) {
        log.info("deleteProfile Request recieved, profile id: {}", request.toString());

        custService.deleteCustomer(UUID.fromString(request.getId()));

        DeleteCustomerResponse response = DeleteCustomerResponse.newBuilder()
                .setMessage("Profile id:" + request.getId() + " has been deleted!")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomerById(GetCustomerByIdRequest request, StreamObserver<GetCustomerResponse> responseObserver) {
        log.info("getCustomerById Request received, profile id: {}", request.toString());

        customerResponseDTO customer = custService.getCustomerById(UUID.fromString(request.getId()));


        GetCustomerResponse response = GetCustomerResponse.newBuilder()
                .setFirstName(customer.getFirstName())
                .setLastName(customer.getLastName())
                .setEmail(customer.getEmail())
                .setAddress(customer.getAddress())
                .setBirthDate(customer.getBirthDate())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateCustomer(UpdateCustomerRequest request, StreamObserver<UpdateCustomerResponse> responseObserver) {
        log.info("updateCustomer Request received, profile: {}", request.toString());
        // map gRPC -> DTO
        customerRequestDTO dto = new customerRequestDTO();
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setEmail(request.getEmail());
        dto.setAddress(request.getAddress());
        dto.setBirthDate(request.getBirthDate());
        // DTO -> repo
        custService.updateCustomer(UUID.fromString(request.getId()), dto);

        UpdateCustomerResponse response = UpdateCustomerResponse.newBuilder()
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setEmail(request.getEmail())
                .setAddress(request.getAddress())
                .setBirthDate(request.getBirthDate())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomerId(GetCustomerIdByEmailRequest request, StreamObserver<GetCustomerIdByEmailResponse> responseObserver) {
        log.info("GetCustomerIdByEmailRequest Request received, email: {}", request.toString());

        String id = custService.getCustomerByEmail(request.getEmail());

        GetCustomerIdByEmailResponse response = GetCustomerIdByEmailResponse.newBuilder()
                .setId(id)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
