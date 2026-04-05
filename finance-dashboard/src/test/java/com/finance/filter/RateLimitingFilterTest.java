package com.finance.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequestsPerClient(2);
        properties.setMaxGlobalRequests(1000);
        properties.setMaxConcurrentRequests(10);
        properties.setWindowSeconds(60);
        properties.setIncludePaths(java.util.List.of("/api/**"));

        rateLimitingFilter = new RateLimitingFilter(properties, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void shouldAllowRequestsWithinLimit() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request.setRemoteAddr("10.10.10.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        rateLimitingFilter.doFilter(request, response, chain);

        assertNotNull(response.getHeader("X-RateLimit-Limit"));
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    void shouldReturn429WhenLimitExceeded() throws Exception {
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request1.setRemoteAddr("10.10.10.2");
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        rateLimitingFilter.doFilter(request1, response1, new MockFilterChain());
        assertEquals(HttpStatus.OK.value(), response1.getStatus());

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request2.setRemoteAddr("10.10.10.2");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitingFilter.doFilter(request2, response2, new MockFilterChain());
        assertEquals(HttpStatus.OK.value(), response2.getStatus());

        MockHttpServletRequest request3 = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request3.setRemoteAddr("10.10.10.2");
        MockHttpServletResponse response3 = new MockHttpServletResponse();
        rateLimitingFilter.doFilter(request3, response3, new MockFilterChain());

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response3.getStatus());
        assertEquals("0", response3.getHeader("X-RateLimit-Remaining"));
        assertNotNull(response3.getHeader("Retry-After"));
        assertTrue(response3.getContentAsString().contains("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void shouldSkipNonApiPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        request.setRemoteAddr("10.10.10.3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        rateLimitingFilter.doFilter(request, response, chain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(response.getHeader("X-RateLimit-Limit"));
    }

    @Test
    void shouldReturn429WhenConcurrencyLimitExceeded() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequestsPerClient(1000);
        properties.setMaxGlobalRequests(1000);
        properties.setMaxConcurrentRequests(1);
        properties.setWindowSeconds(60);
        properties.setIncludePaths(java.util.List.of("/api/**"));
        RateLimitingFilter filter = new RateLimitingFilter(properties, new ObjectMapper().findAndRegisterModules());

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockFilterChain slowChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread firstThread = new Thread(() -> {
            try {
                filter.doFilter(firstRequest, firstResponse, slowChain);
            } catch (Exception ignored) {
            }
        });
        firstThread.start();
        Thread.sleep(50);

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        firstThread.join();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), secondResponse.getStatus());
        assertEquals("concurrency", secondResponse.getHeader("X-RateLimit-Scope"));
    }
}
