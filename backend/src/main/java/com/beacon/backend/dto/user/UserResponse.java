package com.beacon.backend.dto.user;

import java.time.LocalDate;

public record UserResponse(
        int id,
        String username,
        String firstName,
        String lastName,
        LocalDate dob,
        String email
) {}
