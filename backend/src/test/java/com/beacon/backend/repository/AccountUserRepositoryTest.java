package com.beacon.backend.repository;

import com.beacon.backend.model.AccountUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static com.beacon.backend.testutils.TestUsers.*;

@DataJpaTest
public class AccountUserRepositoryTest {
    @Autowired
    private AccountUserRepository accountUserRepository;

    /**
     * Saves and returns the user.
     */
    @Test
    public void testSaveAndFindById() {
        AccountUser savedUser = accountUserRepository.save(user1());
        AccountUser foundUser = accountUserRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(foundUser.getEmail()).isEqualTo(savedUser.getEmail());
    }

    /**
     * Returns a list of all users.
     */
    @Test
    public void testAllUsers() {
        accountUserRepository.saveAll(allUsers());
        List<AccountUser> foundUsers = accountUserRepository.findAll();

        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).extracting(AccountUser::getUsername)
                .containsExactlyInAnyOrder(user1().getUsername(), user2().getUsername());
        assertThat(foundUsers).extracting(AccountUser::getEmail)
                .containsExactlyInAnyOrder(user1().getEmail(), user2().getEmail());
    }

    /**
     * Deletes a user by id.
     */
    @Test
    public void testDeleteUser() {
        AccountUser savedUser = accountUserRepository.save(user1());
        accountUserRepository.deleteById(savedUser.getId());
        Optional<AccountUser> foundUser = accountUserRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    /**
     * Returns a user when searched by username.
     */
    @Test
    public void testFindByUsername() {
        AccountUser savedUser = accountUserRepository.save(user1());
        AccountUser foundUser = accountUserRepository.findByUsername(savedUser.getUsername()).orElseThrow();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(foundUser.getEmail()).isEqualTo(savedUser.getEmail());
    }

    /**
     * Does not return an account user if it does not exist when searched by username.
     */
    @Test
    public void testFindByUsernameNotExist() {
        accountUserRepository.save(user1());
        Optional<AccountUser> foundUser = accountUserRepository.findByUsername("notExist");
        assertThat(foundUser).isEmpty();
    }

    /**
     * Returns true if an account user exists when searched by email.
     */
    @Test
    public void testExistsByEmail() {
        AccountUser savedUser = accountUserRepository.save(user1());
        assertThat(accountUserRepository.existsByEmail(savedUser.getEmail())).isTrue();
    }

    /**
     * Returns false if an account user does not exist when searched by email.
     */
    @Test
    public void testNotExistsByEmail() {
        accountUserRepository.save(user1());
        assertThat(accountUserRepository.existsByEmail("notExist")).isFalse();
    }

    /**
     * Returns true if an account user exists when searched by username.
     */
    @Test
    public void testExistsByUsername() {
        AccountUser savedUser = accountUserRepository.save(user1());
        assertThat(accountUserRepository.existsByUsername(savedUser.getUsername())).isTrue();
    }

    /**
     * Returns false if an account user does not exist when searched by username.
     */
    @Test
    public void testNotExistsByUsername() {
        accountUserRepository.save(user1());
        assertThat(accountUserRepository.existsByUsername("notExist")).isFalse();
    }

    /**
     * Returns false when the username belongs to the same user id.
     */
    @Test
    public void testExistsByUsernameAndUserIdNotWhenSameUserHasUsername() {
        AccountUser savedUser = accountUserRepository.save(user1());

        boolean exists = accountUserRepository.existsByUsernameAndUserIdNot(
                savedUser.getUsername(), savedUser.getId());

        assertThat(exists).isFalse();
    }

    /**
     * Returns true when a different user id has the username.
     */
    @Test
    public void testExistsByUsernameAndUserIdNotWhenDifferentUserHasUsername() {
        AccountUser savedUser = accountUserRepository.save(user1());

        boolean exists = accountUserRepository.existsByUsernameAndUserIdNot(
                savedUser.getUsername(), savedUser.getId() + 1);

        assertThat(exists).isTrue();
    }

    /**
     * Returns false when the email belongs to the same user id.
     */
    @Test
    public void testExistsByEmailAndUserIdNotWhenSameUserHasEmail() {
        AccountUser savedUser = accountUserRepository.save(user1());

        boolean exists = accountUserRepository.existsByEmailAndUserIdNot(
                savedUser.getEmail(), savedUser.getId());

        assertThat(exists).isFalse();
    }

    /**
     * Returns true when a different user id has the email.
     */
    @Test
    public void testExistsByEmailAndUserIdNotWhenDifferentUserHasEmail() {
        AccountUser savedUser = accountUserRepository.save(user1());

        boolean exists = accountUserRepository.existsByEmailAndUserIdNot(
                savedUser.getEmail(),
                savedUser.getId() + 1
        );

        assertThat(exists).isTrue();
    }
}
