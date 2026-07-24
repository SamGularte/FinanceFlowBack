package com.samuelgularte.financeflow.auth.infrastructure.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private static final String BASE64_SECRET = "dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciB0ZXN0aW5nIHB1cnBvc2VzIDEyMzQ1Njc4OTA=";
    private static final int EXPIRATION_MS = 60000;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", BASE64_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
    }

    @Nested
    @DisplayName("token generation and parsing")
    class TokenGeneration {

        @Test
        @DisplayName("should generate token and parse username back")
        void shouldRoundtripUsername() {
            String username = "joao";

            String token = jwtUtils.generateTokenFromUsername(username);
            String parsed = jwtUtils.getUserNameFromJwtToken(token);

            assertEquals(username, parsed);
        }
    }

    @Nested
    @DisplayName("token validation")
    class TokenValidation {

        @Test
        @DisplayName("should return true for a valid token")
        void shouldValidateValidToken() {
            String token = jwtUtils.generateTokenFromUsername("joao");

            assertTrue(jwtUtils.validateJwtToken(token));
        }

        @Test
        @DisplayName("should return false for an expired token")
        void shouldRejectExpiredToken() {
            String expiredToken = Jwts.builder()
                    .subject("joao")
                    .issuedAt(new Date(System.currentTimeMillis() - 100000))
                    .expiration(new Date(System.currentTimeMillis() - 50000))
                    .signWith(key())
                    .compact();

            assertFalse(jwtUtils.validateJwtToken(expiredToken));
        }

        @Test
        @DisplayName("should return false for a tampered token")
        void shouldRejectTamperedToken() {
            String token = jwtUtils.generateTokenFromUsername("joao");
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";

            assertFalse(jwtUtils.validateJwtToken(tampered));
        }
    }

    @Nested
    @DisplayName("header parsing")
    class HeaderParsing {

        @Test
        @DisplayName("should return null when Authorization header is missing")
        void shouldReturnNullWhenHeaderMissing() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(null);

            assertNull(jwtUtils.getJwtFromHeader(request));
        }

        @Test
        @DisplayName("should return null when header does not start with Bearer")
        void shouldReturnNullWhenNotBearer() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Basic some-token");

            assertNull(jwtUtils.getJwtFromHeader(request));
        }
    }
}
