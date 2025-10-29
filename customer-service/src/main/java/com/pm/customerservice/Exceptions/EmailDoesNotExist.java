package com.pm.customerservice.Exceptions;

public class EmailDoesNotExist extends RuntimeException {
    public EmailDoesNotExist(String message) {
        super(message);
    }
}
