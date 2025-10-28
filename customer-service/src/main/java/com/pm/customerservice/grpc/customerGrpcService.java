package com.pm.customerservice.grpc;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Repo.customerRepo;
import com.pm.customerservice.Service.CustomerService;
import com.pm.customerservice.mapper.Mapper;
import com.pm.proto.*;
import io.grpc.stub.StreamObserver;
import jakarta.validation.Valid;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@GrpcService
public class customerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {

    @Autowired
    private CustomerService custService;

    private static final Logger log = LoggerFactory.getLogger(customerGrpcService.class);

    @Override
    public void createCustomer(CreateCustomerRequest customerRequest,
    StreamObserver<CreateCustomerResponse> responseObserver){
        log.info("createProfile Request recieved {}", customerRequest.toString());
            //pass argument customerRequest through customerRequestDTO validations.
            customerRequestDTO request = new customerRequestDTO();
            request.setFirstName(customerRequest.getFirstName());
            request.setLastName(customerRequest.getLastName());
            request.setEmail(customerRequest.getEmail());
            request.setAddress(customerRequest.getAddress());
            request.setBirthDate(customerRequest.getBirthDate());

            custService.createCustomer( request);

            CreateCustomerResponse response = CreateCustomerResponse.newBuilder()
                    .setFirstName(customerRequest.getFirstName())
                    .setLastName(customerRequest.getLastName())
                    .setStatus("Profile has been created!")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
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

//    @Override
//    public void getCustomerById(GetCustomerByIdRequest request, StreamObserver<GetCustomerResponse> responseObserver) {
//        log.info("getCustomerById Request recieved, profile id: {}", request.toString());
//
//        custService.getCustomerById(UUID.fromString(request.getId()));
//    }
}
