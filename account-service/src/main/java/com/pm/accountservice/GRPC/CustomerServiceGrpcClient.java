package com.pm.accountservice.GRPC;

import com.pm.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceGrpcClient.class);
    private final CustomerServiceGrpc.CustomerServiceBlockingStub stub;

    public CustomerServiceGrpcClient(
            @Value("${proto.service.address:localhost}") String serverAddress,
            @Value("${proto.service.grpc.port:9090}") int serverPort) {
        log.info("Connecting to Customer Service gRPC server on {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();
        stub = CustomerServiceGrpc.newBlockingStub(channel);
    }

    public GetCustomerResponse getCustomerById(String customerId) {
        log.info("Fetching customer with id: {}", customerId);
        GetCustomerByIdRequest request = GetCustomerByIdRequest.newBuilder()
                .setId(customerId)
                .build();
        return stub.getCustomerById(request);
    }
}
