package com.beacon.backend.controller;

import com.beacon.backend.dto.profile.PublicProfileResponse;
import com.beacon.backend.security.AccountUserDetailsService;
import com.beacon.backend.service.JwtService;
import com.beacon.backend.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PublicProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private AccountUserDetailsService accountUserDetailsService;

    /**
     * Returns 200 and the public profile for GET /api/public/profiles/{uuid}.
     */
    @Test
    public void testGetPublicProfileReturns200() throws Exception {
        String uuid = UUID.randomUUID().toString();
        PublicProfileResponse publicProfileResponse = new PublicProfileResponse("Derek Finnell");
        when(profileService.getPublicProfile(uuid)).thenReturn(publicProfileResponse);

        mockMvc.perform(get("/api/public/profiles/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayName").value(publicProfileResponse.displayName()))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.emergencyEmail").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.address").doesNotExist());
    }
}
