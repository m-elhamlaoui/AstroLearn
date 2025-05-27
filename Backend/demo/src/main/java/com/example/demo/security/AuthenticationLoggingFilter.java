package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to log authentication details for debugging purposes
 */
public class AuthenticationLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Only log for specific endpoints we're troubleshooting
        if (requestURI.startsWith("/articles") && "POST".equals(method)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            logger.info("==== Authentication Debug Info ====");
            logger.info("Request: {} {}", method, requestURI);
            logger.info("Auth present: {}", (auth != null));
            
            if (auth != null) {
                logger.info("Auth name: {}", auth.getName());
                logger.info("Auth principal: {}", auth.getPrincipal());
                logger.info("Auth authorities: {}", auth.getAuthorities());
                logger.info("Auth details: {}", auth.getDetails());
                logger.info("Auth authenticated: {}", auth.isAuthenticated());
            }
            
            // Log headers for debugging
            logger.info("Authorization header: {}", request.getHeader("Authorization"));
        }
        
        filterChain.doFilter(request, response);
    }
}
