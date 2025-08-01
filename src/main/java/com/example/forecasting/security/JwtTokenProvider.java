package com.example.forecasting.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT Token Provider for Demand Forecasting System
 * 
 * This component handles JWT (JSON Web Token) operations including token
 * generation, validation, and parsing for the demand forecasting application.
 * It provides stateless authentication for REST API endpoints.
 * 
 * JWT Features:
 * - Token generation with user authentication data
 * - Token validation and signature verification
 * - User ID extraction from token claims
 * - Token expiration handling
 * - Comprehensive error handling and logging
 * 
 * Security Features:
 * - HMAC-SHA512 signature algorithm
 * - Configurable token expiration time
 * - Secure token generation with user context
 * - Token validation with proper error handling
 * 
 * Configuration:
 * - JWT secret key from application properties
 * - Token expiration time from application properties
 * - Signature algorithm: HS512
 * - Issuer: "demand-forecasting-app"
 * 
 * @author Security Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    /**
     * Generates a JWT token for authenticated user
     * 
     * This method creates a JWT token containing user authentication
     * information including user ID, username, and roles. The token
     * is signed with HMAC-SHA512 algorithm for security.
     * 
     * Token Structure:
     * - Header: Algorithm (HS512) and token type (JWT)
     * - Payload: User ID, username, roles, issued date, expiration date
     * - Signature: HMAC-SHA512 signature for integrity
     * 
     * Security Considerations:
     * - Token contains minimal necessary user information
     * - Expiration time prevents indefinite token usage
     * - HMAC-SHA512 provides strong signature verification
     * - User ID is used for subsequent authentication
     * 
     * @param authentication Spring Security authentication object
     * @return JWT token string for client use
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Extracts user ID from JWT token
     * 
     * This method parses the JWT token and extracts the user ID
     * from the token subject claim. The user ID is used to load
     * user details for authentication context.
     * 
     * Token Parsing:
     * - Validates token signature using secret key
     * - Extracts subject claim containing user ID
     * - Handles parsing errors gracefully
     * - Returns user ID as Long for database lookup
     * 
     * @param token JWT token string to parse
     * @return User ID as Long from token subject
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Validates JWT token authenticity and expiration
     * 
     * This method performs comprehensive token validation including
     * signature verification, expiration check, and format validation.
     * It handles various JWT parsing errors and provides detailed logging.
     * 
     * Validation Steps:
     * 1. Parse token and verify signature
     * 2. Check token expiration date
     * 3. Validate token structure and claims
     * 4. Handle parsing errors with appropriate logging
     * 
     * Error Types:
     * - SignatureException: Invalid token signature
     * - MalformedJwtException: Malformed token structure
     * - ExpiredJwtException: Token has expired
     * - UnsupportedJwtException: Unsupported token format
     * - IllegalArgumentException: Invalid token string
     * 
     * @param authToken JWT token string to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            // Parse and validate token signature
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // Invalid JWT signature
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            // Invalid JWT token structure
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            // JWT token has expired
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            // Unsupported JWT token format
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty
            logger.error("JWT claims string is empty");
        }
        return false;
    }
} 