package com.example.forecasting.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Entry Point for Demand Forecasting System
 * 
 * This component handles unauthorized access attempts and authentication
 * failures in the demand forecasting application. It provides custom
 * error responses for various authentication scenarios and logs security
 * events for monitoring and debugging purposes.
 * 
 * Authentication Failure Scenarios:
 * - Missing JWT token in request
 * - Invalid or expired JWT token
 * - Malformed authentication headers
 * - Unauthorized access to protected resources
 * - Authentication context failures
 * 
 * Security Features:
 * - Custom error response format
 * - Comprehensive security logging
 * - HTTP status code management
 * - Request information capture
 * - Security event tracking
 * 
 * Response Format:
 * - HTTP 401 Unauthorized status code
 * - JSON error response with details
 * - Security headers for client guidance
 * - Consistent error message format
 * 
 * @author Security Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Handles unauthorized access attempts and authentication failures
     * 
     * This method is called by Spring Security when an unauthenticated
     * user attempts to access a protected resource or when authentication
     * fails for any reason. It provides a custom error response and logs
     * the security event for monitoring purposes.
     * 
     * Authentication Failure Handling:
     * - Logs the unauthorized access attempt with request details
     * - Sets appropriate HTTP status code (401 Unauthorized)
     * - Provides custom error response in JSON format
     * - Includes security headers for client guidance
     * - Captures request information for security analysis
     * 
     * Security Logging:
     * - Records unauthorized access attempts
     * - Logs request details for security analysis
     * - Tracks authentication failure patterns
     * - Provides debugging information for security issues
     * 
     * Error Response:
     * - HTTP 401 Unauthorized status
     * - JSON response with error details
     * - Consistent error message format
     * - Security headers for client handling
     * 
     * @param request HTTP request that caused the authentication failure
     * @param response HTTP response to send error details
     * @param authException Authentication exception that occurred
     * @throws IOException if response writing fails
     */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        // Log the unauthorized access attempt with request details
        logger.error("Unauthorized error: {} - Request: {} {} from IP: {}",
                authException.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                getClientIpAddress(request));

        // Set HTTP status code to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Set content type for JSON response
        response.setContentType("application/json;charset=UTF-8");
        
        // Set security headers
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Create custom error response
        String errorResponse = createErrorResponse(request, authException);
        
        // Write error response to client
        response.getWriter().write(errorResponse);
    }

    /**
     * Creates a custom error response for authentication failures
     * 
     * This method generates a structured JSON error response that
     * provides useful information to the client about the authentication
     * failure while maintaining security by not exposing sensitive details.
     * 
     * Error Response Structure:
     * - Error code and message
     * - Request details for debugging
     * - Timestamp for tracking
     * - Security guidance for client
     * 
     * @param request HTTP request that caused the failure
     * @param authException Authentication exception details
     * @return JSON formatted error response string
     */
    private String createErrorResponse(HttpServletRequest request, AuthenticationException authException) {
        return String.format(
            "{\n" +
            "  \"error\": {\n" +
            "    \"code\": \"UNAUTHORIZED\",\n" +
            "    \"message\": \"Authentication required for this resource\",\n" +
            "    \"details\": \"%s\",\n" +
            "    \"timestamp\": \"%s\",\n" +
            "    \"path\": \"%s\",\n" +
            "    \"method\": \"%s\"\n" +
            "  }\n" +
            "}",
            authException.getMessage(),
            java.time.Instant.now(),
            request.getRequestURI(),
            request.getMethod()
        );
    }

    /**
     * Extracts the client IP address from the HTTP request
     * 
     * This method determines the actual client IP address by checking
     * various HTTP headers that may contain the real IP address when
     * the application is behind a proxy or load balancer.
     * 
     * IP Address Detection:
     * - Checks X-Forwarded-For header for proxy IPs
     * - Checks X-Real-IP header for real client IP
     * - Falls back to remote address if headers not present
     * - Handles multiple IP addresses in forwarded headers
     * 
     * Security Considerations:
     * - Helps identify source of unauthorized access attempts
     * - Supports security monitoring and threat detection
     * - Assists in rate limiting and access control
     * - Provides debugging information for security issues
     * 
     * @param request HTTP request to extract IP from
     * @return Client IP address as String
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check for forwarded IP address (common with proxies/load balancers)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check for real IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        // Fall back to remote address
        return request.getRemoteAddr();
    }
} 