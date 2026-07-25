package com.samuelgularte.financeflow.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final Clock clock;

    public RateLimitFilter() {
        this.clock = Clock.systemUTC();
    }

    public RateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String key = request.getHeader("X-Forwarded-For");
        if (key == null || key.isBlank()) {
            key = request.getRemoteAddr();
        }
        if (key == null || key.isBlank()) {
            key = "unknown";
        }

        Instant now = clock.instant();
        Deque<Instant> timestamps = requests.computeIfAbsent(key, k -> new LinkedList<>());

        synchronized (timestamps) {
            timestamps.removeIf(t -> t.isBefore(now.minus(WINDOW)));
            if (timestamps.size() >= MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests. Try again later.\"}");
                return;
            }
            timestamps.addLast(now);
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/auth/public/signin") && !path.equals("/auth/public/forgot-password") && !path.equals("/auth/public/refresh-token");
    }
}
