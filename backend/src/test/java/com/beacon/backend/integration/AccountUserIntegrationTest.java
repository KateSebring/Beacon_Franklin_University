package com.beacon.backend.integration;

import com.beacon.backend.dto.login.LoginRequest;
import com.beacon.backend.dto.user.ChangePasswordRequest;
import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.service.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static com.beacon.backend.testutils.TestUsers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AccountUserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    public void setup(){
        accountUserRepository.deleteAll();
    }

    // Builds a Bearer auth header for JWT protected requests
    private String bearer(String token) {
        return "Bearer " + token;
    }

    // Logs in through /api/auth/login and returns the JWT from the response body.
    private String loginAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("username", username, "password", password)
        );

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText(); // { "token": "..." }
    }

    private ResultActions performRegister(CreateUserRequest request) throws Exception {
        return this.mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performLogin(LoginRequest request) throws Exception {
        return this.mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performPutWithToken(String url, UpdateUserRequest request, String token)
            throws Exception {
        return this.mockMvc.perform(put(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performChangePasswordWithToken(String url, ChangePasswordRequest request, String token)
            throws Exception {
        return this.mockMvc.perform(put(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performDeleteWithToken(String url, String token) throws Exception {
        return this.mockMvc.perform(delete(url)
                .header("Authorization", bearer(token)));
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

    /**
     * Registers user and then logs in with that new account.
     */
    @Test
    public void testRegisterAndLoginUser() throws Exception {
        CreateUserRequest validCreateUserRequest = testUserRequest();

        performRegister(validCreateUserRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(validCreateUserRequest.username()))
                .andExpect(jsonPath("$.email").value(validCreateUserRequest.email().toLowerCase().trim()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AccountUser createdUser = accountUserRepository.findByUsername(validCreateUserRequest.username()).orElseThrow();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(validCreateUserRequest.username());
        assertThat(createdUser.getEmail()).isEqualTo(validCreateUserRequest.email().toLowerCase().trim());
        assertThat(createdUser.getPasswordHash()).isNotEqualTo(validCreateUserRequest.password());
        assertThat(createdUser.getRole()).isEqualTo("ROLE_USER");
        assertThat(passwordService.matches(
                validCreateUserRequest.password(), createdUser.getPasswordHash())).isTrue();

        performLogin(new LoginRequest(validCreateUserRequest.username(), validCreateUserRequest.password()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * Rejects registration when the email format is invalid.
     */
    @Test
    public void testRegisterRejectsInvalidEmailFormat() throws Exception {
        CreateUserRequest invalidEmailRequest = new CreateUserRequest(
                "bademailuser",
                "pw1",
                "Bad",
                "Email",
                java.time.LocalDate.of(2025, 2, 18),
                "not-an-email"
        );

        assertProblem(
                performRegister(invalidEmailRequest),
                status().isBadRequest(),
                "Invalid Email",
                400,
                "Invalid email address. Please try again."
        );

        assertThat(accountUserRepository.findByUsername(invalidEmailRequest.username())).isEmpty();
    }

    /**
     * Updates the user's own account with a valid token.
     */
    @Test
    public void testUpdateOwnAccount() throws Exception {
        CreateUserRequest validCreateUserRequest = testUserRequest();
        performRegister(validCreateUserRequest);

        AccountUser registeredUser = accountUserRepository.findByUsername(validCreateUserRequest.username()).orElseThrow();
        int createdUserId = registeredUser.getId();
        String originalPasswordHash = registeredUser.getPasswordHash();

        String token = loginAndGetToken(validCreateUserRequest.username(), validCreateUserRequest.password());

        UpdateUserRequest updateUserRequest = updateUserRequest();

        performPutWithToken("/api/users/me", updateUserRequest, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updateUserRequest.username()))
                .andExpect(jsonPath("$.email").value(updateUserRequest.email().toLowerCase().trim()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AccountUser updatedUser = accountUserRepository.findById(createdUserId).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo(updateUserRequest.username());
        assertThat(updatedUser.getEmail()).isEqualTo(updateUserRequest.email().toLowerCase().trim());
        assertThat(updatedUser.getPasswordHash()).isEqualTo(originalPasswordHash);
    }

    /**
     * Updates the user's credentials with and logs in with new credentials.
     */
    @Test
    public void testUpdatedCredentialsCanLogIn() throws Exception {
        CreateUserRequest validCreateUserRequest = testUserRequest();

        performRegister(validCreateUserRequest);

        AccountUser registeredUser = accountUserRepository
                .findByUsername(validCreateUserRequest.username()).orElseThrow();
        int createdUserId = registeredUser.getId();

        String token = loginAndGetToken(validCreateUserRequest.username(), validCreateUserRequest.password());

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(
                validCreateUserRequest.password(), "new-password");

        performChangePasswordWithToken("/api/users/me/password", changePasswordRequest, token)
                .andExpect(status().isNoContent());

        AccountUser updatedUser = accountUserRepository.findById(createdUserId).orElseThrow();
        assertThat(passwordService.matches(changePasswordRequest.newPassword(), updatedUser.getPasswordHash()))
                .isTrue();

        String newToken = loginAndGetToken(
                validCreateUserRequest.username(),
                changePasswordRequest.newPassword()
        );
        assertThat(newToken).isNotEmpty();

        assertProblem(
                performLogin(new LoginRequest(validCreateUserRequest.username(), validCreateUserRequest.password())),
                status().isUnauthorized(),
                "Authentication Failed",
                401,
                "Bad credentials"
        );
    }

    /**
     * A user cannot update their own email with a duplicate email from an existing user.
     */
    @Test
    public void testUpdateWithDuplicateEmail() throws Exception {
        CreateUserRequest validCreateUserRequest = testUserRequest();
        CreateUserRequest duplicateEmailCreateUserRequest = testUserRequest2();

        performRegister(validCreateUserRequest);
        performRegister(duplicateEmailCreateUserRequest);

        AccountUser registeredUser = accountUserRepository.findByUsername(validCreateUserRequest.username())
                .orElseThrow();
        int createdUserId = registeredUser.getId();

        String token = loginAndGetToken(validCreateUserRequest.username(), validCreateUserRequest.password());
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                validCreateUserRequest.username(),
                validCreateUserRequest.firstName(),
                validCreateUserRequest.lastName(),
                validCreateUserRequest.dob(),
                duplicateEmailRequest2().email()
        );

        assertProblem(
                performPutWithToken("/api/users/me", updateUserRequest, token),
                status().isConflict(),
                "Duplicate Email",
                409,
                "User already exists with this email " + duplicateEmailRequest2().email()
        );

        AccountUser updatedUser = accountUserRepository.findById(createdUserId).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo(validCreateUserRequest.email().toLowerCase().trim());
    }

    /**
     * Delete removes the user's account and they can no longer log in.
     */
    @Test
    public void testDeleteOwnAccount() throws Exception {
        CreateUserRequest validCreateUserRequest = testUserRequest();

        performRegister(validCreateUserRequest);

        AccountUser registeredUser = accountUserRepository
                .findByUsername(validCreateUserRequest.username()).orElseThrow();
        int createdUserId = registeredUser.getId();

        String token = loginAndGetToken(validCreateUserRequest.username(), validCreateUserRequest.password());

        performDeleteWithToken("/api/users/me", token)
                .andExpect(status().isNoContent());

        assertThat(accountUserRepository.findById(createdUserId)).isEmpty();

        assertProblem(
                performLogin(new LoginRequest(validCreateUserRequest.username(), validCreateUserRequest.password())),
                status().isUnauthorized(),
                "Authentication Failed",
                 401,
                "Bad credentials"
        );
    }
}
