package com.chapman.edu.commissions.architecture.orthogonal.infrastructure.data;

import com.chapman.edu.commissions.architecture.orthogonal.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing User entities.
 * Located in infrastructure/data since there is no dedicated "users" feature slice.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
