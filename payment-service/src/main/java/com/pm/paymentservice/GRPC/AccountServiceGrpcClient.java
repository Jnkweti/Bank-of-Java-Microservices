package com.pm.paymentservice.GRPC;

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
            // Reads from application.yml - defaults to localhost if not set.
            // In AWS these will be overridden by environment variables pointing
            // to the actual account-service container address.
            @Value("${proto.account.address:localhost}") String serverAddress,
            @Value("${proto.account.grpc.port:9091}") int serverPort) {
        log.info("Connecting to Account Service gRPC on {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();
        stub = AccountServGrpc.newBlockingStub(channel);
    }

    // Fetches full account details by account ID.
    // Returns the proto Account message which has: id, customerId, accName, type, status, balance.
    public Account getAccountById(String accountId) {
        log.info("Fetching account by id: {}", accountId);
        GetAccIdRequest request = GetAccIdRequest.newBuilder()
                .setId(accountId)
                .build();
        return stub.getAccById(request).getAccount();
    }

    // Updates an account's balance while keeping all other fields (name, type, status) unchanged.
    // We pass the full account back because UpdateAccRequest requires all fields -
    // we use the existing values from the account and only change the balance.
    public void updateAccountBalance(Account account, String newBalance) {
        log.info("Updating balance for account: {} to {}", account.getId(), newBalance);
        UpdateAccRequest request = UpdateAccRequest.newBuilder()
                .setId(account.getId())
                .setAccName(account.getAccName())
                .setType(account.getType())
                .setStat(account.getStatus())
                .setBalance(newBalance)
                .build();
        stub.updateAcc(request);
    }
}
