package com.beacon.backend.utils.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("User already exists with this email " + email);
    }
}
