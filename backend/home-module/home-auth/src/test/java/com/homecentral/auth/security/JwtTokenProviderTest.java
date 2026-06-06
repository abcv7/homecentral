package com.homecentral.auth.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret",
                "homecentral-default-secret-key-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L);
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("access", jwtTokenProvider.getTokenType(token));
        assertEquals(1L, jwtTokenProvider.getUserId(token));
        assertEquals("testuser", jwtTokenProvider.getUsername(token));
    }

    @Test
    void shouldGenerateAndValidateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(1L, "testuser");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("refresh", jwtTokenProvider.getTokenType(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "testuser") + "tampered";
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void shouldParseClaimsCorrectly() {
        String token = jwtTokenProvider.generateAccessToken(42L, "alice");
        Claims claims = jwtTokenProvider.parseToken(token);
        assertEquals("alice", claims.getSubject());
        assertEquals(42L, claims.get("userId", Long.class));
        assertEquals("access", claims.get("type", String.class));
    }
}
