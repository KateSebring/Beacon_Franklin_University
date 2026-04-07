package com.beacon.backend.utils.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException() {
        super("Profile not found for this id or uuid.");
    }
}
