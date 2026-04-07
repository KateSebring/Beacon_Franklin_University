package com.beacon.backend.dto.profile;

public record CreateProfileRequest(
        String firstName,
        String lastName,
        String emergencyFirstName,
        String emergencyLastName,
        String emergencyEmail
) implements ProfileRequest {}
