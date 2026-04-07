package com.beacon.backend.dto.profile;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String emergencyFirstName,
        String emergencyLastName,
        String emergencyEmail
) implements ProfileRequest {}
