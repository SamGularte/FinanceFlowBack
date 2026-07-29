package com.samuelgularte.financeflow.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private static final String TARGET_PATH = "/auth/public/signin";

    private HttpServletRequest mockRequest(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(TARGET_PATH);
        when(request.getHeader("X-Forwarded-For")).thenReturn(ip);
        return request;
    }

    @Nested
    @DisplayName("rate limiting by IP")
    class RateLimiting {

        @Test
        @DisplayName("should allow first 5 requests and block the 6th from the same IP")
        void shouldBlockAfterLimit() throws Exception {
            RateLimitFilter rateLimitFilter = new RateLimitFilter();
            String ip = "192.168.1.1";
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilter(mockRequest(ip), response, chain);
            }

            verify(chain, times(5)).doFilter(any(), any());
            verify(response, never()).setStatus(429);

            HttpServletRequest sixthRequest = mockRequest(ip);
            HttpServletResponse sixthResponse = mock(HttpServletResponse.class);
            StringWriter stringWriter = new StringWriter();
            when(sixthResponse.getWriter()).thenReturn(new PrintWriter(stringWriter));

            rateLimitFilter.doFilter(sixthRequest, sixthResponse, chain);

            verify(sixthResponse).setStatus(429);
            verify(sixthResponse).setContentType("application/json");
            assertTrue(stringWriter.toString().contains("Too many requests"));
        }

        @Test
        @DisplayName("should maintain separate counters for different IPs")
        void shouldSeparateCountersByIp() throws Exception {
            RateLimitFilter rateLimitFilter = new RateLimitFilter();
            String ip1 = "192.168.1.1";
            String ip2 = "10.0.0.1";
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilter(mockRequest(ip1), response, chain);
            }

            rateLimitFilter.doFilter(mockRequest(ip2), response, chain);

            verify(chain, times(6)).doFilter(any(), any());
            verify(response, never()).setStatus(429);
        }

        @Test
        @DisplayName("should reset counter after 1 minute window")
        void shouldResetAfterWindow() throws Exception {
            Instant base = Instant.parse("2026-07-24T12:00:00Z");
            Clock clock = Clock.fixed(base, ZoneOffset.UTC);
            RateLimitFilter rateLimitFilter = new RateLimitFilter(clock);
            String ip = "192.168.1.1";
            FilterChain chain = mock(FilterChain.class);

            HttpServletResponse responseOk = mock(HttpServletResponse.class);
            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilter(mockRequest(ip), responseOk, chain);
            }
            verify(chain, times(5)).doFilter(any(), any());

            HttpServletResponse responseBlocked = mock(HttpServletResponse.class);
            StringWriter blockedWriter = new StringWriter();
            when(responseBlocked.getWriter()).thenReturn(new PrintWriter(blockedWriter));
            rateLimitFilter.doFilter(mockRequest(ip), responseBlocked, chain);
            verify(responseBlocked).setStatus(429);

            Clock advanced = Clock.offset(clock, Duration.ofMinutes(1).plusSeconds(1));
            RateLimitFilter rateLimitFilterAdvanced = new RateLimitFilter(advanced);

            HttpServletResponse responseAfterWindow = mock(HttpServletResponse.class);
            rateLimitFilterAdvanced.doFilter(mockRequest(ip), responseAfterWindow, chain);
            verify(chain, times(6)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle null remote address without throwing")
        void shouldHandleNullIp() throws Exception {
            RateLimitFilter rateLimitFilter = new RateLimitFilter();
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn(TARGET_PATH);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(null);

            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            rateLimitFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not apply filter to non-target paths")
        void shouldSkipNonTargetPaths() throws Exception {
            RateLimitFilter rateLimitFilter = new RateLimitFilter();
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/transactions");

            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            rateLimitFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("should apply rate limit to refresh-token path")
        void shouldRateLimitRefreshToken() throws Exception {
            RateLimitFilter rateLimitFilter = new RateLimitFilter();
            String ip = "10.0.0.2";
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/auth/public/refresh-token");
            when(request.getHeader("X-Forwarded-For")).thenReturn(ip);
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilter(request, response, chain);
            }

            verify(chain, times(5)).doFilter(any(), any());
            verify(response, never()).setStatus(429);

            HttpServletRequest sixthRequest = mock(HttpServletRequest.class);
            when(sixthRequest.getRequestURI()).thenReturn("/auth/public/refresh-token");
            when(sixthRequest.getHeader("X-Forwarded-For")).thenReturn(ip);
            HttpServletResponse sixthResponse = mock(HttpServletResponse.class);
            StringWriter stringWriter = new StringWriter();
            when(sixthResponse.getWriter()).thenReturn(new PrintWriter(stringWriter));

            rateLimitFilter.doFilter(sixthRequest, sixthResponse, chain);

            verify(sixthResponse).setStatus(429);
        }
    }
}
