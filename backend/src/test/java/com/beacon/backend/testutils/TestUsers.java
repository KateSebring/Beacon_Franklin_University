package com.beacon.backend.testutils;

import com.beacon.backend.dto.user.CreateUserRequest;
import com.beacon.backend.dto.user.UpdateUserRequest;
import com.beacon.backend.model.AccountUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TestUsers {
    private TestUsers() {}

    public static AccountUser user1() {
        return new AccountUser(
                "dafinnell",
                "password",
                "derek",
                "finnell",
                LocalDate.of(2025, 2, 18),
                "derek@derek.com"
        );
    }

    public static AccountUser user2() {
        return new AccountUser(
                "testuser",
                "testpw",
                "testfirst",
                "testlast",
                LocalDate.of(2000,1,1),
                "test@test.com"
        );
    }

    public static List<AccountUser> allUsers() {
        List<AccountUser> users = new ArrayList<>();
        users.add(user1());
        users.add(user2());
        return users;
    }

    public static CreateUserRequest testUserRequest() {
        return new CreateUserRequest(
                "userRequest",
                "derek1234",
                "reqFirst",
                "reqLast",
                LocalDate.of(2000, 1, 2),
                "userRequest@request.com"
        );
    }

    public static CreateUserRequest testUserRequest2() {
        return new CreateUserRequest(
                "derektest",
                "derekmarch",
                "derek",
                "finnell",
                LocalDate.of(1946,3,5),
                "derek@yahoo.com"
        );
    }

    public static UpdateUserRequest updateUserRequest() {
        return new UpdateUserRequest(
                "newUserRequest",
                "newReqFirst",
                "newReqLast",
                LocalDate.of(2001, 1,2),
                "newUserRequest@request.com"
        );
    }

    public static UpdateUserRequest duplicateEmailRequest2() {
        return new UpdateUserRequest(
                "derektest2",
                "dereks",
                "finnell2",
                LocalDate.of(1947,3,5),
                "derek@yahoo.com"
        );
    }
}
