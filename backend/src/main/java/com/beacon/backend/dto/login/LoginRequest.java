package com.beacon.backend.dto.login;

public record LoginRequest(
	String username,
	String password
) {}
