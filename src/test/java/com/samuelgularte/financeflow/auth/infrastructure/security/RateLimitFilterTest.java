package com.samuelgularte.financeflow.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private static final String TARGET_PATH = "/auth/public/signin";

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
    }

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
            String ip = "192.168.1.1";
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < 5; i++) {
                HttpServletRequest request = mockRequest(ip);
                rateLimitFilter.doFilter(request, response, chain);
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
            String ip1 = "192.168.1.1";
            String ip2 = "10.0.0.1";
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            for (int i = 0; i < 5; i++) {
                rateLimitFilter.doFilter(mockRequest(ip1), response, chain);
            }

            HttpServletRequest ip2Request = mockRequest(ip2);
            rateLimitFilter.doFilter(ip2Request, response, chain);

            verify(chain, times(6)).doFilter(any(), any());
            verify(response, never()).setStatus(429);
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle null remote address without throwing")
        void shouldHandleNullIp() throws Exception {
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
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/auth/public/signup");

            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            rateLimitFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }
}
