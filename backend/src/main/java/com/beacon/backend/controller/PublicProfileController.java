package com.beacon.backend.controller;

import com.beacon.backend.dto.profile.PublicProfileResponse;
import com.beacon.backend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/profiles")
@CrossOrigin(origins = "http://localhost:3000")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PublicProfileResponse> getProfileByUuid(@PathVariable String uuid) {
        PublicProfileResponse profile = profileService.getPublicProfile(uuid);
        return ResponseEntity.ok(profile);
    }
}
