package com.beacon.backend.testutils;

import com.beacon.backend.dto.profile.CreateProfileRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.model.Profile;

import java.util.ArrayList;
import java.util.List;

import static com.beacon.backend.testutils.TestUsers.user1;
import static com.beacon.backend.testutils.TestUsers.user2;

public final class TestProfiles {
    private TestProfiles() {}

    private static final String DEFAULT_FIRST_NAME = "John";
    private static final String DEFAULT_LAST_NAME = "Doe";
    private static final String DEFAULT_EMERGENCY_FIRST_NAME = "Mom";
    private static final String DEFAULT_EMERGENCY_LAST_NAME = "Smith";
    private static final String DEFAULT_EMERGENCY_EMAIL = "mom@gmail.com";

    private static final String ALT_FIRST_NAME = "Jane";
    private static final String ALT_LAST_NAME = "Doe";
    private static final String ALT_EMERGENCY_FIRST_NAME = "Dad";
    private static final String ALT_EMERGENCY_LAST_NAME = "Smith";
    private static final String ALT_EMERGENCY_EMAIL = "dad@gmail.com";

    public static Profile testProfileFactory(AccountUser user, String firstName, String lastName,
                                             String emergencyFirstName, String emergencyLastName,
                                             String emergencyEmail) {
        return new Profile(
                user,
                firstName,
                lastName,
                emergencyFirstName,
                emergencyLastName,
                emergencyEmail
        );
    }

    public static Profile profile1() {
        return testProfileFactory(
                user1(),
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_EMERGENCY_FIRST_NAME,
                DEFAULT_EMERGENCY_LAST_NAME,
                DEFAULT_EMERGENCY_EMAIL
        );
    }

    public static Profile profile2() {
        return testProfileFactory(
                user2(),
                ALT_FIRST_NAME,
                ALT_LAST_NAME,
                ALT_EMERGENCY_FIRST_NAME,
                ALT_EMERGENCY_LAST_NAME,
                ALT_EMERGENCY_EMAIL
        );
    }

    public static List<Profile> allProfiles() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(profile1());
        profiles.add(profile2());

        return profiles;
    }

    public static List<Profile> profilesOwnedBy(AccountUser owner) {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(testProfileFactory(
                owner,
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_EMERGENCY_FIRST_NAME,
                DEFAULT_EMERGENCY_LAST_NAME,
                DEFAULT_EMERGENCY_EMAIL
        ));

        profiles.add(testProfileFactory(
                owner,
                ALT_FIRST_NAME,
                ALT_LAST_NAME,
                ALT_EMERGENCY_FIRST_NAME,
                ALT_EMERGENCY_LAST_NAME,
                ALT_EMERGENCY_EMAIL
        ));

        return profiles;
    }

    public static List<Profile> profilesMixedOwners(AccountUser owner1,
                                                    AccountUser owner2) {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(testProfileFactory(
                owner1,
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_EMERGENCY_FIRST_NAME,
                DEFAULT_EMERGENCY_LAST_NAME,
                DEFAULT_EMERGENCY_EMAIL
        ));

        profiles.add(testProfileFactory(
                owner2,
                ALT_FIRST_NAME,
                ALT_LAST_NAME,
                ALT_EMERGENCY_FIRST_NAME,
                ALT_EMERGENCY_LAST_NAME,
                ALT_EMERGENCY_EMAIL
        ));

        return profiles;
    }
    public static CreateProfileRequest request1() {
        return new CreateProfileRequest(
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_EMERGENCY_FIRST_NAME,
                DEFAULT_EMERGENCY_LAST_NAME,
                DEFAULT_EMERGENCY_EMAIL
        );
    }

    public static CreateProfileRequest request2() {
        return new CreateProfileRequest(
                ALT_FIRST_NAME,
                ALT_LAST_NAME,
                ALT_EMERGENCY_FIRST_NAME,
                ALT_EMERGENCY_LAST_NAME,
                ALT_EMERGENCY_EMAIL
        );
    }
}
