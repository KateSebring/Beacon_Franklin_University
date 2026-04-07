package com.beacon.backend.utils.adapter;

import com.beacon.backend.dto.profile.ProfileResponse;
import com.beacon.backend.model.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {
    public ProfileResponse toProfileResponse(Profile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileResponse(
                profile.getId(),
                profile.getUuid(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmergencyFirstName(),
                profile.getEmergencyLastName(),
                profile.getEmergencyEmail()
        );
    }

    public List<ProfileResponse> allToProfileResponse(List<Profile> profiles) {
        return profiles.stream()
                .map(this::toProfileResponse)
                .collect(Collectors.toList());
    }
}