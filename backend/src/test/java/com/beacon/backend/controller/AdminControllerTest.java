package com.beacon.backend.controller;

import com.beacon.backend.model.AccountUser;
import com.beacon.backend.security.AccountUserDetailsService;
import com.beacon.backend.service.AccountUserService;
import com.beacon.backend.service.JwtService;
import com.beacon.backend.utils.adapter.AccountUserMapper;
import com.beacon.backend.utils.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static com.beacon.backend.testutils.TestUsers.allUsers;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AccountUserMapper.class)
public class AdminControllerTest {
    private static final int REQUESTED_USER_ID = 1;
    private static final int MISSING_USER_ID = 9999;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountUserService accountUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AccountUserDetailsService accountUserDetailsService;

    private List<AccountUser> users;

    @BeforeEach
    public void setup() {
        users = allUsers();
    }

    private ResultActions performGetAll() throws Exception {
        return this.mockMvc.perform(get("/api/admin/users"));
    }

    private ResultActions performGetById(int id) throws Exception {
        return this.mockMvc.perform(get("/api/admin/users/{id}", id));
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
     * Returns 200 and a user array for GET /api/admin/users.
     */
    @Test
    public void testAllUsers() throws Exception {
        when(accountUserService.getUsers()).thenReturn(users);
        performGetAll()
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(users.size()))
                .andExpect(jsonPath("$[0].username").value(users.getFirst().getUsername()))
                .andExpect(jsonPath("$[0].email").value(users.getFirst().getEmail()))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[1].username").value(users.get(1).getUsername()))
                .andExpect(jsonPath("$[1].email").value(users.get(1).getEmail()))
                .andExpect(jsonPath("$[1].passwordHash").doesNotExist());

        verify(accountUserService).getUsers();
    }

    /**
     * Returns 200 and an empty array for GET /api/admin/users when there are no users.
     */
    @Test
    public void testAllUsersEmpty() throws Exception {
        when(accountUserService.getUsers()).thenReturn(List.of());

        performGetAll()
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountUserService).getUsers();
    }

    /**
     * Returns 200 and the requested user for GET /api/admin/users/{id}
     */
    @Test
    public void testGetUser() throws Exception {
        when(accountUserService.getUserById(REQUESTED_USER_ID)).thenReturn(users.get(1));
        assertUserResponse(performGetById(REQUESTED_USER_ID), users.get(1));

        verify(accountUserService).getUserById(REQUESTED_USER_ID);
    }

    /**
     * Returns 404 problem details when GET /api/admin/users/{id} uses an unknown id.
     */
    @Test
    public void testGetUserNotFound() throws Exception {
        when(accountUserService.getUserById(MISSING_USER_ID))
                .thenThrow(new UserNotFoundException(MISSING_USER_ID));

        assertProblem(
                performGetById(MISSING_USER_ID),
                status().isNotFound(),
                "User Not Found",
                404,
                "User not found for this id"
        );

        verify(accountUserService).getUserById(MISSING_USER_ID);
    }
}
