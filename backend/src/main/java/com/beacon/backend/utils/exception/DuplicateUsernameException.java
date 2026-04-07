package com.beacon.backend.utils.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super("This username already exists");
    }
}
