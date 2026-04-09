package com.jamel.expense_tracker.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import com.jamel.expense_tracker.service.RateLimiterService;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {
    
    private final RateLimiterService rateLimiter;
    
    public RateLimitFilter(RateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get client IP (or use API key, user ID, etc.)
        String clientIp = httpRequest.getRemoteAddr();
        
        // Check rate limit
        if (!rateLimiter.isAllowed(clientIp)) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
