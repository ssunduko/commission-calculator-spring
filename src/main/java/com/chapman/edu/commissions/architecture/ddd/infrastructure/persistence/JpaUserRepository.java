package com.chapman.edu.commissions.architecture.ddd.infrastructure.persistence;

import com.chapman.edu.commissions.architecture.ddd.domain.user.User;
import com.chapman.edu.commissions.architecture.ddd.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the domain UserRepository.
 * Spring Data JPA auto-generates the query methods at runtime.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
}
