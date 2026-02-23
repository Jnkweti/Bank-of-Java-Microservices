package com.pm.paymentservice.GRPC;

import com.pm.paymentservice.DTO.PaymentRequestDTO;
import com.pm.paymentservice.DTO.PaymentResponseDTO;
import com.pm.paymentservice.Enum.PaymentType;
import com.pm.paymentservice.Exception.AccountNotActiveException;
import com.pm.paymentservice.Exception.InsufficientFundsException;
import com.pm.paymentservice.Exception.PaymentNotFoundException;
import com.pm.paymentservice.Service.PaymentService;
import com.pm.proto.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GrpcService
@AllArgsConstructor
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PaymentGrpcService.class);

    private final PaymentService paymentService;

    // ── ProcessPayment ────────────────────────────────────────────────────────
    // Maps the incoming proto request to a PaymentRequestDTO, delegates to the
    // service layer, and wraps the result back into the proto response.
    @Override
    public void processPayment(ProcessPaymentRequest request,
                               StreamObserver<ProcessPaymentResponse> responseObserver) {
        try {
            log.info("gRPC processPayment: from={} to={} amount={}",
                    request.getFromAccountId(), request.getToAccountId(), request.getAmount());

            PaymentRequestDTO dto = new PaymentRequestDTO();
            dto.setFromAccountId(request.getFromAccountId());
            dto.setToAccountId(request.getToAccountId());
            dto.setAmount(request.getAmount());
            // Proto type is PAY_TRANSFER — strip the prefix to match Java enum TRANSFER
            dto.setType(PaymentType.valueOf(request.getType().name().replace("PAY_", "")));
            dto.setDescription(request.getDescription());

            PaymentResponseDTO result = paymentService.processPayment(dto);

            ProcessPaymentResponse response = ProcessPaymentResponse.newBuilder()
                    .setPayment(buildProtoPayment(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (AccountNotActiveException | InsufficientFundsException e) {
            log.warn("gRPC processPayment business rule violation: {}", e.getMessage());
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC processPayment unexpected error: {}", e.getMessage());
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ── GetPaymentById ────────────────────────────────────────────────────────
    @Override
    public void getPaymentById(GetPaymentByIdRequest request,
                               StreamObserver<GetPaymentByIdResponse> responseObserver) {
        try {
            log.info("gRPC getPaymentById: id={}", request.getId());

            PaymentResponseDTO result = paymentService.getPayment(request.getId());

            GetPaymentByIdResponse response = GetPaymentByIdResponse.newBuilder()
                    .setPayment(buildProtoPayment(result))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (PaymentNotFoundException e) {
            log.warn("gRPC getPaymentById not found: {}", e.getMessage());
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC getPaymentById unexpected error: {}", e.getMessage());
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ── GetPaymentsByAccount ──────────────────────────────────────────────────
    @Override
    public void getPaymentsByAccount(GetPaymentsByAccountRequest request,
                                     StreamObserver<GetPaymentsByAccountResponse> responseObserver) {
        try {
            log.info("gRPC getPaymentsByAccount: accountId={}", request.getAccountId());

            List<PaymentResponseDTO> results = paymentService.getPaymentsByAccount(request.getAccountId());

            // Map every DTO to a proto Payment, collect into the repeated field
            GetPaymentsByAccountResponse response = GetPaymentsByAccountResponse.newBuilder()
                    .addAllPayments(results.stream().map(this::buildProtoPayment).toList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC getPaymentsByAccount unexpected error: {}", e.getMessage());
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    // Converts a PaymentResponseDTO into the proto Payment message.
    // Status/type are stored as plain enum names (e.g. "COMPLETED", "TRANSFER")
    // but proto enum values are prefixed with PAY_ (e.g. PAY_COMPLETED).
    private com.pm.proto.Payment buildProtoPayment(PaymentResponseDTO dto) {
        return com.pm.proto.Payment.newBuilder()
                .setId(dto.getPaymentId())
                .setFromAccountId(dto.getFromAccountId())
                .setToAccountId(dto.getToAccountId())
                .setAmount(dto.getAmount())
                .setStatus(com.pm.proto.PaymentStatus.valueOf("PAY_" + dto.getStatus()))
                .setType(com.pm.proto.PaymentType.valueOf("PAY_" + dto.getType()))
                .setDescription(dto.getDescription() != null ? dto.getDescription() : "")
                .setCreatedAt(dto.getCreatedAt())
                .setUpdatedAt(dto.getUpdatedAt())
                .build();
    }
}
