package com.pm.customerservice.Exceptions;

import io.grpc.*;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import com.pm.customerservice.Exceptions.EmailAlreadyExistException;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(EmailAlreadyExistException.class)
    public Status handleEmailExists(EmailAlreadyExistException e) {
        return Status.ALREADY_EXISTS.withDescription(e.getMessage());
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGenericException(Exception e) {
        return Status.INTERNAL.withDescription("Unexpected error: " + e.getMessage());
    }

    @GrpcExceptionHandler(CustomerNotFoundException.class)
    public Status handleCustomerNotFound(CustomerNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage());
    }
}

