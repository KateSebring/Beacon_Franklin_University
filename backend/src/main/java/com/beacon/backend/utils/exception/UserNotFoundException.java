package com.beacon.backend.utils.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int id) {
        super("User not found for this id");
    }
}
