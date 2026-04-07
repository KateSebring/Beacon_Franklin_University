package com.beacon.backend.utils.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String email) {
        super("Invalid email address. Please try again.");
    }
}
