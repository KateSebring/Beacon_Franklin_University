package com.beacon.backend.service;

import com.beacon.backend.dto.user.ChangePasswordRequest;
import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.utils.exception.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static com.beacon.backend.testutils.TestUsers.updateUserRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AccountUserServiceTest {
    @Autowired
    private AccountUserService accountUserService;

    @MockitoBean
    private PasswordService passwordService;

    @MockitoBean
    private AccountUserRepository accountUserRepository;

    private AccountUser userWithId(int id, String username, String email) {
        AccountUser user = new AccountUser();
        ReflectionTestUtils.setField(user, "userId", id);
        user.setUsername(username);
        user.setPasswordHash("hashed-password");
        user.setFirstName("Test");
        user.setLastName("Derek");
        user.setDOB(LocalDate.of(1994,3, 14));
        user.setEmail(email);
        user.setRole("ROLE_USER");
        return user;
    }

    /**
     * Returns the user when the username and email are unique and the request is valid.
     */
    @Test
    public void testValidRegisterUser() throws InvalidEmailException, InvalidUsernameException {
        when(accountUserRepository.existsByEmail("derek@derek.com")).thenReturn(false);
        when(accountUserRepository.existsByUsername("dafinnell")).thenReturn(false);
        when(passwordService.hash("password")).thenReturn("password hashed (password)");
        CreateUserRequest request = new CreateUserRequest(
                "dafinnell",
                "password",
                "derek",
                "finnell",
                LocalDate.of(2025,2,18),
                "Derek@derek.com"
        );

        accountUserService.registerUser(request);

        // Capture the values of the user and user is saved to the mock
        ArgumentCaptor<AccountUser> captor = ArgumentCaptor.forClass(AccountUser.class);
        verify(accountUserRepository).save(captor.capture());
        AccountUser savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("derek@derek.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("password hashed (password)");
        assertThat(savedUser.getUsername()).isEqualTo("dafinnell");
    }

    /**
     * Rejects registration when the email format is invalid.
     */
    @Test
    public void testRegisterUserRejectsInvalidEmailFormat() {
        CreateUserRequest request = new CreateUserRequest(
                "dafinnell",
                "password",
                "derek",
                "finnell",
                LocalDate.of(2025, 2, 18),
                "not-an-email"
        );

        assertThatThrownBy(() -> accountUserService.registerUser(request))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage("Invalid email address. Please try again.");

        verify(accountUserRepository, never()).existsByEmail(anyString());
        verify(accountUserRepository, never()).existsByUsername(anyString());
        verify(accountUserRepository, never()).save(any(AccountUser.class));
        verify(passwordService, never()).hash(anyString());
    }

    /**
     * Returns the updated user when the authenticated user matches.
     */
    @Test
    public void testAuthenticatedUserAllowsUpdate() {
        UpdateUserRequest request = updateUserRequest();
        AccountUser currentUser = userWithId(1, "testuser", "test@test.com");
        String originalPasswordHash = currentUser.getPasswordHash();

        when(accountUserRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(accountUserRepository.existsByEmailAndUserIdNot(request.email(), 1)).thenReturn(false);
        when(accountUserRepository.existsByUsernameAndUserIdNot(request.username(), 1))
                .thenReturn(false);
        when(accountUserRepository.save(any(AccountUser.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        AccountUser updatedUser = accountUserService.modifyUser(request, "testuser");

        assertThat(updatedUser.getId()).isEqualTo(1);
        assertThat(updatedUser.getUsername()).isEqualTo(request.username());
        assertThat(updatedUser.getEmail()).isEqualTo(request.email().toLowerCase().trim());
        assertThat(updatedUser.getPasswordHash()).isEqualTo(originalPasswordHash);
        verify(passwordService, never()).hash(anyString());

        ArgumentCaptor<AccountUser> captor = ArgumentCaptor.forClass(AccountUser.class);
        verify(accountUserRepository).save(captor.capture());
        AccountUser savedUser = captor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(request.username());
        assertThat(savedUser.getPasswordHash()).isEqualTo(originalPasswordHash);
    }

    /**
     * Password change is allowed when the current password matches the user's password.
     */
    @Test
    public void testChangePasswordSucceedsWithCorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("old-password",
                "new-password");
        AccountUser currentUser = userWithId(1, "testuser", "test@test.com");

        when(accountUserRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(passwordService.matches("old-password", "hashed-password"))
                .thenReturn(true);
        when(passwordService.hash("new-password")).thenReturn("new-hashed-password");
        when(accountUserRepository.save(any(AccountUser.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        accountUserService.changePassword(request, "testuser");

        verify(passwordService).matches("old-password", "hashed-password");
        verify(passwordService).hash("new-password");

        ArgumentCaptor<AccountUser> captor = ArgumentCaptor.forClass(AccountUser.class);
        verify(accountUserRepository).save(captor.capture());
        AccountUser savedUser = captor.getValue();

        assertThat(savedUser.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(currentUser.getPasswordHash()).isEqualTo("new-hashed-password");
    }

    /**
     * Throws BadCredentialsException when the current password does not match the user's password.
     */
    @Test
    public void testChangePasswordRejectsWrongCurrentPassword() {
        ChangePasswordRequest badRequest = new ChangePasswordRequest(
                "bad-password", "new-password");
        AccountUser user = userWithId(1, "testuser", "test@test.com");
        when(accountUserRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordService.matches("bad-password", "hashed-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> accountUserService.changePassword(badRequest, "testuser"))
                .isInstanceOf(BadCredentialsException.class);

        verify(passwordService).matches("bad-password", "hashed-password");
        verify(accountUserRepository, never()).save(any(AccountUser.class));
    }

    /**
     * Throws InvalidPasswordException when the new password is invalid.
     */
    @Test
    public void testChangePasswordRejectsBlankNewPassword() {
        ChangePasswordRequest badRequest = new ChangePasswordRequest("old-password", "");
        AccountUser currentUser = userWithId(1, "testuser", "test@test.com");

        when(accountUserRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(passwordService.matches("old-password", "hashed-password"))
                .thenReturn(true);
        when(passwordService.hash("")).thenThrow(new InvalidPasswordException());

        assertThatThrownBy(() -> accountUserService.changePassword(badRequest, "testuser"))
                .isInstanceOf(InvalidPasswordException.class);

        verify(passwordService, never()).hash("new-password");
        verify(accountUserRepository, never()).save(any(AccountUser.class));
    }

    /**
     * Delete allowed when authenticated user matches the target id.
     */
    @Test
    public void testDeleteAllowedWhenAuthenticatedUserMatchesId() {
        AccountUser currentUser = userWithId(1, "testuser", "test@test.com");
        when(accountUserRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(accountUserRepository.findById(1)).thenReturn(Optional.of(currentUser));

        accountUserService.removeAuthenticatedUser("testuser");

        verify(accountUserRepository).findByUsername("testuser");
        verify(accountUserRepository).findById(1);
        verify(accountUserRepository).deleteById(1);
    }

    /**
     * Throws UserNotFoundException when the owner matches but the user does not exist.
     */
    @Test
    public void testDeleteUserThatDoesNotExist() {
        AccountUser currentUser = userWithId(1, "testuser", "test@test.com");
        when(accountUserRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(accountUserRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountUserService.removeAuthenticatedUser("testuser"))
                .isInstanceOf(UserNotFoundException.class);

        verify(accountUserRepository).findById(1);
        verify(accountUserRepository, never()).deleteById(1);
    }
}
