package com.example.forecasting.service;

import com.example.forecasting.model.User;
import java.util.List;
import java.util.Optional;

/**
 * User Service Interface for Demand Forecasting System
 * 
 * This interface defines the contract for user management operations
 * including user creation, updates, deletion, and retrieval.
 * 
 * Business Operations:
 * - User account management (CRUD operations)
 * - User authentication and authorization
 * - Role management and permissions
 * - Account status management
 * 
 * @author User Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserService {
    
    /**
     * Retrieves all users in the system
     * 
     * @return List of all users
     */
    List<User> getAllUsers();
    
    /**
     * Finds a user by their ID
     * 
     * @param userId User ID to search for
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long userId);
    
    /**
     * Finds a user by their username
     * 
     * @param username Username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by their email address
     * 
     * @param email Email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Creates a new user account
     * 
     * @param user User object to create
     * @return Created user with generated ID
     */
    User createUser(User user);
    
    /**
     * Updates an existing user account
     * 
     * @param userId ID of the user to update
     * @param user Updated user information
     * @return Updated user object
     */
    User updateUser(Long userId, User user);
    
    /**
     * Deletes a user account
     * 
     * @param userId ID of the user to delete
     */
    void deleteUser(Long userId);
    
    /**
     * Checks if a username exists in the system
     * 
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Checks if an email address exists in the system
     * 
     * @param email Email address to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
} 