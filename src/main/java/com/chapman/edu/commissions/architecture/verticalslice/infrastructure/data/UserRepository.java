package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.data;

import com.chapman.edu.commissions.architecture.verticalslice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing User entities.
 * Located in infrastructure/data since User is shared across slices and there
 * is no dedicated "users" feature slice yet.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
