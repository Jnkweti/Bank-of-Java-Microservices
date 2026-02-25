package com.pm.accountservice.GRPC;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.Service.accountService;
import com.pm.proto.*;
import com.pm.proto.AccountServGrpc.AccountServImplBase;
import io.grpc.Status;
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

    // Reusable helper - builds proto Account message from a response DTO
    private Account buildAccountProto(AccResponseDTO result) {
        return Account.newBuilder()
                .setId(result.getAccountId())
                .setAccName(result.getAccountName())
                .setCustomerId(result.getCustomerId())
                .setBalance(result.getAccountBalance())
                .setType(AccType.valueOf(result.getAccountType()))
                .setStatus(AccStatus.valueOf(result.getAccountStatus()))
                .setAccountNumber(result.getAccountNumber())
                .build();
    }

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

            CreateAccResponse response = CreateAccResponse.newBuilder()
                    .setAccount(buildAccountProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in createAcc: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAccById(GetAccIdRequest request, StreamObserver<GetAccIDResponse> responseObserver) {
        try {
            log.info("getAccById request received, id: {}", request.getId());

            AccResponseDTO result = accService.getAccount(request.getId());

            GetAccIDResponse response = GetAccIDResponse.newBuilder()
                    .setAccount(buildAccountProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getAccById: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAccByCusId(GetAccIdRequest request, StreamObserver<GetAccIDResponse> responseObserver) {
        try {
            log.info("getAccByCusId request received, customerId: {}", request.getId());

            AccResponseDTO result = accService.getAccountByCustomerId(request.getId());

            GetAccIDResponse response = GetAccIDResponse.newBuilder()
                    .setAccount(buildAccountProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getAccByCusId: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateAcc(UpdateAccRequest request, StreamObserver<UpdateAccResponse> responseObserver) {
        try {
            log.info("updateAcc request received, id: {}", request.getId());

            AccRequestDTO dto = new AccRequestDTO();
            dto.setAccountName(request.getAccName());
            dto.setType(AccountType.valueOf(request.getType().name()));
            dto.setStatus(AccountStatus.valueOf(request.getStat().name()));
            dto.setBalance(request.getBalance());

            AccResponseDTO result = accService.updateAccount(request.getId(), dto);

            UpdateAccResponse response = UpdateAccResponse.newBuilder()
                    .setAccount(buildAccountProto(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in updateAcc: {}", e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteAccIdRequest(DeleteAccByIdRequest request, StreamObserver<DeleteAccByIdResponse> responseObserver) {
        try {
            log.info("deleteAcc request received, id: {}", request.getAccId());

            accService.deleteAccount(request.getAccId());

            DeleteAccByIdResponse response = DeleteAccByIdResponse.newBuilder()
                    .setStatus("Account " + request.getAccId() + " has been deleted")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in deleteAcc: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
