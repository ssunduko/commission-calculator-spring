package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
        "test-secret-test-secret-test-secret-test-secret", 60);

    @Test
    void issueToken_producesTokenThatParsesBackToSameClaims() {
        String token = jwtService.issueToken("usr-123", "jsmith");

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("usr-123");
        assertThat(claims.get("username")).isEqualTo("jsmith");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration().after(claims.getIssuedAt())).isTrue();
    }

    @Test
    void parseToken_rejectsTamperedTokenSignature() {
        String token = jwtService.issueToken("usr-123", "jsmith");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parseToken(tampered))
            .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void parseToken_rejectsTokenSignedWithDifferentSecret() {
        String token = jwtService.issueToken("usr-123", "jsmith");
        JwtService other = new JwtService(
            "different-different-different-different-different-different", 60);

        assertThatThrownBy(() -> other.parseToken(token))
            .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void getExpirationSeconds_reflectsConstructorArgument() {
        JwtService ninety = new JwtService(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 90);
        assertThat(ninety.getExpirationSeconds()).isEqualTo(90 * 60);
    }
}
