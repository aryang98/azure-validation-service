package com.example.forecasting.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Filter for Demand Forecasting System
 * 
 * This filter intercepts all HTTP requests to validate JWT tokens and set
 * the authentication context for authorized users. It extends OncePerRequestFilter
 * to ensure it runs once per request and integrates with Spring Security.
 * 
 * Authentication Flow:
 * 1. Intercepts all incoming HTTP requests
 * 2. Extracts JWT token from Authorization header
 * 3. Validates token using JwtTokenProvider
 * 4. Loads user details from database
 * 5. Sets authentication context in SecurityContextHolder
 * 6. Continues request processing
 * 
 * Security Features:
 * - JWT token validation and parsing
 * - User authentication context management
 * - Comprehensive error handling and logging
 * - Stateless authentication support
 * 
 * Integration Points:
 * - Works with JwtTokenProvider for token validation
 * - Uses CustomUserDetailsService for user loading
 * - Integrates with Spring Security framework
 * - Supports stateless REST API authentication
 * 
 * @author Security Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Constructor with dependency injection
     * 
     * @param tokenProvider Provider for JWT token operations
     * @param customUserDetailsService Service for loading user details
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Processes each HTTP request to validate JWT tokens and set authentication
     * 
     * This method is called for every HTTP request and performs the following:
     * 1. Extracts JWT token from Authorization header
     * 2. Validates token authenticity and expiration
     * 3. Loads user details from database
     * 4. Sets authentication context for the request
     * 5. Continues with request processing
     * 
     * Authentication Process:
     * - Extracts "Bearer" token from Authorization header
     * - Validates token using JwtTokenProvider
     * - Extracts user ID from token claims
     * - Loads user details by ID
     * - Creates authentication token
     * - Sets authentication in SecurityContextHolder
     * 
     * Error Handling:
     * - Invalid tokens are ignored (not logged as errors)
     * - Expired tokens are handled gracefully
     * - Malformed tokens are logged for debugging
     * - Database errors are logged and handled
     * 
     * Security Considerations:
     * - Token validation prevents unauthorized access
     * - User context is maintained throughout request
     * - Failed authentication doesn't block request processing
     * - Comprehensive logging for security monitoring
     * 
     * @param request HTTP request to process
     * @param response HTTP response object
     * @param filterChain Spring Security filter chain
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O operations fail
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);

            // Validate token and set authentication context
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Extract user ID from JWT token
                Long userId = tokenProvider.getUserIdFromJWT(jwt);

                // Load user details from database
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                // Set authentication details
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication context for the request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log authentication errors for debugging and monitoring
            logger.error("Could not set user authentication in security context", ex);
        }

        // Continue with request processing
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from HTTP request Authorization header
     * 
     * This method parses the Authorization header to extract the JWT token.
     * It expects the token to be in the format "Bearer <token>" and returns
     * only the token part for validation.
     * 
     * Header Format:
     * - Expected: "Authorization: Bearer <jwt_token>"
     * - Returns: "<jwt_token>" (without "Bearer " prefix)
     * 
     * Validation:
     * - Checks if Authorization header exists
     * - Validates "Bearer " prefix
     * - Ensures token is not empty
     * - Handles malformed headers gracefully
     * 
     * @param request HTTP request containing the Authorization header
     * @return JWT token string or null if not found/invalid format
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if Authorization header exists and has Bearer prefix
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token by removing "Bearer " prefix
            return bearerToken.substring(7);
        }
        
        return null;
    }
} 