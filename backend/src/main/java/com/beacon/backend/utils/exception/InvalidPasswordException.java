package com.beacon.backend.utils.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Enter a valid password");
    }
}
