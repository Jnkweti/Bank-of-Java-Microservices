package com.pm.customerservice.Exceptions.Validation;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public abstract class GrpcValidation {
    @Autowired
    protected Validator validator;

    public <T> void validate(T dto, StreamObserver<?> responseObserver) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Validation failed: " + message)
                            .asRuntimeException()
            );
            throw new RuntimeException("Validation failed: " + message);
        }
    }

    /**
     * Helper to handle unexpected errors in a consistent way.
     */
    public void handleException(Exception e, StreamObserver<?> responseObserver) {
        responseObserver.onError(
                Status.INTERNAL
                        .withDescription("Unexpected error: " + e.getMessage())
                        .asRuntimeException()
        );
    }
}

