package com.beacon.backend.dto.user;

import java.time.LocalDate;

public record CreateUserRequest (
        String username,
        String password,
        String firstName,
        String lastName,
        LocalDate dob,
        String email
) {}