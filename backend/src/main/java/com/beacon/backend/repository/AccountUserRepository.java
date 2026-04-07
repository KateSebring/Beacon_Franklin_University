package com.beacon.backend.repository;

import com.beacon.backend.model.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountUserRepository
        extends JpaRepository<AccountUser, Integer> {

    boolean existsByEmail(String email);

    Optional<AccountUser> findByUsername(String username);
    boolean existsByUsername(String username);

    boolean existsByEmailAndUserIdNot(String email, int id);
    boolean existsByUsernameAndUserIdNot(String username, int id);
}
