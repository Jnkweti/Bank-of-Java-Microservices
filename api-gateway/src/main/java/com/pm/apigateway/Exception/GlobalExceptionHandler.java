package com.pm.apigateway.Exception;

import io.grpc.StatusRuntimeException;
import io.grpc.Status;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Map<String,String>> handleStatusRuntimeException(StatusRuntimeException ex){
        HttpStatus httpStatus = switch(ex.getStatus().getCode())
        {
            case OK -> HttpStatus.OK;
            case CANCELLED, DATA_LOSS, UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case UNKNOWN, INTERNAL, ABORTED -> HttpStatus.INTERNAL_SERVER_ERROR;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case DEADLINE_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case RESOURCE_EXHAUSTED -> HttpStatus.BANDWIDTH_LIMIT_EXCEEDED;
            case FAILED_PRECONDITION -> HttpStatus.PRECONDITION_FAILED;
            case OUT_OF_RANGE -> HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
            case UNIMPLEMENTED -> HttpStatus.NOT_IMPLEMENTED;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(httpStatus).body(Map.of("Error", ex.getMessage()));
    }
}
