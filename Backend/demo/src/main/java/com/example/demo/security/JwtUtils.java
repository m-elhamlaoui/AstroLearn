package com.example.demo.security;



import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);


    // Load the secret key from application properties or environment variables
    @Value("${demo.app.jwtSecret}")
    private String jwtSecret;

    @Value("${demo.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${demo.app.jwtCookieName}")
    private String jwtCookie;

    /**
     * Retrieve the JWT token from cookies.
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        return (cookie != null) ? cookie.getValue() : null;
    }

    /**
     * Generate a JWT token and store it in a cookie.
     */
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateJwtToken(userPrincipal);
        return ResponseCookie.from(jwtCookie, jwt)
                .path("/")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();
    }

    /**
     * Clean the JWT cookie (remove it).
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, null)
                .path("/")
                .build();
    }

    /**
     * Generate a JWT token using the user's email.
     */
    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return Jwts.builder()
                .setSubject(userPrincipal.getEmail()) // Email is used as subject
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim("roles", userPrincipal.getAuthorities()) // Include roles as a claim
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * Generate a token directly from the email (alternative).
     */
    public String generateTokenFromEmail(String email) {
        SecretKey secret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email) // Email is set as subject
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(secret)
                .compact();
    }

    /**
     * Extract the email from the JWT token.
     */
    public String getEmailFromJwtToken(String token) {
        SecretKey secret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // Email is stored as the subject
    }

    /**
     * Get roles or other information (if you want to extract custom claims).
     */
    public Object getRolesFromJwtToken(String token) {
        SecretKey secret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("roles"); // Extract roles claim as an example
    }

    /**
     * Validate the JWT token.
     */
    public boolean validateJwtToken(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            logger.error("JWT token is null or empty");
            return false;
        }

        logger.debug("JWT token received: {}", authToken);

        try {
            SecretKey secret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(authToken);
            logger.debug("JWT token signature verified successfully.");
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}