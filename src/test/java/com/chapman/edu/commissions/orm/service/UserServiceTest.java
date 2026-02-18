package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.entity.UserRole;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ORM UserService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - @ExtendWith(MockitoExtension.class): Initializes mocks without starting Spring
 * - @Mock: Creates a mock implementation of the repository
 * - @InjectMocks: Creates the service with mocked dependencies injected
 * - Testing @Cacheable/@CacheEvict logic (cache annotations don't fire in unit tests
 *   since they require the Spring proxy; integration tests would be needed for that)
 * - Testing @Transactional isolation and propagation (similarly, requires integration tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ORM UserService — Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("jsmith", "john@test.com", "John", "Smith");
        testUser.setId("usr-001");
        testUser.addRole(UserRole.SALES_REP);
        testUser.setActive(true);
    }

    // ============================================================
    // FIND TESTS
    // ============================================================

    @Test
    @DisplayName("findById should return user when user exists")
    void findById_shouldReturnUser_whenUserExists() {
        when(userRepository.findById("usr-001")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById("usr-001");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jsmith");
        verify(userRepository).findById("usr-001");
    }

    @Test
    @DisplayName("findById should return empty when user does not exist")
    void findById_shouldReturnEmpty_whenUserNotFound() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.findById("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername should return user when username exists")
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        when(userRepository.findByUsername("jsmith")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("jsmith");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
    }

    // ============================================================
    // CREATE TESTS
    // ============================================================

    @Test
    @DisplayName("createUser should save and return user")
    void createUser_shouldSaveAndReturnUser() {
        User newUser = new User("newuser", "new@test.com", "New", "User");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.createUser(newUser);

        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("createUser should throw exception when username already exists")
    void createUser_shouldThrowException_whenUsernameExists() {
        User newUser = new User("jsmith", "different@test.com", "J", "Smith");
        when(userRepository.existsByUsername("jsmith")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser should throw exception when email already exists")
    void createUser_shouldThrowException_whenEmailExists() {
        User newUser = new User("different", "john@test.com", "J", "Smith");
        when(userRepository.existsByUsername("different")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    // ============================================================
    // DEACTIVATE TESTS
    // ============================================================

    @Test
    @DisplayName("deactivateUser should set active to false")
    void deactivateUser_shouldSetActiveToFalse() {
        when(userRepository.findById("usr-001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.deactivateUser("usr-001");

        assertThat(result.isActive()).isFalse();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("deactivateUser should throw exception when user not found")
    void deactivateUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
