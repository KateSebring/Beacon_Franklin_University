package com.beacon.backend.repository;

import com.beacon.backend.model.AccountUser;
import com.beacon.backend.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static com.beacon.backend.testutils.TestProfiles.allProfiles;
import static com.beacon.backend.testutils.TestProfiles.profile1;
import static com.beacon.backend.testutils.TestUsers.user1;
import static com.beacon.backend.testutils.TestUsers.user2;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    /**
     * Persists a profile and retrieves it by id.
     */
    @Test
    public void testSaveAndFindById() {
        AccountUser owner = accountUserRepository.save(user1());
        Profile profile = profile1();
        profile.setOwner(owner);

        Profile savedProfile = profileRepository.save(profile);
        Profile foundProfile = profileRepository.findById(savedProfile.getId()).orElseThrow();

        assertThat(foundProfile.getUuid()).isNotBlank();
        assertThat(foundProfile.getFirstName()).isEqualTo(savedProfile.getFirstName());
        assertThat(foundProfile.getLastName()).isEqualTo(savedProfile.getLastName());
    }

    /**
     * Returns all persisted profiles.
     */
    @Test
    public void testFindAllProfiles() {
        AccountUser owner1 = accountUserRepository.save(user1());
        AccountUser owner2 = accountUserRepository.save(user2());

        List<Profile> profiles = allProfiles();
        profiles.get(0).setOwner(owner1);
        profiles.get(1).setOwner(owner2);
        profileRepository.saveAll(profiles);
        List<Profile> foundProfiles = profileRepository.findAll();

        assertThat(foundProfiles).hasSize(2);
    }

    /**
     * Deletes a persisted profile by id.
     */
    @Test
    public void testDeleteProfile() {
        AccountUser owner1 = accountUserRepository.save(user1());
        AccountUser owner2 = accountUserRepository.save(user2());
        List<Profile> profiles = allProfiles();
        profiles.get(0).setOwner(owner1);
        profiles.get(1).setOwner(owner2);
        profileRepository.saveAll(profiles);

        List<Profile> original = profileRepository.findAll();
        profileRepository.deleteById(original.getFirst().getId());
        List<Profile> modified = profileRepository.findAll();

        assertThat(modified).hasSize(original.size() - 1);
    }

    /**
     * Generates a UUID when a profile created with the empty constructor is persisted.
     */
    @Test
    public void testUuidAutoGenerate() {
        AccountUser owner = accountUserRepository.save(user1());
        Profile empty = new Profile();
        empty.setOwner(owner);
        empty.setFirstName("John");
        empty.setLastName("Doe");
        empty.setEmergencyFirstName("Mom");
        empty.setEmergencyLastName("Smith");
        empty.setEmergencyEmail("mom@gmail.com");

        Profile savedProfile = profileRepository.save(empty);

        Profile foundProfile = profileRepository.findById(savedProfile.getId()).orElseThrow();
        assertThat(foundProfile.getUuid()).isNotBlank();
    }

    /**
     * Persists emergency contact fields when a profile is saved.
     */
    @Test
    public void testEmergencyContactPersistence() {
        AccountUser owner = accountUserRepository.save(user1());
        Profile profile = profile1();
        profile.setOwner(owner);

        Profile savedProfile = profileRepository.save(profile);
        Profile foundProfile = profileRepository.findById(savedProfile.getId()).orElseThrow();

        assertThat(foundProfile.getEmergencyFirstName()).isEqualTo(savedProfile.getEmergencyFirstName());
        assertThat(foundProfile.getEmergencyLastName()).isEqualTo(savedProfile.getEmergencyLastName());
        assertThat(foundProfile.getEmergencyEmail()).isEqualTo(savedProfile.getEmergencyEmail());
    }

    /**
     * Returns a persisted profile when searched by UUID.
     */
    @Test
    public void testFindByUuid() {
        AccountUser owner = accountUserRepository.save(user1());
        Profile profile = profile1();
        profile.setOwner(owner);

        Profile savedProfile = profileRepository.save(profile);
        Profile foundProfile = profileRepository.findByUuid(savedProfile.getUuid()).orElseThrow();

        assertThat(foundProfile.getEmergencyFirstName()).isEqualTo(savedProfile.getEmergencyFirstName());
        assertThat(foundProfile.getEmergencyLastName()).isEqualTo(savedProfile.getEmergencyLastName());
        assertThat(foundProfile.getEmergencyEmail()).isEqualTo(savedProfile.getEmergencyEmail());
    }
}
