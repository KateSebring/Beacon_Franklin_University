package com.beacon.backend.service;

import java.util.List;
import java.util.UUID;

import com.beacon.backend.dto.profile.CreateProfileRequest;
import com.beacon.backend.dto.profile.ProfileRequest;
import com.beacon.backend.dto.profile.PublicProfileResponse;
import com.beacon.backend.dto.profile.UpdateProfileRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.repository.ProfileRepository;
import com.beacon.backend.utils.exception.*;
import org.springframework.stereotype.Service;

import com.beacon.backend.model.Profile;

@Service
public class ProfileService {
	private final ProfileRepository profileRepository;
	private final AccountUserRepository accountUserRepository;

	public ProfileService(ProfileRepository profileRepository, AccountUserRepository accountUserRepository) {
		this.profileRepository = profileRepository;
		this.accountUserRepository = accountUserRepository;
	}

	public List<Profile> getProfiles(String currentUsername) {
		AccountUser currentUser = resolveAuthenticatedUser(currentUsername);
		return profileRepository.findAllByOwnerUserId(currentUser.getId());
	}

	public Profile getProfileById(int id, String currentUsername) {
		AccountUser currentUser = resolveAuthenticatedUser(currentUsername);
		return profileRepository.findByIdAndOwnerUserId(id, currentUser.getId())
				.orElseThrow(ProfileNotFoundException::new);
	}

	public Profile createProfile(CreateProfileRequest request, String currentUsername) {
		AccountUser currentUser = resolveAuthenticatedUser(currentUsername);
		Profile profile = new Profile();
		return upsert(profile, request, currentUser);
	}

	public Profile modifyProfile(int id, UpdateProfileRequest request, String currentUsername) {
		AccountUser currentUser = resolveAuthenticatedUser(currentUsername);
		Profile foundProfile = profileRepository.findByIdAndOwnerUserId(id, currentUser.getId())
				.orElseThrow(ProfileNotFoundException::new);

		return upsert(foundProfile, request, currentUser);
	}

	private Profile upsert(Profile target, ProfileRequest request, AccountUser currentUser) {
		// Validate owner & fields
		validateRequest(request);

		// Map fields
		target.setOwner(currentUser);
		target.setFirstName(request.firstName());
		target.setLastName(request.lastName());
		target.setEmergencyFirstName(request.emergencyFirstName());
		target.setEmergencyLastName(request.emergencyLastName());
		target.setEmergencyEmail(request.emergencyEmail());
		
		return profileRepository.save(target);
	}

	public void removeProfileById(int id, String currentUsername) {
		AccountUser currentUser = resolveAuthenticatedUser(currentUsername);
		Profile foundProfile = profileRepository.findByIdAndOwnerUserId(id, currentUser.getId())
				.orElseThrow(ProfileNotFoundException::new);
		int profileId = foundProfile.getId();
		profileRepository.deleteById(profileId);
	}

	public PublicProfileResponse getPublicProfile(String uuid) {
		String normalizedUuid = validateAndNormalizeUuid(uuid);
		Profile foundProfile = profileRepository.findByUuid(normalizedUuid)
				.orElseThrow(ProfileNotFoundException::new);

		// TODO - Maybe trim these before they go into db later and remove trim here
		String displayName = foundProfile.getFirstName().trim() + " " + foundProfile.getLastName().trim();
		return new PublicProfileResponse(displayName);
	}

	private void validateRequest(ProfileRequest request) {
		String firstName = request.firstName();
		String lastName = request.lastName();
		String emergencyFirstName = request.emergencyFirstName();
		String emergencyLastName = request.emergencyLastName();
		String emergencyEmail = request.emergencyEmail();

		if (firstName == null || firstName.isBlank() ||
				lastName == null || lastName.isBlank() ||
				emergencyFirstName == null || emergencyFirstName.isBlank() ||
				emergencyLastName == null || emergencyLastName.isBlank() ||
				emergencyEmail == null || emergencyEmail.isBlank()) {
			throw new InvalidProfileAttributeException(request);
		}

		if (!emergencyEmail.contains("@")) {
			throw new InvalidEmailException(emergencyEmail);
		}
	}

	private AccountUser resolveAuthenticatedUser(String username) {
		if (username == null || username.isBlank()) {
			throw new InvalidUsernameException(username);
		}
		
		String normalizedUsername = username.trim();
		
		return accountUserRepository.findByUsername(normalizedUsername)
				.orElseThrow(() -> new InvalidUsernameException(normalizedUsername));
	}

	private String validateAndNormalizeUuid(String uuid) {
		if (uuid == null) {
			throw new InvalidProfileUuidException();
		}

		String normalizedUuid = uuid.trim();

		if (normalizedUuid.isBlank()) {
			throw new InvalidProfileUuidException();
		}

		try {
			UUID.fromString(normalizedUuid);
		} catch (IllegalArgumentException ex) {
			throw new InvalidProfileUuidException();
		}

		return normalizedUuid;
	}
	
	protected AccountUser findProfileOwnerByUUID(String uuid) {
		String normalizedUuid = validateAndNormalizeUuid(uuid);
		Profile foundProfile = profileRepository.findByUuid(normalizedUuid)
				.orElseThrow(ProfileNotFoundException::new);
		return foundProfile.getOwner();
	}
}
