package com.beacon.backend.controller;

import com.beacon.backend.dto.profile.CreateProfileRequest;
import com.beacon.backend.dto.profile.ProfileResponse;
import com.beacon.backend.dto.profile.UpdateProfileRequest;
import com.beacon.backend.model.Profile;
import com.beacon.backend.security.AccountUserDetailsService;
import com.beacon.backend.service.JwtService;
import com.beacon.backend.service.ProfileService;
import com.beacon.backend.utils.adapter.ProfileMapper;
import com.beacon.backend.utils.exception.InvalidProfileAttributeException;
import com.beacon.backend.utils.exception.ProfileNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.beacon.backend.testutils.TestProfiles.profile1;
import static com.beacon.backend.testutils.TestProfiles.profilesOwnedBy;
import static com.beacon.backend.testutils.TestUsers.user1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private ProfileMapper profileMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AccountUserDetailsService accountUserDetailsService;

    private UsernamePasswordAuthenticationToken authUser() {
        return new UsernamePasswordAuthenticationToken("dafinnell", null);
    }

    @Test
    public void testGetProfilesReturnsOnlyAuthenticatedUsersProfiles() throws Exception {
        List<Profile> profiles = profilesOwnedBy(user1());
        List<ProfileResponse> responses = List.of(
                new ProfileResponse(
                        profiles.get(0).getId(),
                        profiles.get(0).getUuid(),
                        profiles.get(0).getFirstName(),
                        profiles.get(0).getLastName(),
                        profiles.get(0).getEmergencyFirstName(),
                        profiles.get(0).getEmergencyLastName(),
                        profiles.get(0).getEmergencyEmail()
                ),
                new ProfileResponse(
                        profiles.get(1).getId(),
                        profiles.get(1).getUuid(),
                        profiles.get(1).getFirstName(),
                        profiles.get(1).getLastName(),
                        profiles.get(1).getEmergencyFirstName(),
                        profiles.get(1).getEmergencyLastName(),
                        profiles.get(1).getEmergencyEmail()
                )
        );

        when(profileService.getProfiles("dafinnell")).thenReturn(profiles);
        when(profileMapper.allToProfileResponse(profiles)).thenReturn(responses);

        mockMvc.perform(get("/api/profile")
                        .principal(authUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value(responses.get(0).firstName()))
                .andExpect(jsonPath("$[1].firstName").value(responses.get(1).firstName()))
                .andExpect(jsonPath("$[0].emergencyEmail").value(responses.get(0).emergencyEmail()))
                .andExpect(jsonPath("$[1].emergencyEmail").value(responses.get(1).emergencyEmail()));

        verify(profileService).getProfiles("dafinnell");
        verify(profileMapper).allToProfileResponse(profiles);
    }

    @Test
    public void testGetProfileByIdReturns404WhenProfileMissing() throws Exception {
        when(profileService.getProfileById(1, "dafinnell"))
                .thenThrow(new ProfileNotFoundException());

        mockMvc.perform(get("/api/profile/1")
                        .principal(authUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Profile Not Found"))
                .andExpect(jsonPath("$.detail").value("Profile not found for this id or uuid."));
    }

    @Test
    public void testCreateProfileReturns201() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Derek",
                "Finnell",
                "Mom",
                "Finnell",
                "mom@gmail.com"
        );
        Profile createdProfile = profile1();
        ProfileResponse response = new ProfileResponse(
                createdProfile.getId(),
                createdProfile.getUuid(),
                createdProfile.getFirstName(),
                createdProfile.getLastName(),
                createdProfile.getEmergencyFirstName(),
                createdProfile.getEmergencyLastName(),
                createdProfile.getEmergencyEmail()
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenReturn(createdProfile);
        when(profileMapper.toProfileResponse(createdProfile)).thenReturn(response);

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testCreateProfileReturnsGeneratedUuidInDto() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Derek",
                "Finnell",
                "Mom",
                "Finnell",
                "mom@gmail.com"
        );
        Profile createdProfile = profile1();
        ProfileResponse response = new ProfileResponse(
                createdProfile.getId(),
                createdProfile.getUuid(),
                createdProfile.getFirstName(),
                createdProfile.getLastName(),
                createdProfile.getEmergencyFirstName(),
                createdProfile.getEmergencyLastName(),
                createdProfile.getEmergencyEmail()
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenReturn(createdProfile);
        when(profileMapper.toProfileResponse(createdProfile)).thenReturn(response);

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(response.uuid()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()));
    }

    @Test
    public void testCreateProfileRejectsEmptyFirstName() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "",
                "Finnell",
                "Mom",
                "Finnell",
                "mom@gmail.com"
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new InvalidProfileAttributeException(request));

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Profile Attribute"))
                .andExpect(jsonPath("$.detail").value("A profile attribute is invalid"));
    }

    @Test
    public void testCreateProfileRejectsEmptyLastName() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Derek",
                "",
                "Mom",
                "Smith",
                "mom@gmail.com"
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new InvalidProfileAttributeException(request));

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Profile Attribute"))
                .andExpect(jsonPath("$.detail").value("A profile attribute is invalid"));
    }

    @Test
    public void testCreateProfileRejectsEmptyEmergencyFirstName() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Derek",
                "Finnell",
                "",
                "Finnell",
                "mom@gmail.com"
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new InvalidProfileAttributeException(request));

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Profile Attribute"))
                .andExpect(jsonPath("$.detail").value("A profile attribute is invalid"));
    }

    @Test
    public void testCreateProfileRejectsEmptyEmergencyLastName() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Derek",
                "Finnell",
                "Mom",
                "",
                "mom@gmail.com"
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new InvalidProfileAttributeException(request));

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Profile Attribute"))
                .andExpect(jsonPath("$.detail").value("A profile attribute is invalid"));
    }

    @Test
    public void testCreateProfileRejectsEmptyEmergencyEmail() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(
                "Derek",
                "Finnell",
                "Mom",
                "Finnell",
                ""
        );

        when(profileService.createProfile(any(CreateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new InvalidProfileAttributeException(request));

        mockMvc.perform(post("/api/profile")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Profile Attribute"))
                .andExpect(jsonPath("$.detail").value("A profile attribute is invalid"));
    }

    @Test
    public void testModifyProfileReturns200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Derek",
                "Finnell",
                "Mom",
                "Finnell",
                "mom@gmail.com"
        );
        Profile modifiedProfile = profile1();
        ProfileResponse response = new ProfileResponse(
                modifiedProfile.getId(),
                modifiedProfile.getUuid(),
                modifiedProfile.getFirstName(),
                modifiedProfile.getLastName(),
                modifiedProfile.getEmergencyFirstName(),
                modifiedProfile.getEmergencyLastName(),
                modifiedProfile.getEmergencyEmail()
        );

        when(profileService.modifyProfile(eq(1), any(UpdateProfileRequest.class), eq("dafinnell")))
                .thenReturn(modifiedProfile);
        when(profileMapper.toProfileResponse(modifiedProfile)).thenReturn(response);

        mockMvc.perform(put("/api/profile/1")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(response.firstName()));
    }

    @Test
    public void testModifyProfileRejectsInvalidInput() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "",
                "Finnell",
                "Mom",
                "Finnell",
                "mom@gmail.com"
        );

        when(profileService.modifyProfile(eq(1), any(UpdateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new InvalidProfileAttributeException(request));

        mockMvc.perform(put("/api/profile/1")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Profile Attribute"))
                .andExpect(jsonPath("$.detail").value("A profile attribute is invalid"));
    }

    @Test
    public void testModifyProfileReturns404WhenProfileMissing() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Derek",
                "Finnell",
                "Mom",
                "Finnell",
                "mom@gmail.com"
        );

        when(profileService.modifyProfile(eq(1), any(UpdateProfileRequest.class), eq("dafinnell")))
                .thenThrow(new ProfileNotFoundException());

        mockMvc.perform(put("/api/profile/1")
                        .principal(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Profile Not Found"))
                .andExpect(jsonPath("$.detail").value("Profile not found for this id or uuid."));
    }

    @Test
    public void testDeleteProfileReturns204() throws Exception {
        mockMvc.perform(delete("/api/profile/1")
                        .principal(authUser()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteProfileReturns404WhenProfileMissing() throws Exception {
        doThrow(new ProfileNotFoundException()).when(profileService)
                .removeProfileById(1, "dafinnell");

        mockMvc.perform(delete("/api/profile/1")
                        .principal(authUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Profile Not Found"))
                .andExpect(jsonPath("$.detail").value("Profile not found for this id or uuid."));
    }
}
