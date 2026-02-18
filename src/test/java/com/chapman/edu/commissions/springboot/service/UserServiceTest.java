package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import com.chapman.edu.commissions.springboot.dto.request.CreateUserRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Spring Boot UserService.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing password hashing (PasswordEncoder mock)
 * - Testing business validation (duplicate username check)
 * - Testing default role assignment
 * - Testing service orchestration patterns
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringBoot UserService — Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateUserRequest();
        createRequest.setUsername("newuser");
        createRequest.setEmail("new@test.com");
        createRequest.setFirstName("New");
        createRequest.setLastName("User");
        createRequest.setPassword("password123");
        createRequest.setDepartment("Sales");
    }

    @Test
    @DisplayName("createUser should hash password and save")
    void createUser_shouldHashPasswordAndSave() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("user-new");
            return u;
        });

        User result = userService.createUser(createRequest);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser should throw when username is taken")
    void createUser_shouldThrowBusinessValidation_whenUsernameTaken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser should assign default SALES_REP role when no roles provided")
    void createUser_shouldAssignDefaultRole_whenNoRolesProvided() {
        createRequest.setRoles(null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(createRequest);

        assertThat(result.getRoles()).contains(UserRole.SALES_REP);
    }

    @Test
    @DisplayName("createUser should assign specified roles")
    void createUser_shouldAssignSpecifiedRoles() {
        createRequest.setRoles(Set.of("SALES_MANAGER", "FINANCE_ADMIN"));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(createRequest);

        assertThat(result.getRoles()).contains(UserRole.SALES_MANAGER, UserRole.FINANCE_ADMIN);
    }

    @Test
    @DisplayName("getUserById should return user when exists")
    void getUserById_shouldReturnUser_whenExists() {
        User user = new User("jsmith", "john@test.com", "John", "Smith");
        user.setId("user-001");
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));

        User result = userService.getUserById("user-001");

        assertThat(result.getUsername()).isEqualTo("jsmith");
    }

    @Test
    @DisplayName("getUserById should throw ResourceNotFoundException when not found")
    void getUserById_shouldThrowResourceNotFoundException_whenNotExists() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deactivateUser should set inactive and save")
    void deactivateUser_shouldSetInactiveAndSave() {
        User user = new User("jsmith", "john@test.com", "John", "Smith");
        user.setId("user-001");
        user.setActive(true);
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.deactivateUser("user-001");

        assertThat(result.isActive()).isFalse();
        verify(userRepository).save(user);
    }
}
