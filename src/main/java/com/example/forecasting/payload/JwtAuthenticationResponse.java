package com.example.forecasting.payload;

/**
 * JWT Authentication Response for Demand Forecasting System
 * 
 * This class represents the authentication response payload returned
 * to clients after successful login or registration in the demand
 * forecasting application. It contains the JWT token and user information
 * needed for subsequent authenticated requests.
 * 
 * Authentication Response Flow:
 * 1. User submits credentials via LoginRequest or SignUpRequest
 * 2. Server validates credentials and generates JWT token
 * 3. Server creates JwtAuthenticationResponse with token and user info
 * 4. Response is sent to client for storage and use
 * 5. Client includes token in Authorization header for future requests
 * 
 * Response Components:
 * - accessToken: JWT token for authentication
 * - tokenType: Token type identifier (Bearer)
 * - userInfo: Basic user information for client use
 * - expiresIn: Token expiration time in milliseconds
 * 
 * Security Features:
 * - JWT token contains user authentication data
 * - Token has configurable expiration time
 * - User information is limited to safe fields
 * - Token type clearly identifies authentication method
 * 
 * Client Usage:
 * - Store token securely (localStorage, sessionStorage, or secure cookie)
 * - Include in Authorization header: "Bearer <token>"
 * - Handle token expiration and refresh
 * - Use user information for UI display
 * 
 * @author Authentication Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class JwtAuthenticationResponse {
    
    /**
     * JWT access token for authentication
     * 
     * This field contains the JWT token that the client will use
     * for authentication in subsequent API requests. The token
     * contains user authentication data and has a configurable
     * expiration time.
     * 
     * Token Features:
     * - Contains user ID and authentication data
     * - Signed with HMAC-SHA512 algorithm
     * - Has configurable expiration time
     * - Stateless authentication support
     * 
     * Client Usage:
     * - Include in Authorization header: "Bearer <token>"
     * - Store securely in client application
     * - Handle token expiration gracefully
     * - Refresh token when needed
     * 
     * Security Considerations:
     * - Token should be stored securely
     * - Token has limited lifetime
     * - Token contains sensitive authentication data
     * - Token should not be logged or exposed
     */
    private String accessToken;
    
    /**
     * Token type identifier
     * 
     * This field identifies the type of authentication token being
     * used. It helps clients understand how to use the token and
     * provides context for the authentication method.
     * 
     * Token Type:
     * - "Bearer": Standard JWT token type
     * - Used in Authorization header format
     * - Indicates stateless authentication
     * - Standard for REST API authentication
     * 
     * Usage:
     * - Authorization: "Bearer <accessToken>"
     * - Helps clients format requests correctly
     * - Provides authentication context
     * - Standard across different clients
     */
    private String tokenType = "Bearer";
    
    /**
     * Basic user information for client use
     * 
     * This field contains basic user information that the client
     * can use for UI display and user context. It includes safe
     * user data that doesn't compromise security.
     * 
     * User Information:
     * - User ID for client-side operations
     * - Username for display purposes
     * - Email for user identification
     * - Name for UI display
     * - Roles for permission checking
     * 
     * Security Considerations:
     * - Only includes safe, non-sensitive data
     * - No password or security information
     * - Limited to display and identification purposes
     * - Can be used for UI personalization
     */
    private UserInfo userInfo;
    
    /**
     * Token expiration time in milliseconds
     * 
     * This field indicates when the JWT token will expire, allowing
     * clients to handle token refresh and expiration gracefully.
     * 
     * Expiration Handling:
     * - Clients can check expiration before requests
     * - Automatic token refresh can be implemented
     * - Graceful degradation when token expires
     * - User re-authentication when needed
     * 
     * Usage:
     * - Store expiration time for client-side checking
     * - Implement automatic token refresh
     * - Handle expired token scenarios
     * - Provide user feedback for authentication
     */
    private Long expiresIn;

    /**
     * Default constructor for JSON serialization
     * 
     * This constructor is required for JSON serialization when
     * the server sends the authentication response. It should not
     * be used directly in application code.
     */
    public JwtAuthenticationResponse() {
    }

    /**
     * Constructor with authentication token and user information
     * 
     * This constructor creates a JWT authentication response with
     * the access token and user information for client use.
     * 
     * @param accessToken JWT token for authentication
     * @param userInfo Basic user information for client
     * @param expiresIn Token expiration time in milliseconds
     */
    public JwtAuthenticationResponse(String accessToken, UserInfo userInfo, Long expiresIn) {
        this.accessToken = accessToken;
        this.userInfo = userInfo;
        this.expiresIn = expiresIn;
    }

    /**
     * Gets the JWT access token
     * 
     * @return JWT access token as String
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the JWT access token
     * 
     * @param accessToken JWT access token for authentication
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets the token type
     * 
     * @return Token type as String (typically "Bearer")
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the token type
     * 
     * @param tokenType Token type identifier
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * Gets the user information
     * 
     * @return UserInfo object containing basic user data
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Sets the user information
     * 
     * @param userInfo Basic user information for client use
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * Gets the token expiration time
     * 
     * @return Token expiration time in milliseconds
     */
    public Long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Sets the token expiration time
     * 
     * @param expiresIn Token expiration time in milliseconds
     */
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * User Information for Client Use
     * 
     * This inner class contains basic user information that is safe
     * to send to the client for UI display and user context purposes.
     * It excludes sensitive information like passwords and security data.
     * 
     * @author Authentication Team
     * @version 1.0.0
     * @since 2024-01-01
     */
    public static class UserInfo {
        
        /**
         * User ID for client-side operations
         * 
         * This field contains the unique identifier for the user
         * that can be used for client-side operations and API calls.
         */
        private Long id;
        
        /**
         * Username for display and identification
         * 
         * This field contains the username that the user uses for
         * authentication and can be displayed in the UI.
         */
        private String username;
        
        /**
         * User's email address
         * 
         * This field contains the user's email address for
         * identification and display purposes.
         */
        private String email;
        
        /**
         * User's full name for display
         * 
         * This field contains the user's full name as it should
         * appear in the user interface.
         */
        private String name;
        
        /**
         * User's roles for permission checking
         * 
         * This field contains the user's roles that determine
         * their permissions and access levels in the application.
         */
        private String[] roles;

        /**
         * Default constructor for JSON serialization
         */
        public UserInfo() {
        }

        /**
         * Constructor with user information
         * 
         * @param id User ID
         * @param username Username for authentication
         * @param email User's email address
         * @param name User's full name
         * @param roles User's roles in the system
         */
        public UserInfo(Long id, String username, String email, String name, String[] roles) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name;
            this.roles = roles;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String[] getRoles() { return roles; }
        public void setRoles(String[] roles) { this.roles = roles; }
    }
} 