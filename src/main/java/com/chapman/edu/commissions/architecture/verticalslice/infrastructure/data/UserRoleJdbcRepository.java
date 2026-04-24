package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.data;

import com.chapman.edu.commissions.architecture.verticalslice.domain.UserRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Roles live in a join table (user_roles) that the {@code User} entity
 * does not map with JPA (the roles field is @Transient). This thin JDBC
 * repository writes and reads those role rows directly so the registration
 * slice can assign roles at signup time and the auth slice can look them up.
 */
@Repository
public class UserRoleJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void assignRole(String userId, UserRole role) {
        jdbcTemplate.update(
            "MERGE INTO user_roles (user_id, role) KEY(user_id, role) VALUES (?, ?)",
            userId, role.name()
        );
    }

    public List<String> findRoleNames(String userId) {
        return jdbcTemplate.queryForList(
            "SELECT role FROM user_roles WHERE user_id = ?",
            String.class,
            userId
        );
    }
}
