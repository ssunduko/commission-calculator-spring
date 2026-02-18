package com.chapman.edu.commissions.springboot.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing JWT token generation and validation
 * - Using ReflectionTestUtils to set @Value-injected fields
 * - Testing security components in isolation (without Spring context)
 * - Testing error/edge cases (expired tokens, tampered tokens)
 *
 * WHY ReflectionTestUtils?
 * JwtTokenProvider uses @Value for jwtSecret and jwtExpirationMs.
 * In a unit test, Spring doesn't process @Value annotations.
 * ReflectionTestUtils lets us set those fields directly.
 */
@DisplayName("JwtTokenProvider — Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "ThisIsATestSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L); // 24 hours
    }

    @Test
    @DisplayName("generateToken should return a non-null JWT string")
    void generateToken_shouldReturnValidJwtString() {
        String token = jwtTokenProvider.generateToken("admin");

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        // JWT has 3 parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("getUsernameFromToken should extract the correct username")
    void getUsernameFromToken_shouldExtractCorrectUsername() {
        String token = jwtTokenProvider.generateToken("testuser");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("validateToken should return true for a valid token")
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtTokenProvider.generateToken("admin");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken should return false for a tampered token")
    void validateToken_shouldReturnFalse_forTamperedToken() {
        String token = jwtTokenProvider.generateToken("admin");
        // Tamper with the token by changing a character
        String tampered = token.substring(0, token.length() - 2) + "XX";

        boolean isValid = jwtTokenProvider.validateToken(tampered);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken should return false for a malformed token")
    void validateToken_shouldReturnFalse_forMalformedToken() {
        boolean isValid = jwtTokenProvider.validateToken("not.a.valid.jwt.token");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken should return false for an expired token")
    void validateToken_shouldReturnFalse_forExpiredToken() {
        // Set expiration to 0ms (instantly expired)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 0L);

        String token = jwtTokenProvider.generateToken("admin");

        // Token should be expired immediately
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Different usernames should produce different tokens")
    void generateToken_shouldProduceDifferentTokens_forDifferentUsers() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");

        assertThat(token1).isNotEqualTo(token2);
    }
}
