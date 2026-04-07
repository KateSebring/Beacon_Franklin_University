package com.beacon.backend.dto.profile;

public record ProfileResponse(
        int id,
        String uuid,
        String firstName,
        String lastName,
        String emergencyFirstName,
        String emergencyLastName,
        String emergencyEmail
) {}
