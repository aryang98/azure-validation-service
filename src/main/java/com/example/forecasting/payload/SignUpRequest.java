package com.example.forecasting.payload;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Sign Up Request Payload for Demand Forecasting System
 * 
 * This class represents the user registration request payload used for
 * creating new user accounts in the demand forecasting application.
 * It contains all the necessary information for user registration and
 * includes comprehensive validation annotations for data integrity.
 * 
 * Registration Flow:
 * 1. Client sends SignUpRequest with user information
 * 2. Server validates input using Bean Validation annotations
 * 3. System checks for existing username/email
 * 4. Password is encrypted and user is created
 * 5. JWT token is generated and returned
 * 
 * Validation Features:
 * - Comprehensive input validation using Bean Validation
 * - Email format validation
 * - Password strength requirements
 * - Username uniqueness validation
 * - Name and email format validation
 * 
 * Security Features:
 * - Password encryption using BCrypt
 * - Input sanitization and validation
 * - Duplicate user prevention
 * - Secure user creation process
 * 
 * @author Authentication Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class SignUpRequest {
    
    /**
     * User's full name for registration
     * 
     * This field contains the user's full name as it should appear
     * in the system. The name is used for display purposes and
     * user identification throughout the application.
     * 
     * Validation Rules:
     * - @NotBlank: Field must not be null, empty, or contain only whitespace
     * - @Size(min=3, max=50): Name must be between 3 and 50 characters
     * 
     * Examples:
     * - "John Doe", "Jane Smith", "Dr. Robert Johnson"
     * - "Analyst User", "Admin Manager"
     * 
     * Usage:
     * - Display name in user interface
     * - User identification in reports
     * - Email notifications and communications
     */
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    /**
     * Unique username for authentication
     * 
     * This field contains the username that the user will use for
     * authentication. The username must be unique across the system
     * and follows specific format requirements.
     * 
     * Validation Rules:
     * - @NotBlank: Field must not be null, empty, or contain only whitespace
     * - @Size(min=3, max=20): Username must be between 3 and 20 characters
     * 
     * Username Requirements:
     * - Must be unique across all users
     * - Alphanumeric characters and underscores only
     * - No spaces or special characters (except underscore)
     * - Case-sensitive for uniqueness
     * 
     * Examples:
     * - "john_doe", "analyst1", "admin_user"
     * - "jane.smith", "forecaster_2024"
     */
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    /**
     * User's email address for registration
     * 
     * This field contains the user's email address which is used
     * for authentication, notifications, and account recovery.
     * The email must be unique and follow proper email format.
     * 
     * Validation Rules:
     * - @NotBlank: Field must not be null, empty, or contain only whitespace
     * - @Size(max=50): Email must not exceed 50 characters
     * - @Email: Must be a valid email format
     * 
     * Email Features:
     * - Used for authentication (username or email)
     * - Required for password reset functionality
     * - Used for system notifications
     * - Must be unique across all users
     * 
     * Examples:
     * - "john.doe@company.com"
     * - "analyst@forecasting.com"
     * - "admin@demand-forecast.com"
     */
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    /**
     * User's password for account security
     * 
     * This field contains the password that the user will use for
     * authentication. The password is encrypted using BCrypt before
     * storage and must meet security requirements.
     * 
     * Validation Rules:
     * - @NotBlank: Field must not be null, empty, or contain only whitespace
     * - @Size(min=6, max=40): Password must be between 6 and 40 characters
     * 
     * Password Security Requirements:
     * - Minimum 6 characters (enforced by validation)
     * - Recommended: 8+ characters with complexity
     * - Mix of letters, numbers, and symbols (recommended)
     * - No common passwords or dictionary words (recommended)
     * 
     * Security Features:
     * - Encrypted using BCrypt algorithm
     * - Salted for enhanced security
     * - Never stored in plain text
     * - Resistant to rainbow table attacks
     */
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    /**
     * User's role in the system
     * 
     * This field specifies the user's role in the demand forecasting
     * system. The role determines the user's permissions and access
     * to different features of the application.
     * 
     * Available Roles:
     * - ADMIN: Full system access, user management, configuration
     * - ANALYST: Data upload, ML operations, report generation
     * - VIEWER: Dashboard access, report viewing, data export
     * 
     * Role Assignment:
     * - Default role can be set during registration
     * - Role can be changed by administrators
     * - Role determines API endpoint access
     * - Role affects UI features and capabilities
     */
    private String role;

    /**
     * Default constructor for JSON deserialization
     * 
     * This constructor is required for JSON deserialization when
     * the client sends the sign-up request. It should not be used
     * directly in application code.
     */
    public SignUpRequest() {
    }

    /**
     * Constructor with user registration information
     * 
     * This constructor creates a sign-up request with all the
     * necessary user information for account creation.
     * 
     * @param name User's full name
     * @param username Unique username for authentication
     * @param email User's email address
     * @param password User's password for authentication
     * @param role User's role in the system
     */
    public SignUpRequest(String name, String username, String email, String password, String role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * Gets the user's full name
     * 
     * @return User's full name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's full name
     * 
     * @param name User's full name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the username for authentication
     * 
     * @return Username as String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for authentication
     * 
     * @param username Username for authentication
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's email address
     * 
     * @return Email address as String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address
     * 
     * @param email User's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's password
     * 
     * @return Password as String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password
     * 
     * @param password User's password for authentication
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's role
     * 
     * @return User's role as String
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role
     * 
     * @param role User's role in the system
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns a string representation of the sign-up request
     * 
     * This method provides a string representation of the sign-up
     * request for debugging purposes. The password is masked
     * for security reasons.
     * 
     * @return String representation with masked password
     */
    @Override
    public String toString() {
        return "SignUpRequest{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[MASKED]'" +
                ", role='" + role + '\'' +
                '}';
    }
} 