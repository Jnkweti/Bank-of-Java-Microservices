package com.pm.accountservice.Exception;

public class AccountNumberAlreadyExistException extends RuntimeException {
    public AccountNumberAlreadyExistException(String message) {
        super(message);
    }
}
