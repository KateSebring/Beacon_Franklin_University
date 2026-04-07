package com.beacon.backend.utils.exception;

public class InvalidUserIdException extends RuntimeException {
    public InvalidUserIdException(int ownerUserId) {
        super("The User Id requested is not properly formatted. id: " + ownerUserId);
    }
}
