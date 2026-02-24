package com.pm.authservice.Service;

import com.pm.authservice.Enum.Role;
import com.pm.authservice.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // @Value fields aren't injected without a Spring context —
        // ReflectionTestUtils lets us set private fields directly
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 900000L);   // 15 min
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiryMs", 604800000L); // 7 days
        // simulate what @PostConstruct does at startup
        jwtService.generateKeyPair();
    }

    @Test
    void generateAccessToken_shouldContainExpectedClaims() {
        User user = buildUser(1L, "alice@bank.com", Role.CUSTOMER);

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("alice@bank.com");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
    }

    @Test
    void generateRefreshToken_shouldOnlyContainSubject() {
        User user = buildUser(1L, "alice@bank.com", Role.CUSTOMER);

        String token = jwtService.generateRefreshToken(user);
        Claims claims = jwtService.validateToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        // refresh token is intentionally minimal — no email or role
        assertThat(claims.get("email")).isNull();
        assertThat(claims.get("role")).isNull();
    }

    @Test
    void validateToken_shouldThrow_whenTokenIsExpired() {
        // set expiry to 1ms so the token is immediately stale
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 1L);
        User user = buildUser(1L, "alice@bank.com", Role.CUSTOMER);

        String token = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void validateToken_shouldThrow_whenTokenIsTampered() {
        User user = buildUser(1L, "alice@bank.com", Role.CUSTOMER);
        String token = jwtService.generateAccessToken(user);

        // flip a character in the signature segment
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtService.validateToken(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getPublicKey_shouldReturnNonNullRsaKey() {
        assertThat(jwtService.getPublicKey()).isNotNull();
        assertThat(jwtService.getPublicKey().getAlgorithm()).isEqualTo("RSA");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildUser(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
