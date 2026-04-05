package com.finance.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.config.RateLimitProperties;
import com.finance.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";

    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ConcurrentHashMap<String, CounterWindow> clientWindows = new ConcurrentHashMap<>();
    private final CounterWindow globalWindow = new CounterWindow();
    private final AtomicLong lastCleanupEpochSecond = new AtomicLong(0);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!rateLimitProperties.isEnabled() || !shouldRateLimit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long nowEpochSecond = System.currentTimeMillis() / 1000;
        cleanupOldClientWindows(nowEpochSecond);

        CounterResult globalCounter = globalWindow.incrementAndCheck(
                nowEpochSecond,
                rateLimitProperties.getMaxGlobalRequests(),
                rateLimitProperties.getWindowSeconds()
        );

        if (!globalCounter.allowed()) {
            writeTooManyRequests(
                    response,
                    globalCounter,
                    rateLimitProperties.getMaxGlobalRequests(),
                    "global"
            );
            return;
        }

        String clientKey = resolveClientKey(request);
        CounterWindow clientWindow = clientWindows.computeIfAbsent(clientKey, key -> new CounterWindow());
        CounterResult clientCounter = clientWindow.incrementAndCheck(
                nowEpochSecond,
                rateLimitProperties.getMaxRequestsPerClient(),
                rateLimitProperties.getWindowSeconds()
        );

        if (!clientCounter.allowed()) {
            log.warn("Rate limit exceeded for key={} path={} method={}", clientKey, request.getRequestURI(), request.getMethod());
            writeTooManyRequests(
                    response,
                    clientCounter,
                    rateLimitProperties.getMaxRequestsPerClient(),
                    "client"
            );
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitProperties.getMaxRequestsPerClient()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(clientCounter.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(clientCounter.retryAfterSeconds()));

        filterChain.doFilter(request, response);
    }

    private boolean shouldRateLimit(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String requestUri = request.getRequestURI();
        return rateLimitProperties.getIncludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "user:" + authentication.getName();
        }

        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0 && !parts[0].isBlank()) {
                return parts[0].trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void cleanupOldClientWindows(long nowEpochSecond) {
        long windowSeconds = rateLimitProperties.getWindowSeconds();
        long lastCleanup = lastCleanupEpochSecond.get();
        if (nowEpochSecond - lastCleanup < windowSeconds) {
            return;
        }
        if (!lastCleanupEpochSecond.compareAndSet(lastCleanup, nowEpochSecond)) {
            return;
        }

        long staleThreshold = nowEpochSecond - (windowSeconds * 2);
        clientWindows.entrySet().removeIf(entry -> entry.getValue().getWindowStartEpochSecond() < staleThreshold);
    }

    private void writeTooManyRequests(
            HttpServletResponse response,
            CounterResult counterResult,
            int limit,
            String scope
    ) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(counterResult.retryAfterSeconds()));
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(counterResult.retryAfterSeconds()));
        response.setHeader("X-RateLimit-Scope", scope);

        Map<String, String> details = new LinkedHashMap<>();
        details.put("scope", scope);
        details.put("limit", String.valueOf(limit));
        details.put("windowSeconds", String.valueOf(rateLimitProperties.getWindowSeconds()));

        ApiError error = ApiError.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .message("Rate limit exceeded. Please retry later.")
                .code(RATE_LIMIT_EXCEEDED)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        objectMapper.writeValue(response.getWriter(), error);
    }

    private static class CounterWindow {
        private long windowStartEpochSecond;
        private int count;

        synchronized CounterResult incrementAndCheck(long nowEpochSecond, int limit, long windowSeconds) {
            if (windowStartEpochSecond == 0 || nowEpochSecond - windowStartEpochSecond >= windowSeconds) {
                windowStartEpochSecond = nowEpochSecond;
                count = 0;
            }

            count++;
            int remaining = Math.max(limit - count, 0);
            long retryAfterSeconds = Math.max(windowSeconds - (nowEpochSecond - windowStartEpochSecond), 1);
            return new CounterResult(count <= limit, remaining, retryAfterSeconds);
        }

        synchronized long getWindowStartEpochSecond() {
            return windowStartEpochSecond;
        }
    }

    private record CounterResult(boolean allowed, int remaining, long retryAfterSeconds) {
    }
}
