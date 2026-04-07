package com.beacon.backend.utils.exception;

public class ForbiddenAccountAccessException extends RuntimeException {
    public ForbiddenAccountAccessException() {
        super("You may only modify or delete your own account.");
    }
}
