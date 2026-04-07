package com.beacon.backend.security;

import com.beacon.backend.dto.login.LoginRequest;
import com.beacon.backend.dto.user.ChangePasswordRequest;
import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.service.JwtService;
import com.beacon.backend.service.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // Unlike unit tests, do not bypass filters.
@ActiveProfiles("test") // Pulls from application-test.properties
public class SecurityIntegrationTest {

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private AccountUser testUser;
    private AccountUser testAdmin;
    private AccountUser otherUser;
    private int userId;
    private int adminId;
    private int otherUserId;

    @BeforeEach
    public void setupRepositories() {
        // Clear existing data in repositories so results are independent
        accountUserRepository.deleteAll();

        // Setup User & Admin
        testUser = new AccountUser();
        testUser.setUsername("testuser");
        // This must be hashed because login uses Spring Security password matching
        testUser.setPasswordHash(passwordService.hash("pw1"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setDOB(LocalDate.of(2025, 2, 28));
        testUser.setEmail("testuser@gmail.com");
        testUser.setRole("ROLE_USER");
        testUser = accountUserRepository.save(testUser);

        otherUser = new AccountUser();
        otherUser.setUsername("otheruser");
        otherUser.setPasswordHash(passwordService.hash("pw2"));
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setDOB(LocalDate.of(2025, 3, 13));
        otherUser.setEmail("otheruser@gmail.com");
        otherUser.setRole("ROLE_USER");
        otherUser = accountUserRepository.save(otherUser);

        testAdmin = new AccountUser();
        testAdmin.setUsername("testadmin");
        testAdmin.setPasswordHash(passwordService.hash("pw1"));
        testAdmin.setFirstName("Test");
        testAdmin.setLastName("Admin");
        testAdmin.setDOB(LocalDate.of(2025, 2, 28));
        testAdmin.setEmail("testadmin@gmail.com");
        testAdmin.setRole("ROLE_ADMIN");
        testAdmin = accountUserRepository.save(testAdmin);

        // Assign ids here so values aren't hardcoded in tests
        userId = testUser.getId();
        adminId = testAdmin.getId();
        otherUserId = otherUser.getId();
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

    private ResultActions performGet(String url) throws Exception {
        return this.mockMvc.perform(get(url));
    }

    private ResultActions performGet(String url, int id) throws Exception {
        return this.mockMvc.perform(get(url, id));
    }

    private ResultActions performGetWithToken(String url, String token) throws Exception {
        return this.mockMvc.perform(get(url)
                .header("Authorization", bearer(token)));
    }
    private ResultActions performGetWithToken(String url, int id, String token) throws Exception {
        return this.mockMvc.perform(get(url, id)
                .header("Authorization", bearer(token)));
    }

    private ResultActions performPutWithToken(String url, UpdateUserRequest request, String token)
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

    private ResultActions performPut(String url, UpdateUserRequest request) throws Exception {
        return this.mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performChangePassword(String url, ChangePasswordRequest request) throws Exception {
        return this.mockMvc.perform(put(url)
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

    private ResultActions performDelete(String url) throws Exception {
        return this.mockMvc.perform(delete(url));
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

    private String userToken() throws Exception {
        return loginAndGetToken("testuser", "pw1");
    }

    private String adminToken() throws Exception {
        return loginAndGetToken("testadmin", "pw1");
    }

    private UpdateUserRequest validUpdateRequest() {
        return new UpdateUserRequest(
                "dafinnell",
                "derek",
                "finnell",
                LocalDate.of(2025, 2, 18),
                "derek@derek.com"
        );
    }

    /**
     * Returns a non-empty JWT for POST /api/auth/login with valid credentials.
     */
    @Test
    public void testLoginTestUserReturnsToken() throws Exception {
        String retrievedToken = userToken();
        assertThat(retrievedToken).isNotBlank();
    }

    /**
     * Returns 201 for POST /api/auth/register with a valid request.
     */
    @Test
    public void testRegisterPublicEndpoint() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "dafinnell",
                "pw1",
                "derek",
                "finnell",
                LocalDate.of(2025, 2, 18),
                "derek@derek.com"
        );

        performRegister(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("dafinnell"))
                .andExpect(jsonPath("$.email").value("derek@derek.com"));
    }

    /**
     * Returns 409 for POST /api/auth/register when user already exists.
     */
    @Test
    public void testRegisterDuplicateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "testuser",
                "pw1",
                "Test",
                "User",
                LocalDate.of(2025, 2, 28),
                "testuser@gmail.com"
        );

        assertProblem(
                performRegister(request),
                status().isConflict(),
                "Duplicate Email",
                409,
                "User already exists with this email " + request.email()
        );
    }

    /**
     * Returns 200 and token for POST /api/auth/login with valid credentials.
     */
    @Test
    public void testLoginWithoutTokenReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "pw1");

        String response = performLogin(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();
        assertThat(token).contains(".");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_USER");
    }

    /**
     * Returns 401 for POST /api/auth/login with invalid credentials.
     */
    @Test
    public void testLoginWithBadPassword() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "badpassword");

        assertProblem(
                performLogin(request),
                status().isUnauthorized(),
                "Authentication Failed",
                401,
                "Bad credentials"
        );
    }


    /**
     * Returns 403 for GET /api/admin/users without a token.
     */
    @Test
    public void testGetAdminUsersWithoutTokenIsRejected() throws Exception {
        performGet("/api/admin/users")
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for GET /api/admin/users with a user token.
     */
    @Test
    @Disabled("Blocked until AdminController merge and admin roles working")
    public void testGetAdminUsersWithUserTokenIsRejected() throws Exception {
        performGetWithToken("/api/admin/users", userToken())
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for GET /api/admin/users/{id} without a token.
     */
    @Test
    public void testGetAdminUserByIdWithoutTokenIsRejected() throws Exception {
        performGet("/api/admin/users/{id}", userId)
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for GET /api/admin/users/{id} with a user token.
     */
    @Test
    @Disabled("Blocked until AdminController merge and admin roles working")
    public void testGetAdminUserByIdWithUserTokenIsRejected() throws Exception {
        performGetWithToken("/api/admin/users/{id}", userId, userToken())
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 200 for GET /api/admin/users with an admin token.
     */
    @Test
    @Disabled("Blocked until AdminController merge and admin roles working")
    public void testGetAdminUsersWithAdminToken() throws Exception {
        performGetWithToken("/api/admin/users", adminToken())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Returns 200 for GET /api/admin/users/{id} with an admin token.
     */
    @Test
    @Disabled("Blocked until AdminController merge and admin roles working")
    public void testGetAdminUserByIdWithAdminToken() throws Exception {
        performGetWithToken("/api/admin/users/{id}", adminId, adminToken())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(adminId))
                .andExpect(jsonPath("$.username").value(testAdmin.getUsername()));
    }

    /**
     * Returns 403 for GET /api/profile without a token.
     */
    @Test
    public void testGetProfilesWithoutTokenIsRejected() throws Exception {
        performGet("/api/profile")
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for GET /api/profile with an admin token.
     */
    @Test
    public void testGetProfilesWithAdminTokenIsRejected() throws Exception {
        performGetWithToken("/api/profile", adminToken())
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for DELETE /api/profile/{id} without a token.
     */
    @Test
    public void testDeleteProfileWithoutTokenIsRejected() throws Exception {
        performDelete("/api/profile/1")
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for PUT /api/users/me without a token.
     */
    @Test
    public void testUpdateUserWithoutToken() throws Exception {
        UpdateUserRequest request = validUpdateRequest();

        performPut("/api/users/me", request)
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for PUT /api/users/me/password without a token.
     */
    @Test
    public void testChangePasswordWithoutToken() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("old-password",
                "new-password");
        performChangePassword("/api/users/me/password", request)
                .andExpect(status().isForbidden());
    }

    /**
     * Returns 403 for DELETE /api/users/me without a token.
     */
    @Test
    public void testDeleteUserWithoutToken() throws Exception {
        performDelete("/api/users/me")
                .andExpect(status().isForbidden());
    }

    /**
     * Throws MalformedJwtException for GET /api/admin/users with a malformed bearer token.
     */
    @Test
    public void testGetAdminUsersWithMalformedToken() throws Exception {
        assertThatThrownBy(() ->
                performGetWithToken("/api/admin/users", "this-isn't-jwt")
                        .andReturn()
        ).isInstanceOf(io.jsonwebtoken.MalformedJwtException.class);
    }

    /**
     * Returns 200 for PUT /api/users/me with a user owning the id.
     */
    @Test
    public void testUpdateUserWithOwnToken() throws Exception {
        String token = userToken();
        UpdateUserRequest request = validUpdateRequest();
        performPutWithToken("/api/users/me", request, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(request.username()))
                .andExpect(jsonPath("$.email").value(request.email()));

        AccountUser updatedUser = accountUserRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo(request.username());
        assertThat(updatedUser.getEmail()).isEqualTo(request.email().toLowerCase().trim());
    }

    /**
     * Returns 204 when DELETE /api/users/me succeeds.
     */
    @Test
    public void testDeleteUserWithOwnToken() throws Exception {
        String token = userToken();
        performDeleteWithToken("/api/users/me", token)
                .andExpect(status().isNoContent());

        assertThat(accountUserRepository.findById(userId)).isEmpty();
    }
}
