package com.beacon.backend.controller;

import com.beacon.backend.dto.user.ChangePasswordRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.dto.user.UserResponse;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.service.AccountUserService;
import com.beacon.backend.utils.adapter.AccountUserMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class AccountUserController {

	private final AccountUserService accountUserService;
    private final AccountUserMapper mapper;
	
	public AccountUserController(AccountUserService userService,
                                 AccountUserMapper mapper) {
		this.accountUserService = userService;
        this.mapper = mapper;
	}

    /**
     * Retrieves the current user's information.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser(Authentication authentication) {
        String currentUsername = authentication.getName();
        AccountUser currentUser = accountUserService.getAuthenticatedUser(currentUsername);
        return ResponseEntity.ok(mapper.toUserResponse(currentUser));
    }

    /**
     * Takes in a user's information, then modifies their information
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> editUser(@RequestBody UpdateUserRequest request, Authentication authentication) {
        String currentUsername = authentication.getName();
        AccountUser modified = accountUserService.modifyUser(request, currentUsername);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toUserResponse(modified));
    }

    /**
     * Takes in an authenticated user, their old password, and a new password, then updates their password.
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request, Authentication authentication) {
        String currentUsername = authentication.getName();
        accountUserService.changePassword(request, currentUsername);
        return ResponseEntity.noContent().build();
    }

    /**
     * Takes in an authenticated user and removes them from the database
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        String currentUsername = authentication.getName();
        accountUserService.removeAuthenticatedUser(currentUsername);
        return ResponseEntity.noContent().build();
    }
}