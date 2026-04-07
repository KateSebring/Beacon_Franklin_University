package com.beacon.backend.service;

import com.beacon.backend.dto.login.LoginRequest;
import com.beacon.backend.dto.user.ChangePasswordRequest;
import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.security.AccountUserDetails;
import com.beacon.backend.security.AccountUserDetailsService;
import com.beacon.backend.utils.exception.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AccountUserService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final AccountUserRepository accountUserRepository;
    private final PasswordService passwordService;
    private final AuthenticationManager authenticationManager;
    private final AccountUserDetailsService accountUserDetailsService;
    private final JwtService jwtService;

    public AccountUserService(AccountUserRepository accountUserRepository,
                              PasswordService passwordService, AuthenticationManager authenticationManager, AccountUserDetailsService accountUserDetailsService, JwtService jwtService) {
        this.accountUserRepository = accountUserRepository;
        this.passwordService = passwordService;
        this.authenticationManager = authenticationManager;
        this.accountUserDetailsService = accountUserDetailsService;
        this.jwtService = jwtService;
    }
    
    public String loginUser(LoginRequest request) throws BadCredentialsException {
    	String username = normalizeUsername(request.username());
        String password = request.password();
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        	
        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AccountUserDetails accountUserDetails)) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        String role = accountUserDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password."))
                .getAuthority();

        return jwtService.generateToken(accountUserDetails.getUserId(), role);
    }

    public AccountUser registerUser(CreateUserRequest request) {
        AccountUser newUser = new AccountUser();
        String email = normalizeEmail(request.email());
        String username = normalizeUsername(request.username());
        String password = request.password();

        validateUsernameAndEmail(username, email);

        if (password == null || password.isBlank()) {
            throw new InvalidPasswordException();
        }

        if (accountUserRepository.existsByEmail(email))
            throw new DuplicateEmailException(email);
        if (accountUserRepository.existsByUsername(username))
            throw new DuplicateUsernameException();

        String passwordHash = passwordService.hash(request.password());

        newUser.setUsername(username);
        newUser.setPasswordHash(passwordHash);
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setDOB(request.dob());
        newUser.setEmail(email);
        newUser.setRole("ROLE_USER");

        return accountUserRepository.save(newUser);
    }

    public AccountUser getAuthenticatedUser(String currentUsername) {
        String username = normalizeUsername(currentUsername);
        return resolveAuthenticatedUser(username);
    }

    public AccountUser modifyUser(UpdateUserRequest request, String currentUsername) {
        AccountUser currentUser = resolveAuthenticatedUser(currentUsername);

        String email = normalizeEmail(request.email());
        String username = normalizeUsername(request.username());

        validateUsernameAndEmail(username, email);

        if (accountUserRepository.existsByEmailAndUserIdNot(email, currentUser.getId()))
            throw new DuplicateEmailException(email);
        if (accountUserRepository.existsByUsernameAndUserIdNot(username, currentUser.getId()))
            throw new DuplicateUsernameException();

        currentUser.setUsername(username);
        currentUser.setFirstName(request.firstName());
        currentUser.setLastName(request.lastName());
        currentUser.setDOB(request.dob());
        currentUser.setEmail(email);

        return accountUserRepository.save(currentUser);
    }

    public void changePassword(ChangePasswordRequest request, String currentUsername) {
        AccountUser currentUser = resolveAuthenticatedUser(currentUsername);

        String newPassword = request.newPassword();
        String currentPassword = request.currentPassword();

        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidPasswordException();
        }

        if(passwordService.matches(currentPassword, currentUser.getPasswordHash())) {
            String passwordHash = passwordService.hash(newPassword);
            currentUser.setPasswordHash(passwordHash);
            accountUserRepository.save(currentUser);
        } else {
            throw new BadCredentialsException("Current password is incorrect.");
        }
    }

    public List<AccountUser> getUsers() {
        return accountUserRepository.findAll();
    }

    public AccountUser getUserById(int id) {
        return accountUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void removeAuthenticatedUser(String currentUsername) {
        AccountUser currentUser = resolveAuthenticatedUser(currentUsername);
        int id = currentUser.getId();

        accountUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        accountUserRepository.deleteById(id);
    }

    private AccountUser resolveAuthenticatedUser(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException(username);
        }

        String normalizedUsername = username.trim();

        return accountUserRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new InvalidUsernameException(normalizedUsername));
    }

    private void validateUsernameAndEmail(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException(username);
        }
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
    }

    private String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
    
    private String normalizeUsername(String username) {
    	return (username == null) ? null : username.trim();
    }
}
