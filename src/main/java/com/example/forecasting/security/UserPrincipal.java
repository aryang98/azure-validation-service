package com.example.forecasting.security;

import com.example.forecasting.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * User Principal for Demand Forecasting System
 * 
 * This class implements Spring Security's UserDetails interface to provide
 * user authentication and authorization information for the demand forecasting
 * application. It serves as a bridge between the User entity and Spring Security.
 * 
 * Spring Security Integration:
 * - Implements UserDetails interface for authentication
 * - Provides user authorities (roles) for authorization
 * - Supports account status validation (enabled/disabled)
 * - Integrates with JWT token generation and validation
 * 
 * User Information:
 * - User ID, username, email, and password
 * - User roles converted to Spring Security authorities
 * - Account status and validation flags
 * - JSON serialization support for API responses
 * 
 * Security Features:
 * - Password is marked as ignored in JSON serialization
 * - Account status validation for authentication
 * - Role-based authorization support
 * - Comprehensive equals and hashCode methods
 * 
 * @author Security Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String name;
    private String username;
    private String email;
    
    @JsonIgnore
    private String password;
    
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor with all user details
     * 
     * @param id User ID from database
     * @param name User's full name
     * @param username Unique username for authentication
     * @param email User's email address
     * @param password Encrypted password (ignored in JSON)
     * @param authorities Spring Security authorities (roles)
     */
    public UserPrincipal(Long id, String name, String username, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Creates UserPrincipal from User entity
     * 
     * This static method converts a User entity to a UserPrincipal object
     * for Spring Security authentication. It extracts user information and
     * converts user roles to Spring Security authorities.
     * 
     * Role Conversion:
     * - User roles are converted to SimpleGrantedAuthority objects
     * - Role names are prefixed with "ROLE_" for Spring Security
     * - Supports multiple roles per user
     * 
     * @param user User entity from database
     * @return UserPrincipal object for Spring Security
     */
    public static UserPrincipal create(User user) {
        // Convert user role to Spring Security authority
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        String fullName = user.getFirstName() + " " + user.getLastName();
        return new UserPrincipal(
                user.getId(),
                fullName,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * Returns the user ID
     * 
     * @return User ID as Long
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the user's full name
     * 
     * @return User's full name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the user's email address
     * 
     * @return User's email as String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the user's authorities (roles)
     * 
     * This method provides the user's roles as Spring Security authorities
     * for authorization decisions. Authorities are used to determine
     * access to protected resources and endpoints.
     * 
     * @return Collection of GrantedAuthority objects representing user roles
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the user's password
     * 
     * This method returns the encrypted password for authentication.
     * The password is marked with @JsonIgnore to prevent serialization
     * in API responses for security reasons.
     * 
     * @return Encrypted password as String
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's username
     * 
     * This method provides the username for authentication purposes.
     * The username is used for login and token generation.
     * 
     * @return Username as String
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Checks if the user account is not expired
     * 
     * This method always returns true as account expiration is not
     * implemented in the current system. Future implementations may
     * add account expiration functionality.
     * 
     * @return true (account is not expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Checks if the user account is not locked
     * 
     * This method always returns true as account locking is not
     * implemented in the current system. Future implementations may
     * add account locking functionality for security purposes.
     * 
     * @return true (account is not locked)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Checks if the user's credentials are not expired
     * 
     * This method always returns true as credential expiration is not
     * implemented in the current system. Future implementations may
     * add password expiration functionality.
     * 
     * @return true (credentials are not expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Checks if the user account is enabled
     * 
     * This method always returns true as account enabling/disabling
     * is not implemented in the current system. Future implementations
     * may add account status management.
     * 
     * @return true (account is enabled)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Checks equality with another object
     * 
     * Two UserPrincipal objects are considered equal if they have
     * the same user ID. This is used for authentication context
     * management and caching.
     * 
     * @param o Object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Generates hash code for the object
     * 
     * The hash code is based on the user ID to ensure consistency
     * with the equals method for proper object comparison.
     * 
     * @return Hash code as int
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 