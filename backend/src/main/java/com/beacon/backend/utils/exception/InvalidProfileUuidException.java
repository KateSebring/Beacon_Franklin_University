package com.beacon.backend.utils.exception;

public class InvalidProfileUuidException extends RuntimeException {
    public InvalidProfileUuidException() {
        super("Invalid profile uuid. Please try again.");
    }
}
