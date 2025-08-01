package com.example.forecasting.controller;

import com.example.forecasting.model.User;
import com.example.forecasting.payload.ApiResponse;
import com.example.forecasting.payload.JwtAuthenticationResponse;
import com.example.forecasting.payload.LoginRequest;
import com.example.forecasting.payload.SignUpRequest;
import com.example.forecasting.service.UserService;
import com.example.forecasting.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

/**
 * Authentication Controller for Demand Forecasting System
 * 
 * This controller handles user authentication and registration operations
 * for the demand forecasting application. It provides secure endpoints
 * for user sign-in and sign-up with JWT token generation.
 * 
 * Endpoints:
 * - POST /api/auth/signin: User authentication and JWT token generation
 * - POST /api/auth/signup: User registration with default VIEWER role
 * 
 * Security Features:
 * - Password validation and encryption
 * - JWT token generation for authenticated users
 * - Input validation using @Valid annotation
 * - Duplicate username/email prevention
 * 
 * Business Rules:
 * - New users are assigned VIEWER role by default
 * - Passwords are encrypted using BCrypt
 * - Username and email must be unique
 * - JWT tokens are generated upon successful authentication
 * 
 * @author Authentication Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Constructor with dependency injection
     * 
     * @param authenticationManager Spring Security authentication manager
     * @param userService Service for user management operations
     * @param passwordEncoder Encoder for password hashing
     * @param tokenProvider Provider for JWT token operations
     */
    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         PasswordEncoder passwordEncoder,
                         JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Authenticates a user and generates a JWT token
     * 
     * This endpoint validates user credentials and generates a JWT token
     * for successful authentication. The token can be used for subsequent
     * API requests requiring authentication.
     * 
     * Authentication Process:
     * 1. Validates input credentials using @Valid annotation
     * 2. Authenticates user using Spring Security AuthenticationManager
     * 3. Sets authentication context in SecurityContextHolder
     * 4. Generates JWT token using JwtTokenProvider
     * 5. Returns token in JwtAuthenticationResponse
     * 
     * Security Considerations:
     * - Credentials are validated against encrypted passwords
     * - Authentication failures are handled gracefully
     * - JWT tokens have configurable expiration time
     * 
     * @param loginRequest Login credentials (username/email and password)
     * @return ResponseEntity with JWT token or error message
     * @throws AuthenticationException if credentials are invalid
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        // Build user info for response
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo();
        userInfo.setUsername(principal.getUsername());
        // You may want to fetch more user info from the database if needed
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, userInfo, null));
    }

    /**
     * Registers a new user in the system
     * 
     * This endpoint creates a new user account with the provided information.
     * New users are assigned the VIEWER role by default for security.
     * 
     * Registration Process:
     * 1. Validates input data using @Valid annotation
     * 2. Checks for duplicate username and email
     * 3. Encrypts password using BCrypt
     * 4. Creates user with VIEWER role
     * 5. Saves user to database
     * 6. Returns success response with user location
     * 
     * Business Rules:
     * - Username must be unique (3-20 characters)
     * - Email must be unique and valid format
     * - Password must be 6-40 characters
     * - First and last name are required
     * - Default role is VIEWER for security
     * 
     * Security Considerations:
     * - Passwords are encrypted before storage
     * - Input validation prevents injection attacks
     * - Duplicate prevention maintains data integrity
     * 
     * @param signUpRequest User registration information
     * @return ResponseEntity with success message or error details
     * @throws ValidationException if input validation fails
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        // Check for duplicate username
        if(userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        // Check for duplicate email
        if(userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email is already in use!"));
        }

        // Split name into first and last name if needed
        String firstName = signUpRequest.getName();
        String lastName = "";
        if (firstName != null && firstName.contains(" ")) {
            int idx = firstName.lastIndexOf(' ');
            lastName = firstName.substring(idx + 1);
            firstName = firstName.substring(0, idx);
        }

        // Creating user's account with encrypted password and default role
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .firstName(firstName)
                .lastName(lastName)
                .role(User.UserRole.VIEWER) // Default role for security
                .build();

        User result = userService.createUser(user);

        // Create URI for the newly created user
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }
} 