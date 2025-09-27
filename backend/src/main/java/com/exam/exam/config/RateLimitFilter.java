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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS = 100;
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> resetTimes = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ip = getIpAddress(req);
        long now = System.currentTimeMillis();

        // Reset if minute passed
        Long lastReset = resetTimes.get(ip);
        if (lastReset == null || (now - lastReset) > 60000) {
            requestCounts.put(ip, new AtomicInteger(0));
            resetTimes.put(ip, now);
        }

        AtomicInteger counter = requestCounts.get(ip);
        int currentCount = counter.incrementAndGet();
        

        
        if (currentCount > MAX_REQUESTS) {
            res.setStatus(429);
            res.getWriter().write("Too many requests");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        //  check other headers 
        return request.getRemoteAddr();
    }
}