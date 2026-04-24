package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

import com.chapman.edu.commissions.architecture.verticalslice.domain.User;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.data.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("jsmith", "john@example.com", "John", "Smith");
        user.setId("usr-001");
        user.setPasswordHash("$2a$10$hash");
        user.setActive(true);
    }

    @Test
    void login_withValidCredentials_returnsTokenAndMarksLastLogin() {
        when(userRepository.findByUsername("jsmith")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("sales123", "$2a$10$hash")).thenReturn(true);
        when(jwtService.issueToken("usr-001", "jsmith")).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("jsmith", "sales123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo("usr-001");
        assertThat(response.username()).isEqualTo("jsmith");
        assertThat(response.fullName()).isEqualTo("John Smith");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);

        assertThat(user.getLastLogin()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void login_withUnknownUsername_throwsAuthenticationException() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody", "password123")))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid username or password");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withWrongPassword_throwsAuthenticationException() {
        when(userRepository.findByUsername("jsmith")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("jsmith", "wrong")))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid username or password");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withDeactivatedAccount_throwsAuthenticationException() {
        user.setActive(false);
        when(userRepository.findByUsername("jsmith")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("jsmith", "sales123")))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("deactivated");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withBlankPassword_rejectsAtValidation() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("jsmith", "")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password is required");
    }

    @Test
    void login_withBlankUsername_rejectsAtValidation() {
        assertThatThrownBy(() -> authService.login(new LoginRequest(" ", "password")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username is required");
    }
}
