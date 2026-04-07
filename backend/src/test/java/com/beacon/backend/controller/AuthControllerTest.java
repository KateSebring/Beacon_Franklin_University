package com.beacon.backend.controller;

import com.beacon.backend.dto.login.LoginRequest;
import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.security.AccountUserDetailsService;
import com.beacon.backend.service.AccountUserService;
import com.beacon.backend.service.JwtService;
import com.beacon.backend.utils.adapter.AccountUserMapper;
import com.beacon.backend.utils.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static com.beacon.backend.testutils.TestUsers.testUserRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AccountUserMapper.class)
public class AuthControllerTest {
    private CreateUserRequest validCreateUserRequest;
    private AccountUser createdUserResponse;
    private LoginRequest validLoginRequest;
    private String issuedToken;

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
        validCreateUserRequest = testUserRequest();
        createdUserResponse = new AccountUser(
                validCreateUserRequest.username(),
                "hashed-password",
                validCreateUserRequest.firstName(),
                validCreateUserRequest.lastName(),
                validCreateUserRequest.dob(),
                validCreateUserRequest.email()
        );

        validLoginRequest = new LoginRequest("testuser", "pw1");
        issuedToken = "sample-jwt-token";
    }

    private ResultActions performRegister(CreateUserRequest req) throws Exception {
        return this.mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    private ResultActions performLogin(LoginRequest req) throws Exception {
        return this.mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
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
     * Returns 201 and a created user payload for POST /api/auth/register.
     */
    @Test
    public void testAddUser() throws Exception {
        when(accountUserService.registerUser(any(CreateUserRequest.class))).thenReturn(createdUserResponse);

        performRegister(validCreateUserRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(createdUserResponse.getUsername()))
                .andExpect(jsonPath("$.email").value(createdUserResponse.getEmail()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(accountUserService).registerUser(eq(validCreateUserRequest));
    }

    /**
     * Returns 400 problem details when POST /api/auth/register uses an invalid email.
     */
    @Test
    public void testAddUserInvalidEmail() throws Exception {
        CreateUserRequest req = testUserRequest();
        when(accountUserService.registerUser(any(CreateUserRequest.class)))
                .thenThrow(new InvalidEmailException(req.email()));

        assertProblem(
                performRegister(req),
                status().isBadRequest(),
                "Invalid Email",
                400,
                "Invalid email address. Please try again."
        );

        verify(accountUserService).registerUser(any(CreateUserRequest.class));
    }

    /**
     * Returns 400 problem details when POST /api/auth/register uses an invalid username.
     */
    @Test
    public void testAddUserInvalidUsername() throws Exception {
        CreateUserRequest req = testUserRequest();
        when(accountUserService.registerUser(any(CreateUserRequest.class)))
                .thenThrow(new InvalidUsernameException(req.username()));

        assertProblem(
                performRegister(req),
                status().isBadRequest(),
                "Invalid Username",
                400,
                "Invalid username. Please try again."
        );

        verify(accountUserService).registerUser(any(CreateUserRequest.class));
    }

    /**
     * Returns 400 problem details when POST /api/auth/register uses an invalid password.
     */
    @Test
    public void testAddUserInvalidPassword() throws Exception {
        CreateUserRequest req = testUserRequest();
        when(accountUserService.registerUser(any(CreateUserRequest.class)))
                .thenThrow(new InvalidPasswordException());

        assertProblem(
                performRegister(req),
                status().isBadRequest(),
                "Invalid Password",
                400,
                "Enter a valid password"
        );

        verify(accountUserService).registerUser(any(CreateUserRequest.class));
    }

    /**
     * Returns 409 problem details when POST /api/auth/register uses a duplicate email.
     */
    @Test
    public void testAddUserDuplicateEmail() throws Exception {
        CreateUserRequest req = testUserRequest();
        when(accountUserService.registerUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateEmailException(req.email()));

        assertProblem(
                performRegister(req),
                status().isConflict(),
                "Duplicate Email",
                409,
                "User already exists with this email " + req.email()
        );

        verify(accountUserService).registerUser(any(CreateUserRequest.class));
    }

    /**
     * Returns 409 problem details when POST /api/auth/register uses a duplicate username.
     */
    @Test
    public void testAddUserDuplicateUsername() throws Exception {
        CreateUserRequest req = testUserRequest();
        when(accountUserService.registerUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateUsernameException());

        assertProblem(
                performRegister(req),
                status().isConflict(),
                "Duplicate Username",
                409,
                "This username already exists"
        );

        verify(accountUserService).registerUser(any(CreateUserRequest.class));
    }

    /**
     * Returns 400 when POST /api/auth/register receives malformed JSON.
     */
    @Test
    public void testAddUserBadJson() throws Exception {
        String badJson = "{\"username\":\"abc\", \"email\": }";

        this.mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountUserService);
    }

    /**
     * Returns 200 and a JWT token for POST /api/auth/login with valid credentials.
     */
    @Test
    public void testLoginUser() throws Exception {
        when(accountUserService.loginUser(any(LoginRequest.class))).thenReturn(issuedToken);

        performLogin(validLoginRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(issuedToken));

        verify(accountUserService).loginUser(eq(validLoginRequest));
    }

    /**
     * Returns 401 problem details for POST /api/auth/login with invalid credentials.
     */
    @Test
    public void testLoginUserBadCredentials() throws Exception {
        LoginRequest req = new LoginRequest("testuser", "wrong-password");
        when(accountUserService.loginUser(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertProblem(
                performLogin(req),
                status().isUnauthorized(),
                "Authentication Failed",
                401,
                "Bad credentials"
        );

        verify(accountUserService).loginUser(any(LoginRequest.class));
    }

    /**
     * Returns 400 when POST /api/auth/login receives malformed JSON.
     */
    @Test
    public void testLoginUserBadJson() throws Exception {
        String badJson = "{\"username\":\"abc\", \"password\": }";

        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountUserService);
    }
}
