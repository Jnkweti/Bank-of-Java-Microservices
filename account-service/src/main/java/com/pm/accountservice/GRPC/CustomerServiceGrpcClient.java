package com.pm.accountservice.GRPC;

import com.pm.proto.CustomerServiceGrpc;
import com.pm.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class CustomerServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceGrpcClient.class);

    private final CustomerServiceGrpc.CustomerServiceBlockingStub stub;

    public CustomerServiceGrpcClient(
            @Value("${proto.service.address:localhost}") String serverAddress,
            @Value("${proto.service.grpc.port:9090}") int serverPort){
        log.info("connecting to Customer Service to grpc server on {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext().build();
        stub = CustomerServiceGrpc.newBlockingStub(channel);
    }
//    public GetCustomerResponse GetCustomerbyID(String id){
//        GetCustomerByIdRequest request = GetCustomerByIdRequest.newBuilder().setId(id).build();
//    }




}