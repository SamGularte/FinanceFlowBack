package com.samuelgularte.financeflow.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("when token is valid")
    class ValidToken {

        @Test
        @DisplayName("should populate SecurityContext with authentication")
        void shouldPopulateSecurityContext() throws Exception {
            String token = "valid-token";
            String username = "joao";
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getAuthorities()).thenReturn(List.of());
            when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
            when(jwtUtils.validateJwtToken(token)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

            authTokenFilter.doFilterInternal(request, response, filterChain);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            assertSame(userDetails, authentication.getPrincipal());
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(401);
        }
    }

    @Nested
    @DisplayName("when there is no token")
    class NoToken {

        @Test
        @DisplayName("should continue the filter chain without authenticating")
        void shouldContinueWithoutAuth() throws Exception {
            when(jwtUtils.getJwtFromHeader(request)).thenReturn(null);

            authTokenFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }
    }

    @Nested
    @DisplayName("when token is invalid")
    class InvalidToken {

        @Test
        @DisplayName("should continue without auth when token validation fails")
        void shouldContinueWhenTokenInvalid() throws Exception {
            String token = "invalid-token";
            when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
            when(jwtUtils.validateJwtToken(token)).thenReturn(false);

            authTokenFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("should return 401 when token is expired")
        void shouldReturn401WhenExpired() throws Exception {
            String token = "expired-token";
            StringWriter stringWriter = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
            when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
            when(jwtUtils.validateJwtToken(token)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(token)).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

            authTokenFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(401);
            assertTrue(stringWriter.toString().contains("Invalid or expired token"));
            verify(filterChain, never()).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("should return 401 when user is not found")
        void shouldReturn401WhenUserNotFound() throws Exception {
            String token = "valid-token";
            String username = "unknown";
            StringWriter stringWriter = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
            when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
            when(jwtUtils.validateJwtToken(token)).thenReturn(true);
            when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username))
                    .thenThrow(new UsernameNotFoundException(username));

            authTokenFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(401);
            assertTrue(stringWriter.toString().contains("User not found"));
            verify(filterChain, never()).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }
}
