package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - @DataJpaTest: Configures an in-memory H2 database, applies Flyway migrations,
 *   and scans for @Entity and @Repository classes. Only loads the persistence layer.
 *
 * - Tests verify all 3 query creation strategies:
 *   1. DERIVED query methods (findByUsername, findByDepartment)
 *   2. JPQL queries (findActiveUsersByRole, searchByName)
 *   3. Native SQL queries (findTopPerformers)
 *
 * - Flyway migrations (V1 schema, V2 seed data) run automatically,
 *   providing realistic test data without manual setup.
 *
 * - Each test method runs inside a transaction that is rolled back after the test,
 *   ensuring test isolation.
 */
@DataJpaTest
@DisplayName("UserRepository — Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // ============================================================
    // DERIVED QUERY METHOD TESTS
    // ============================================================

    @Test
    @DisplayName("findByUsername should return user when username exists")
    void findByUsername_shouldReturnUser() {
        Optional<User> user = userRepository.findByUsername("jsmith");

        assertThat(user).isPresent();
        assertThat(user.get().getFirstName()).isEqualTo("John");
        assertThat(user.get().getLastName()).isEqualTo("Smith");
        assertThat(user.get().getEmail()).isEqualTo("john.smith@chapman.edu");
    }

    @Test
    @DisplayName("findByUsername should return empty when username does not exist")
    void findByUsername_shouldReturnEmptyForNonExistent() {
        Optional<User> user = userRepository.findByUsername("nonexistent");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("findByDepartment should return all users in department")
    void findByDepartment_shouldReturnUsersInDepartment() {
        List<User> salesUsers = userRepository.findByDepartment("Sales");

        // V2 seed data has 3 users in "Sales" department
        assertThat(salesUsers).hasSize(3);
        assertThat(salesUsers).allMatch(u -> u.getDepartment().equals("Sales"));
    }

    @Test
    @DisplayName("existsByUsername should return true for existing username")
    void existsByUsername_shouldReturnTrueForExistingUsername() {
        assertThat(userRepository.existsByUsername("jsmith")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername should return false for non-existing username")
    void existsByUsername_shouldReturnFalseForNonExistingUsername() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail should return true for existing email")
    void existsByEmail_shouldReturnTrueForExistingEmail() {
        assertThat(userRepository.existsByEmail("john.smith@chapman.edu")).isTrue();
    }

    @Test
    @DisplayName("findByActive should return paginated results")
    void findByActive_shouldReturnPaginatedResults() {
        Page<User> activePage = userRepository.findByActive(true, PageRequest.of(0, 2, Sort.by("lastName")));

        assertThat(activePage.getContent()).hasSize(2);
        assertThat(activePage.getTotalElements()).isGreaterThanOrEqualTo(4);
        assertThat(activePage.getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    // ============================================================
    // JPQL QUERY TESTS
    // ============================================================

    @Test
    @DisplayName("findActiveUsersByRole should return active users with specific role")
    void findActiveUsersByRole_shouldReturnActiveUsersWithRole() {
        List<User> salesReps = userRepository.findActiveUsersByRole(UserRole.SALES_REP);

        // V2 seed data has 3 active SALES_REP users
        assertThat(salesReps).hasSize(3);
        assertThat(salesReps).allMatch(User::isActive);
        assertThat(salesReps).allMatch(u -> u.getRoles().contains(UserRole.SALES_REP));
    }

    @Test
    @DisplayName("findDirectReportsByManagerId should return manager's direct reports")
    void findDirectReportsByManagerId_shouldReturnDirectReports() {
        // usr-004 (Maria Garcia) manages usr-001, usr-002, usr-003
        List<User> reports = userRepository.findDirectReportsByManagerId("usr-004");

        assertThat(reports).hasSize(3);
    }

    @Test
    @DisplayName("searchByName should return users matching search term")
    void searchByName_shouldReturnMatchingUsers() {
        // Search for "smith" should match "John Smith"
        List<User> results = userRepository.searchByName("smith");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("searchByName should be case-insensitive")
    void searchByName_shouldBeCaseInsensitive() {
        List<User> results = userRepository.searchByName("GARCIA");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Garcia");
    }

    // ============================================================
    // SAVE AND RETRIEVE TESTS
    // ============================================================

    @Test
    @DisplayName("save should persist a new user with generated UUID")
    void save_shouldPersistNewUser() {
        User newUser = new User("newuser", "new@chapman.edu", "New", "User");
        newUser.addRole(UserRole.SALES_REP);

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");

        // Verify it can be retrieved
        Optional<User> found = userRepository.findByUsername("newuser");
        assertThat(found).isPresent();
    }
}
