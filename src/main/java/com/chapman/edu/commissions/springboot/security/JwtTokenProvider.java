package com.chapman.edu.commissions.springboot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ============================================================================
 * JWT TOKEN PROVIDER — JSON WEB TOKEN GENERATION & VALIDATION
 * ============================================================================
 *
 * CONCEPT: JWT (JSON Web Token)
 * --------------------------------
 * JWT is a compact, URL-safe token format defined in RFC 7519. It consists
 * of three parts separated by dots:
 *
 *   HEADER.PAYLOAD.SIGNATURE
 *
 *   Header:  {"alg": "HS256", "typ": "JWT"}
 *   Payload: {"sub": "admin", "roles": ["SYSTEM_ADMIN"], "iat": 1705000000, "exp": 1705086400}
 *   Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
 *
 * JWT Flow:
 *   1. Client authenticates with username/password
 *   2. Server generates a signed JWT containing user info (claims)
 *   3. Client stores JWT (usually in localStorage or cookie)
 *   4. Client sends JWT in Authorization header: "Bearer <token>"
 *   5. Server validates the signature and extracts claims
 *   6. Server authorizes based on claims (roles, permissions)
 *
 * Security considerations:
 *   - The secret key must be kept private (never in client code)
 *   - Set reasonable expiration times (24h here for demo)
 *   - Use HTTPS to prevent token interception
 *   - Don't store sensitive data in JWT payload (it's Base64-encoded, not encrypted)
 *
 * CONCEPT: @Value — Externalized Configuration
 * -----------------------------------------------
 * @Value injects values from application.properties into fields.
 * This allows configuring JWT settings without changing code:
 *   app.jwt.secret=mySecretKey
 *   app.jwt.expirationMs=86400000
 *
 * @see io.jsonwebtoken.Jwts
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    /**
     * CONCEPT: @Value — Property Injection
     * The @Value annotation injects values from application.properties.
     * The ${...} syntax references a property key.
     * The ":defaultValue" provides a fallback if the property is not set.
     */
    @Value("${app.jwt.secret:ThisIsASecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs:86400000}")  // 24 hours in milliseconds
    private long jwtExpirationMs;

    /**
     * Generate a JWT token for an authenticated user.
     *
     * @param username the username to embed in the token
     * @return the signed JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(username)               // "sub" claim — who this token is for
                .issuedAt(now)                    // "iat" claim — when the token was issued
                .expiration(expiryDate)           // "exp" claim — when the token expires
                .signWith(key)                    // Sign with HMAC-SHA256
                .compact();                       // Build the token string
    }

    /**
     * Extract the username from a JWT token.
     *
     * @param token the JWT token string
     * @return the username (subject claim)
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validate a JWT token.
     *
     * Checks:
     *   - Signature is valid (not tampered with)
     *   - Token is not expired
     *   - Token is well-formed
     *
     * @param token the JWT token to validate
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

            return true;
        } catch (io.jsonwebtoken.security.SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
