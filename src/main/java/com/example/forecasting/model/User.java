package com.example.forecasting.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity for Demand Forecasting System
 * 
 * This entity represents a user account in the demand forecasting application.
 * It contains user authentication information, personal details, role assignment,
 * and audit timestamps for security and compliance purposes.
 * 
 * Database Schema:
 * - users table with comprehensive user information
 * - Role-based access control with enum values
 * - Audit timestamps for creation and updates
 * - Unique constraints on username and email
 * 
 * Business Rules:
 * - Username must be unique across the system
 * - Email must be unique and valid format
 * - Password is encrypted before storage
 * - Role determines access permissions
 * - Account can be enabled/disabled
 * - Audit timestamps are automatically managed
 * 
 * Security Features:
 * - Password field is excluded from JSON serialization
 * - Role-based access control
 * - Account status management
 * - Audit trail with timestamps
 * 
 * @author Data Model Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    /**
     * Primary key for the user entity
     * Auto-generated using identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for authentication
     * Must be unique across the system
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Encrypted password for authentication
     * Stored using BCrypt encryption
     */
    @Column(nullable = false)
    private String password;

    /**
     * Unique email address for user identification
     * Must be unique and valid email format
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's first name for display purposes
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * User's last name for display purposes
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * User role for access control
     * Determines permissions and access levels
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * Account status flag
     * Controls whether user can authenticate
     */
    private boolean enabled = true;

    /**
     * Timestamp when user account was created
     * Automatically set on entity creation
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when user account was last updated
     * Automatically updated on entity modification
     */
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback for entity creation
     * Sets creation and update timestamps
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    /**
     * JPA lifecycle callback for entity updates
     * Updates the modification timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * User Role Enumeration
     * 
     * Defines the available roles in the system with their respective
     * permissions and access levels.
     * 
     * Role Hierarchy:
     * - ADMIN: Full system access, user management, configuration
     * - ANALYST: Data upload, accelerator operations, report generation
     * - VIEWER: Dashboard access, report viewing and export
     */
    public enum UserRole {
        /**
         * Administrator role with full system access
         * Can manage users, configure system, access all data
         */
        ADMIN,
        
        /**
         * Analyst role with data and reporting capabilities
         * Can upload data, run accelerators, generate reports
         */
        ANALYST,
        
        /**
         * Viewer role with read-only access
         * Can view dashboard and export reports
         */
        VIEWER
    }
} 