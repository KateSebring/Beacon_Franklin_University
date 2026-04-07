package com.beacon.backend.dto.user;

import java.time.LocalDate;

public record UpdateUserRequest(
        String username,
        String firstName,
        String lastName,
        LocalDate dob,
        String email
) {}