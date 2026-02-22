package com.pm.customerservice.grpc;

import com.pm.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceGrpcClient.class);
    private final AccountServGrpc.AccountServBlockingStub stub;

    public AccountServiceGrpcClient(
            @Value("${proto.service.address:localhost}") String serverAddress,
            @Value("${proto.service.grpc.port:9091}") int serverPort
    ){
        log.info("connecting to Account Service on GRPC server on {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext().build();
        stub = AccountServGrpc.newBlockingStub(channel);
    }
    public CreateAccResponse createAccount(String AccName, String customerId,String type,
    String status, String balance ){


        CreateAccRequest request = CreateAccRequest.newBuilder()
                .setAccName(AccName)
                .setBalance(balance)
                .setCustomerId(customerId)
                .setType(AccType.valueOf(type.toUpperCase()))
                .setStatus(AccStatus.valueOf(status.toUpperCase()))
                .build();
        CreateAccResponse response = stub.createAcc(request);
        log.info("recieved response from Account service via GRPC : {}", response);
        return response;
    }
}
