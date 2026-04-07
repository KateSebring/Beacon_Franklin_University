package com.beacon.backend.utils.exception;

import com.beacon.backend.dto.profile.ProfileRequest;

public class InvalidProfileAttributeException extends RuntimeException {
    public InvalidProfileAttributeException(ProfileRequest request) {
        super("A profile attribute is invalid");
    }
}
