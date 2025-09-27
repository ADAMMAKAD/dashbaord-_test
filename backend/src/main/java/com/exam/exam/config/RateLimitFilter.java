package com.exam.exam.config;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitFilter implements Filter {

    // Simple rate limiting - max 50 requests per minute
    private static final int MAX_REQUESTS = 50;
    private Map<String, Integer> requestCounts = new HashMap<>();
    private Map<String, Long> lastResetTime = new HashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String clientIp = req.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        // Check if we need to reset the counter (every minute)
        Long lastReset = lastResetTime.get(clientIp);
        if (lastReset == null || (currentTime - lastReset) > 60000) {
            requestCounts.put(clientIp, 0);
            lastResetTime.put(clientIp, currentTime);
        }

        // Increment request count for this IP
        Integer count = requestCounts.get(clientIp);
        if (count == null) count = 0;
        count++;
        requestCounts.put(clientIp, count);

        // Block if too many requests
        if (count > MAX_REQUESTS) {
            res.setStatus(429);
            res.getWriter().write("Rate limit exceeded. Try again later.");
            return;
        }

        // Continue with the request
        chain.doFilter(request, response);
    }
}