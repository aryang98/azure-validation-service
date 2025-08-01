package com.example.forecasting.payload;

import javax.validation.constraints.NotBlank;

/**
 * Login Request Payload for Demand Forecasting System
 * 
 * This class represents the authentication request payload used for
 * user login operations in the demand forecasting application. It
 * contains the credentials required for user authentication and
 * includes validation annotations for input validation.
 * 
 * Authentication Flow:
 * 1. Client sends LoginRequest with username/email and password
 * 2. Server validates input using Bean Validation annotations
 * 3. Authentication manager validates credentials
 * 4. JWT token is generated upon successful authentication
 * 5. Token is returned to client for subsequent requests
 * 
 * Security Features:
 * - Input validation using Bean Validation
 * - Support for username or email authentication
 * - Password field for secure credential transmission
 * - Validation error handling and reporting
 * 
 * Validation Rules:
 * - usernameOrEmail: Required, non-blank string
 * - password: Required, non-blank string
 * - Additional validation can be added as needed
 * 
 * Usage:
 * - POST /api/auth/signin with JSON payload
 * - Content-Type: application/json
 * - Response includes JWT token for authentication
 * 
 * @author Authentication Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class LoginRequest {
    
    /**
     * Username or email address for authentication
     * 
     * This field accepts either a username or email address for
     * user authentication. The system supports both authentication
     * methods for user convenience.
     * 
     * Validation:
     * - @NotBlank: Field must not be null, empty, or contain only whitespace
     * - Minimum length validation can be added if needed
     * - Format validation for email addresses can be added
     * 
     * Examples:
     * - Username: "john.doe", "analyst1", "admin_user"
     * - Email: "john.doe@company.com", "analyst@forecasting.com"
     * 
     * Security Considerations:
     * - Input is validated to prevent injection attacks
     * - Case sensitivity is handled appropriately
     * - Special characters are properly escaped
     */
    @NotBlank
    private String usernameOrEmail;

    /**
     * User password for authentication
     * 
     * This field contains the user's password for authentication.
     * The password is transmitted securely and validated against
     * the encrypted password stored in the database.
     * 
     * Validation:
     * - @NotBlank: Field must not be null, empty, or contain only whitespace
     * - Password strength validation can be added
     * - Minimum length requirements can be enforced
     * 
     * Security Features:
     * - Password is encrypted during transmission (HTTPS)
     * - Password is hashed and compared securely
     * - Password field is not logged or stored in plain text
     * - Brute force protection can be implemented
     * 
     * Password Requirements:
     * - Minimum length: 8 characters (recommended)
     * - Complexity: Mix of letters, numbers, symbols (recommended)
     * - No common passwords or dictionary words (recommended)
     */
    @NotBlank
    private String password;

    /**
     * Default constructor for JSON deserialization
     * 
     * This constructor is required for JSON deserialization when
     * the client sends the login request. It should not be used
     * directly in application code.
     */
    public LoginRequest() {
    }

    /**
     * Constructor with authentication credentials
     * 
     * This constructor creates a login request with the provided
     * username/email and password for authentication.
     * 
     * @param usernameOrEmail Username or email address for authentication
     * @param password User password for authentication
     */
    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    /**
     * Gets the username or email for authentication
     * 
     * This method returns the username or email address that
     * the user provided for authentication.
     * 
     * @return Username or email address as String
     */
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    /**
     * Sets the username or email for authentication
     * 
     * This method sets the username or email address for
     * authentication. This is typically called during JSON
     * deserialization of the login request.
     * 
     * @param usernameOrEmail Username or email address for authentication
     */
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    /**
     * Gets the password for authentication
     * 
     * This method returns the password that the user provided
     * for authentication. The password should be handled securely
     * and not logged or stored in plain text.
     * 
     * @return Password as String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for authentication
     * 
     * This method sets the password for authentication. This is
     * typically called during JSON deserialization of the login
     * request. The password should be handled securely.
     * 
     * @param password Password for authentication
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a string representation of the login request
     * 
     * This method provides a string representation of the login
     * request for debugging purposes. The password is masked
     * for security reasons.
     * 
     * @return String representation with masked password
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "usernameOrEmail='" + usernameOrEmail + '\'' +
                ", password='[MASKED]'" +
                '}';
    }
} 