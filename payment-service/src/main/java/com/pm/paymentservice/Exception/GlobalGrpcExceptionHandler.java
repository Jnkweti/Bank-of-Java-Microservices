package com.pm.paymentservice.Exception;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(PaymentNotFoundException.class)
    public Status handlePaymentNotFound(PaymentNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage());
    }

    @GrpcExceptionHandler(InsufficientFundsException.class)
    public Status handleInsufficientFunds(InsufficientFundsException e) {
        return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
    }

    @GrpcExceptionHandler(AccountNotActiveException.class)
    public Status handleAccountNotActive(AccountNotActiveException e) {
        return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGenericException(Exception e) {
        return Status.INTERNAL.withDescription("Unexpected error: " + e.getMessage());
    }
}
