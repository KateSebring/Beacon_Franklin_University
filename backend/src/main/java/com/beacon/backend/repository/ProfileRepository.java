package com.beacon.backend.repository;

import com.beacon.backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository
        extends JpaRepository<Profile, Integer> {
    List<Profile> findAllByOwnerUserId(int ownerUserId);
    Optional<Profile> findByIdAndOwnerUserId(int profileId, int ownerUserId);
    Optional<Profile> findByUuid(String uuid);
}
