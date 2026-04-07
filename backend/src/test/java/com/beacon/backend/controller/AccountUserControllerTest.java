package com.beacon.backend.controller;

import com.beacon.backend.dto.user.ChangePasswordRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.security.AccountUserDetailsService;
import com.beacon.backend.service.AccountUserService;
import com.beacon.backend.service.JwtService;
import com.beacon.backend.utils.adapter.AccountUserMapper;
import com.beacon.backend.utils.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static com.beacon.backend.testutils.TestUsers.updateUserRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AccountUserMapper.class)
public class AccountUserControllerTest {
    private static final int EXISTING_USER_ID = 1;
    private static final int MISSING_USER_ID = 9999;
    private static final String AUTHENTICATED_USERNAME = "testuser";

    private UpdateUserRequest validUpdateRequest;
    private AccountUser updatedUserResponse;
    private ChangePasswordRequest validPasswordChangeRequest;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountUserService accountUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AccountUserDetailsService accountUserDetailsService;

    @BeforeEach
    public void setup() {
        validUpdateRequest = updateUserRequest();
        updatedUserResponse = new AccountUser(
                validUpdateRequest.username(),
                "hashed-password",
                validUpdateRequest.firstName(),
                validUpdateRequest.lastName(),
                validUpdateRequest.dob(),
                validUpdateRequest.email()
        );

        validPasswordChangeRequest = new ChangePasswordRequest(
                "old-password",
                "new-password"
        );
    }

    private UsernamePasswordAuthenticationToken authenticatedUser(String username) {
        return new UsernamePasswordAuthenticationToken(username, null);
    }

    private ResultActions performEdit(UpdateUserRequest request, String username) throws Exception {
        return this.mockMvc.perform(put("/api/users/me")
                        .principal(authenticatedUser(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performPasswordChange(ChangePasswordRequest request, String username)
            throws Exception {
        return this.mockMvc.perform(put("/api/users/me/password")
                        .principal(authenticatedUser(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performDelete(String username) throws Exception {
        return this.mockMvc.perform(delete("/api/users/me").principal(authenticatedUser(username)));
    }

    private void assertProblem(ResultActions result,
                               ResultMatcher statusMatcher,
                               String title,
                               int statusCode,
                               String detail) throws Exception {
        result.andExpect(statusMatcher)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.status").value(statusCode))
                .andExpect(jsonPath("$.detail").value(detail));
    }

    private void assertUserResponse(ResultActions result, AccountUser expectedUser) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(expectedUser.getUsername()))
                .andExpect(jsonPath("$.email").value(expectedUser.getEmail()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    /**
     * Returns 200 and updated user for PUT /api/users/{id}.
     */
    @Test
    public void testEditUser() throws Exception {
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME))).thenReturn(updatedUserResponse);

        assertUserResponse(
                performEdit(validUpdateRequest, AUTHENTICATED_USERNAME), updatedUserResponse);

        verify(accountUserService).modifyUser(eq(validUpdateRequest), eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 404 problem details when PUT /api/users/{id} uses an unknown id.
     */
    @Test
    public void testEditUserNotFound() throws Exception {
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME))).thenThrow(new UserNotFoundException(MISSING_USER_ID));

        assertProblem(
                performEdit(validUpdateRequest, AUTHENTICATED_USERNAME),
                status().isNotFound(),
                "User Not Found",
                404,
                "User not found for this id"
        );

        verify(accountUserService).modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 409 problem details when PUT /api/users/{id} uses a duplicate email.
     */
    @Test
    public void testEditUserDuplicateEmail() throws Exception {
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME))).thenThrow(new DuplicateEmailException(validUpdateRequest.email()));

        assertProblem(
                performEdit(validUpdateRequest, AUTHENTICATED_USERNAME),
                status().isConflict(),
                "Duplicate Email",
                409,
                "User already exists with this email " + validUpdateRequest.email()
        );

        verify(accountUserService).modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 409 problem details when PUT /api/users/{id} uses a duplicate username.
     */
    @Test
    public void testEditUserDuplicateUsername() throws Exception {
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME))).thenThrow(new DuplicateUsernameException());

        assertProblem(
                performEdit(validUpdateRequest, AUTHENTICATED_USERNAME),
                status().isConflict(),
                "Duplicate Username",
                 409,
                "This username already exists"
        );

        verify(accountUserService).modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 400 problem details when PUT /api/users/{id} uses an invalid email.
     */
    @Test
    public void testEditUserInvalidEmail() throws Exception {
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME))).thenThrow(new InvalidEmailException(validUpdateRequest.email()));

        assertProblem(
                performEdit(validUpdateRequest, AUTHENTICATED_USERNAME),
                status().isBadRequest(),
                "Invalid Email",
                 400,
                "Invalid email address. Please try again."
        );

        verify(accountUserService).modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 400 problem details when PUT /api/users/{id} uses an invalid username.
     */
    @Test
    public void testEditUserInvalidUsername() throws Exception {
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME))).thenThrow(new InvalidUsernameException(validUpdateRequest.username()));

        assertProblem(
                performEdit(validUpdateRequest, AUTHENTICATED_USERNAME),
                status().isBadRequest(),
                "Invalid Username",
                 400,
                "Invalid username. Please try again."
        );

        verify(accountUserService).modifyUser(any(UpdateUserRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 403 problem details when PUT /api/users/{id} is not the authenticated user with that id.
     */
    @Test
    public void testEditUserForbiddenWhenServiceRejectsOwnership() throws Exception {
        String badUsername = "Derek";
        when(accountUserService.modifyUser(any(UpdateUserRequest.class),
                eq(badUsername))).thenThrow(new ForbiddenAccountAccessException());

        assertProblem(
                performEdit(validUpdateRequest, badUsername),
                status().isForbidden(),
                "Forbidden Account Access",
                403,
                "You may only modify or delete your own account."
        );

        verify(accountUserService).modifyUser(any(UpdateUserRequest.class), eq(badUsername));
    }

    /**
     * Returns 400 when PUT /api/users/{id} receives malformed JSON.
     */
    @Test
    public void testEditUserBadJson() throws Exception {
        String badJson = "{\"username\":\"derek\", \"email\": }";

        mockMvc.perform(put("/api/users/me")
                        .principal(authenticatedUser(AUTHENTICATED_USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountUserService);
    }

    /**
     * Returns 204 for PUT /api/users/{id}/password.
     */
    @Test
    public void testChangePassword() throws Exception {
        doNothing().when(accountUserService).changePassword(
                any(ChangePasswordRequest.class), eq(AUTHENTICATED_USERNAME));

        performPasswordChange(validPasswordChangeRequest, AUTHENTICATED_USERNAME)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(accountUserService).changePassword(any(ChangePasswordRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 400 problem details when PUT /api/users/{id}/password uses an invalid password.
     */
    @Test
    public void testChangePasswordInvalidPassword() throws Exception {
        doThrow(new InvalidPasswordException()).when(accountUserService).changePassword(
                any(ChangePasswordRequest.class), eq(AUTHENTICATED_USERNAME));

        ChangePasswordRequest invalidPasswordRequest = new ChangePasswordRequest(
                "oldPassword", null);

        assertProblem(
                performPasswordChange(invalidPasswordRequest, AUTHENTICATED_USERNAME),
                status().isBadRequest(),
                "Invalid Password",
                400,
                "Enter a valid password"
        );

        verify(accountUserService).changePassword(any(ChangePasswordRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 401 problem details when PUT /api/users/{id}/password uses a wrong old password.
     */
    @Test
    public void testChangePasswordBadCredentials() throws Exception {
        doThrow(new BadCredentialsException("Current password is incorrect.")).when(accountUserService).changePassword(
                any(ChangePasswordRequest.class), eq(AUTHENTICATED_USERNAME)
        );

        ChangePasswordRequest badPasswordRequest = new ChangePasswordRequest(
                "badPassword", "newPassword");

        assertProblem(
                performPasswordChange(badPasswordRequest, AUTHENTICATED_USERNAME),
                status().isUnauthorized(),
                "Authentication Failed",
                 401,
                "Current password is incorrect."
        );

        verify(accountUserService).changePassword(any(ChangePasswordRequest.class),
                eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 403 problem details when PUT /api/users/{id}/password is not the authenticated user with that id.
     */
    @Test
    public void testChangePasswordForbiddenWhenServiceRejectsOwnership() throws Exception {
        String badUsername = "Derek";
        doThrow(new ForbiddenAccountAccessException()).when(accountUserService).changePassword(
                any(ChangePasswordRequest.class), eq(badUsername));

        assertProblem(
                performPasswordChange(validPasswordChangeRequest, badUsername),
                status().isForbidden(),
                "Forbidden Account Access",
                 403,
                "You may only modify or delete your own account."
        );

        verify(accountUserService).changePassword(any(ChangePasswordRequest.class),
                eq(badUsername));
    }

    /**
     * Returns 204 for DELETE /api/users/{id} when user exists.
     */
    @Test
    public void testDeleteUser() throws Exception {
        doNothing().when(accountUserService).removeAuthenticatedUser(eq(AUTHENTICATED_USERNAME));

        performDelete(AUTHENTICATED_USERNAME)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(accountUserService).removeAuthenticatedUser(eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 404 problem details when DELETE /api/users/{id} uses an unknown id.
     */
    @Test
    public void testDeleteUserNotFound() throws Exception {
        doThrow(new UserNotFoundException(MISSING_USER_ID)).when(accountUserService).removeAuthenticatedUser(
                eq(AUTHENTICATED_USERNAME));

        assertProblem(
                performDelete(AUTHENTICATED_USERNAME),
                status().isNotFound(),
                "User Not Found",
                404,
                "User not found for this id"
        );

        verify(accountUserService).removeAuthenticatedUser(eq(AUTHENTICATED_USERNAME));
    }

    /**
     * Returns 403 problem details when DELETE /api/users/{id} uses an unknown id.
     */
    @Test
    public void testDeleteUserForbiddenWhenServiceRejectsOwnership() throws Exception {
        String badUsername = "Derek";
        doThrow(new ForbiddenAccountAccessException()).when(accountUserService)
                .removeAuthenticatedUser(eq(badUsername));

        assertProblem(
                performDelete(badUsername),
                status().isForbidden(),
                "Forbidden Account Access",
                403,
                "You may only modify or delete your own account."
        );

        verify(accountUserService).removeAuthenticatedUser(eq(badUsername));
    }
}
