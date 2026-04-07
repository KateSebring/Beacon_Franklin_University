package com.beacon.backend.service;

import com.beacon.backend.dto.profile.PublicProfileResponse;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.model.Profile;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.repository.ProfileRepository;
import com.beacon.backend.utils.exception.InvalidProfileUuidException;
import com.beacon.backend.utils.exception.ProfileNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.beacon.backend.testutils.TestProfiles.*;
import static com.beacon.backend.testutils.TestUsers.user1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProfileServiceTest {
    private static final int PROFILE_ID = 1;

    @Autowired
    private ProfileService profileService;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private AccountUserRepository accountUserRepository;

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
     * Returns owned profiles when username resolves to an authenticated user.
     */
    @Test
    public void testGetProfilesValidUsername() {
        AccountUser currentUser = user1();
        String currentUsername = currentUser.getUsername();
        List<Profile> profiles = profilesOwnedBy(currentUser);

        when(accountUserRepository.findByUsername(currentUsername))
                .thenReturn(Optional.of(currentUser));
        when(profileRepository.findAllByOwnerUserId(currentUser.getId()))
                .thenReturn(profiles);

        List<Profile> foundProfiles = profileService.getProfiles(currentUsername);

        assertThat(foundProfiles).hasSize(2);
        assertThat(foundProfiles.getFirst().getFirstName()).isEqualTo(profiles.getFirst().getFirstName());
        assertThat(foundProfiles.getFirst().getLastName()).isEqualTo(profiles.getFirst().getLastName());

        assertThat(foundProfiles.getLast().getFirstName()).isEqualTo(profiles.getLast().getFirstName());
        assertThat(foundProfiles.getLast().getLastName()).isEqualTo(profiles.getLast().getLastName());

        verify(accountUserRepository).findByUsername(currentUsername);
        verify(profileRepository).findAllByOwnerUserId(currentUser.getId());
        verifyNoMoreInteractions(accountUserRepository, profileRepository);
    }

    /**
     * Returns an owned profile when id exists for the authenticated user.
     */
    @Test
    public void testGetProfileByIdOwnedProfile() {
        AccountUser currentUser = user1();
        String currentUsername = currentUser.getUsername();
        Profile profile = profilesOwnedBy(currentUser).getFirst();

        when(accountUserRepository.findByUsername(currentUsername))
                .thenReturn(Optional.of(currentUser));
        when(profileRepository.findByIdAndOwnerUserId(PROFILE_ID, currentUser.getId()))
                .thenReturn(Optional.of(profile));

        Profile foundProfile = profileService.getProfileById(PROFILE_ID, currentUsername);

        assertThat(foundProfile.getId()).isEqualTo(profile.getId());
        assertThat(foundProfile.getFirstName()).isEqualTo(profile.getFirstName());
        assertThat(foundProfile.getLastName()).isEqualTo(profile.getLastName());
        assertThat(foundProfile.getEmergencyFirstName()).isEqualTo(profile.getEmergencyFirstName());
        assertThat(foundProfile.getEmergencyLastName()).isEqualTo(profile.getEmergencyLastName());
        assertThat(foundProfile.getEmergencyEmail()).isEqualTo(profile.getEmergencyEmail());

        verify(accountUserRepository).findByUsername(currentUsername);
        verify(profileRepository).findByIdAndOwnerUserId(PROFILE_ID, currentUser.getId());
        verifyNoMoreInteractions(accountUserRepository, profileRepository);
    }

    /**
     * Returns a public profile when the uuid exists.
     */
    @Test
    public void testGetPublicProfileValidUuid() {
        Profile profile = profile1();
        String validUuid = profile.getUuid();
        when(profileRepository.findByUuid(validUuid)).thenReturn(Optional.of(profile));

        PublicProfileResponse publicProfileResponse = profileService.getPublicProfile(validUuid);

        String displayName = profile.getFirstName() + " " + profile.getLastName();
        assertThat(publicProfileResponse.displayName()).isEqualTo(displayName);

        verify(profileRepository).findByUuid(validUuid);
        verifyNoMoreInteractions(profileRepository);
    }

    /**
     * Throws InvalidProfileUuidException when the uuid is malformed.
     */
    @Test
    public void testGetPublicProfileMalformedUuidThrowsException() {
        Profile profile = profile1();
        String badUuid = "badUuid";

        assertThatThrownBy(() -> profileService.getPublicProfile(badUuid))
                .isInstanceOf(InvalidProfileUuidException.class)
                .hasMessage("Invalid profile uuid. Please try again.");

        verifyNoInteractions(profileRepository);
    }

    /**
     * Throws ProfileNotFoundException when the UUID does not match an existing profile.
     */
    @Test
    public void testGetPublicProfileUnknownUuidThrowsProfileNotFoundException() {
        String unknownUuid = UUID.randomUUID().toString();
        when(profileRepository.findByUuid(unknownUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getPublicProfile(unknownUuid))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessage("Profile not found for this id or uuid.");

        verify(profileRepository).findByUuid(unknownUuid);
    }
}
