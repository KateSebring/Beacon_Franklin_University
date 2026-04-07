package com.beacon.backend.controller;

import com.beacon.backend.dto.login.LoginRequest;
import com.beacon.backend.dto.login.LoginResponse;
import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.dto.user.UserResponse;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.service.AccountUserService;
import com.beacon.backend.utils.adapter.AccountUserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Auth endpoints for user account registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private final AccountUserService accountUserService;
    private final AccountUserMapper accountUserMapper;

    public AuthController(AccountUserService accountUserService,
                          AccountUserMapper accountUserMapper) {
        this.accountUserService = accountUserService;
        this.accountUserMapper = accountUserMapper;
    }

    /**
     * Authenticates user credentials and returns JWT on success.
     * Returns 401 when username/password is invalid.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @RequestBody LoginRequest request) throws BadCredentialsException {
        String token = accountUserService.loginUser(request);
        return ResponseEntity
                .ok(new LoginResponse(token));
    }

    /**
     * Registers a new user account.
     * Returns 201 with the created user (without hashed password).
     * Validation/duplicate errors handled by global exception handlers.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> addUser(@RequestBody CreateUserRequest request) {
        AccountUser created = accountUserService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                accountUserMapper.toUserResponse(created));
    }
}
