package com.beacon.backend.controller;

import com.beacon.backend.dto.profile.CreateProfileRequest;
import com.beacon.backend.dto.profile.ProfileResponse;
import com.beacon.backend.dto.profile.UpdateProfileRequest;
import com.beacon.backend.utils.adapter.ProfileMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.beacon.backend.model.Profile;
import com.beacon.backend.service.ProfileService;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {
	
	private final ProfileService profileService;
	private final ProfileMapper profileMapper;
	
	public ProfileController(ProfileService profileService,
							 ProfileMapper profileMapper) {
		this.profileService = profileService;
		this.profileMapper = profileMapper;
	}

	@GetMapping
	public ResponseEntity<List<ProfileResponse>> getAllProfiles(Authentication authentication) {
		String currentUsername = authentication.getName();
		List<Profile> profiles = profileService.getProfiles(currentUsername);
		return ResponseEntity.ok(profileMapper.allToProfileResponse(profiles));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProfileResponse> getProfileById(@PathVariable int id, Authentication authentication) {
		String currentUsername = authentication.getName();
		Profile profile = profileService.getProfileById(id, currentUsername);
		return ResponseEntity.ok(profileMapper.toProfileResponse(profile));
	}

	@PostMapping
	public ResponseEntity<ProfileResponse> createProfile(@RequestBody CreateProfileRequest request,
														 Authentication authentication) {
		String currentUsername = authentication.getName();
		Profile created = profileService.createProfile(request, currentUsername);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(profileMapper.toProfileResponse(created));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProfileResponse> modifyProfile(@PathVariable int id,
														 @RequestBody UpdateProfileRequest request,
														 Authentication authentication) {
		String currentUsername = authentication.getName();
		Profile modified = profileService.modifyProfile(id, request, currentUsername);
		return ResponseEntity.ok(profileMapper.toProfileResponse(modified));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> removeProfile(@PathVariable int id, Authentication authentication) {
		String currentUsername = authentication.getName();
		profileService.removeProfileById(id, currentUsername);
		return ResponseEntity.noContent().build();
	}
}
