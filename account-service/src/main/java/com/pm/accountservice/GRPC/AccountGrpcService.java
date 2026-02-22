package com.pm.accountservice.GRPC;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.Service.accountService;
import com.pm.proto.*;
import com.pm.proto.AccountServGrpc.AccountServImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class AccountGrpcService extends AccountServImplBase {

    private static final Logger log = LoggerFactory.getLogger(AccountGrpcService.class);

    @Autowired
    private accountService accService;

    @Override
    public void createAcc(CreateAccRequest accRequest, StreamObserver<CreateAccResponse> responseObserver) {
        try {
            log.info("createAcc request received: {}", accRequest);

            AccRequestDTO dto = new AccRequestDTO();
            dto.setAccountName(accRequest.getAccName());
            dto.setBalance(accRequest.getBalance());
            dto.setCustomerId(accRequest.getCustomerId());
            dto.setType(AccountType.valueOf(accRequest.getType().name()));
            dto.setStatus(AccountStatus.valueOf(accRequest.getStatus().name()));

            AccResponseDTO result = accService.createAccount(dto);

            Account account = Account.newBuilder()
                    .setId(result.getAccountId())
                    .setAccName(result.getAccountName())
                    .setCustomerId(result.getCustomerId())
                    .setBalance(result.getAccountBalance())
                    .setType(AccType.valueOf(result.getAccountType()))
                    .setStatus(AccStatus.valueOf(result.getAccountStatus()))
                    .build();

            CreateAccResponse response = CreateAccResponse.newBuilder()
                    .setAccount(account)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating account: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
