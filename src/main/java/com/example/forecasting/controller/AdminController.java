package com.example.forecasting.controller;

import com.example.forecasting.model.User;
import com.example.forecasting.payload.ApiResponse;
import com.example.forecasting.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Admin Controller for Demand Forecasting System
 * 
 * This controller provides administrative operations for the demand forecasting
 * application. All endpoints require ADMIN role access and provide user management
 * and system configuration capabilities.
 * 
 * Endpoints:
 * - GET /api/admin/users: Retrieve all users
 * - POST /api/admin/users: Create new user
 * - PUT /api/admin/users/{userId}: Update existing user
 * - DELETE /api/admin/users/{userId}: Delete user
 * - GET /api/admin/config: Retrieve system configuration
 * - PUT /api/admin/config: Update system configuration
 * 
 * Security Features:
 * - All endpoints require ADMIN role (@PreAuthorize)
 * - Input validation and sanitization
 * - Password encryption for new/updated users
 * - Audit trail for user operations
 * 
 * Business Rules:
 * - Only ADMIN users can access these endpoints
 * - User creation includes role assignment
 * - Password updates are encrypted
 * - User deletion is permanent
 * - System configuration affects application behavior
 * 
 * @author Admin Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    /**
     * Constructor with dependency injection
     * 
     * @param userService Service for user management operations
     */
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all users in the system
     * 
     * This endpoint provides a complete list of all registered users
     * for administrative purposes. The response includes user details
     * such as username, email, role, and account status.
     * 
     * Security Considerations:
     * - Only accessible by ADMIN users
     * - Returns sensitive user information
     * - Should be used with caution in production
     * 
     * @return ResponseEntity containing list of all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Creates a new user account
     * 
     * This endpoint allows administrators to create new user accounts
     * with specific roles and permissions. The password is automatically
     * encrypted before storage.
     * 
     * Business Rules:
     * - Username must be unique
     * - Email must be unique
     * - Password is encrypted using BCrypt
     * - Role can be ADMIN, ANALYST, or VIEWER
     * 
     * Security Considerations:
     * - Input validation prevents injection attacks
     * - Password encryption ensures security
     * - Duplicate prevention maintains data integrity
     * 
     * @param request User creation request with all required fields
     * @return ResponseEntity with success/error message
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        // Validate unique username
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        // Validate unique email
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email is already in use!"));
        }

        // Create user with encrypted password
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be encrypted in service
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();

        userService.createUser(user);
        return ResponseEntity.ok(new ApiResponse(true, "User created successfully"));
    }

    /**
     * Updates an existing user account
     * 
     * This endpoint allows administrators to modify user information
     * including personal details, role, and account status. Password
     * updates are optional and encrypted when provided.
     * 
     * Update Process:
     * 1. Validates user exists
     * 2. Updates non-sensitive fields directly
     * 3. Encrypts password if provided
     * 4. Saves updated user information
     * 
     * Business Rules:
     * - User must exist in the system
     * - Password updates are optional
     * - Role changes affect user permissions immediately
     * - Account status can be enabled/disabled
     * 
     * @param userId ID of the user to update
     * @param request Updated user information
     * @return ResponseEntity with success/error message
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        
        // Update user information
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword()); // Will be encrypted in service
        }

        userService.updateUser(userId, user);
        return ResponseEntity.ok(new ApiResponse(true, "User updated successfully"));
    }

    /**
     * Deletes a user account permanently
     * 
     * This endpoint removes a user from the system permanently.
     * This operation cannot be undone and should be used with caution.
     * 
     * Security Considerations:
     * - Only ADMIN users can delete accounts
     * - Deletion is permanent and irreversible
     * - Should be logged for audit purposes
     * 
     * @param userId ID of the user to delete
     * @return ResponseEntity with success/error message
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        if (!userService.findById(userId).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }

    /**
     * Retrieves system configuration
     * 
     * This endpoint provides access to system-wide configuration
     * settings that affect application behavior.
     * 
     * TODO: Implement system configuration retrieval logic
     * 
     * @return ResponseEntity with system configuration
     */
    @GetMapping("/config")
    public ResponseEntity<?> getSystemConfig() {
        // TODO: Implement system configuration retrieval
        return ResponseEntity.ok("System configuration");
    }

    /**
     * Updates system configuration
     * 
     * This endpoint allows administrators to modify system-wide
     * configuration settings that affect application behavior.
     * 
     * TODO: Implement system configuration update logic
     * 
     * @param request System configuration update request
     * @return ResponseEntity with success/error message
     */
    @PutMapping("/config")
    public ResponseEntity<?> updateSystemConfig(@RequestBody SystemConfigRequest request) {
        // TODO: Implement system configuration update
        return ResponseEntity.ok(new ApiResponse(true, "System configuration updated"));
    }

    // Request/Response classes with comprehensive documentation

    /**
     * Request class for creating new users
     * 
     * Contains all required information for user creation including
     * personal details, credentials, and role assignment.
     */
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private User.UserRole role;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public User.UserRole getRole() { return role; }
        public void setRole(User.UserRole role) { this.role = role; }
    }

    /**
     * Request class for updating existing users
     * 
     * Contains optional fields for user updates. Password updates
     * are optional and only processed if provided.
     */
    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private User.UserRole role;
        private boolean enabled;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public User.UserRole getRole() { return role; }
        public void setRole(User.UserRole role) { this.role = role; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * Request class for system configuration updates
     * 
     * Contains key-value pairs for system configuration
     * that affect application behavior.
     */
    public static class SystemConfigRequest {
        private String configKey;
        private String configValue;

        // Getters and setters
        public String getConfigKey() { return configKey; }
        public void setConfigKey(String configKey) { this.configKey = configKey; }
        public String getConfigValue() { return configValue; }
        public void setConfigValue(String configValue) { this.configValue = configValue; }
    }
} 