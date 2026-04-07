package com.beacon.backend.controller;

import com.beacon.backend.dto.user.UserResponse;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.service.AccountUserService;
import com.beacon.backend.utils.adapter.AccountUserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AccountUserService accountUserService;
    private final AccountUserMapper accountUserMapper;

    public AdminController(AccountUserService accountUserService,
                           AccountUserMapper accountUserMapper) {
        this.accountUserService = accountUserService;
        this.accountUserMapper = accountUserMapper;
    }

    /*
     * Fetches all users for client who has admin role
     */
    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<AccountUser> users = accountUserService.getUsers();
        return ResponseEntity.ok(accountUserMapper.allToUserResponse(users));
    }

    /*
     * Fetches user by ID for client who has admin role
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable int id) {
        AccountUser user = accountUserService.getUserById(id);
        return ResponseEntity.ok(accountUserMapper.toUserResponse(user));
    }
}
