package com.example.demo.security.JWT;


import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
         try {
             String requestURI = request.getRequestURI(); // Get URI for logging
             logger.debug("AuthTokenFilter: Processing request for {}", requestURI);
             // Extract JWT from the Authorization header or cookies
             String jwt = parseAuthorizationOrCookies(request);
             logger.debug("AuthTokenFilter: Parsed JWT: {}", jwt != null ? jwt.substring(0, Math.min(jwt.length(), 15)) + "..." : "null");

             if (jwt != null) {
                 boolean isValid = false;
                 String email = null;
                 try {
                     // Validate and get email in one step if possible, or validate then get
                     isValid = jwtUtils.validateJwtToken(jwt);
                     logger.debug("AuthTokenFilter: JWT validation result for {}: {}", requestURI, isValid);
                     if (isValid) {
                         email = jwtUtils.getEmailFromJwtToken(jwt); // Get email only if valid
                     }
                 } catch (Exception e) {
                     logger.error("AuthTokenFilter: JWT validation/parsing error for {}: {}", requestURI, e.getMessage());
                     // Optionally log stack trace if needed: logger.error("Validation stack trace:", e);
                     // isValid remains false
                 }

                 // Proceed only if token was valid AND email was extracted
                 if (isValid && email != null) {
                     logger.debug("Email from JWT: {}", email);

                     // Load UserDetails by email
                     UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                     // Create authentication object
                     UsernamePasswordAuthenticationToken authentication =
                             new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                     authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                     // Set authentication in the security context
                     SecurityContextHolder.getContext().setAuthentication(authentication);
                 }
             }
         }     catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header or cookies.
     *
     * @param request The HTTP request.
     * @return The JWT token, if available.
     */
    private String parseAuthorizationOrCookies(HttpServletRequest request) {
        // Check Authorization header first
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.debug("Authorization Header detected");
            return authorizationHeader.substring(7); // Remove "Bearer " prefix
        }

        // Proceed to check cookies if JWT not found in the header
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("cnc-cookies")) {
                    logger.debug("JWT token from cookie detected");
                    return cookie.getValue();
                }
            }
        }

        // Return null if no JWT is found
        return null;
    }
}
