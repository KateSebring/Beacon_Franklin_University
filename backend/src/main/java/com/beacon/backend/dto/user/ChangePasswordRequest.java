package com.beacon.backend.dto.user;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}
