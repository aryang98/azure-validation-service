package com.example.forecasting.security;

import com.example.forecasting.model.User;
import com.example.forecasting.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom User Details Service for Demand Forecasting System
 * 
 * This service implements Spring Security's UserDetailsService interface
 * to provide custom user authentication and authorization for the demand
 * forecasting application. It handles user lookup by username or email
 * and converts User entities to UserDetails objects for Spring Security.
 * 
 * Authentication Features:
 * - Username or email-based authentication
 * - Role-based authorization with Spring Security
 * - Transactional user data loading
 * - Comprehensive error handling
 * 
 * Security Integration:
 * - Integrates with Spring Security framework
 * - Provides UserDetails for authentication
 * - Supports JWT token generation
 * - Handles user not found scenarios
 * 
 * Business Rules:
 * - Users can authenticate with username or email
 * - User accounts must be enabled for authentication
 * - Roles are converted to Spring Security authorities
 * - Failed authentication attempts are logged
 * 
 * @author Security Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor with dependency injection
     * 
     * @param userRepository Repository for user data operations
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details by username or email for authentication
     * 
     * This method is called by Spring Security during the authentication
     * process to load user details from the database. It supports both
     * username and email-based authentication for user convenience.
     * 
     * Authentication Process:
     * 1. Attempts to find user by username first
     * 2. If not found, attempts to find user by email
     * 3. Converts User entity to UserDetails object
     * 4. Throws UsernameNotFoundException if user not found
     * 
     * Security Considerations:
     * - User account must be enabled for authentication
     * - Password is encrypted and validated by Spring Security
     * - Roles are converted to Spring Security authorities
     * - Authentication failures are handled gracefully
     * 
     * @param usernameOrEmail Username or email address for authentication
     * @return UserDetails object containing user authentication information
     * @throws UsernameNotFoundException if user is not found or account is disabled
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        // Let people login with either username or email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email : " + usernameOrEmail)));

        return UserPrincipal.create(user);
    }

    /**
     * Loads user details by user ID for JWT token validation
     * 
     * This method is used by the JWT authentication filter to load user
     * details when validating JWT tokens. It provides a way to load user
     * information without requiring username/email authentication.
     * 
     * JWT Integration:
     * - Used by JwtAuthenticationFilter for token validation
     * - Loads user details from user ID in JWT token
     * - Supports stateless authentication
     * - Maintains user context for authorization
     * 
     * @param id User ID to load user details for
     * @return UserDetails object containing user authentication information
     * @throws UsernameNotFoundException if user is not found or account is disabled
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id : " + id)
        );

        return UserPrincipal.create(user);
    }
} 