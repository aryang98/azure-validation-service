package com.example.forecasting.config;

import com.example.forecasting.security.CustomUserDetailsService;
import com.example.forecasting.security.JwtAuthenticationEntryPoint;
import com.example.forecasting.security.JwtAuthenticationFilter;
import com.example.forecasting.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration for Demand Forecasting System
 * 
 * This configuration class sets up Spring Security for the demand forecasting
 * application, including authentication, authorization, JWT token handling,
 * and role-based access control for different user types.
 * 
 * Security Features:
 * - JWT-based stateless authentication
 * - Role-based authorization (ADMIN, ANALYST, VIEWER)
 * - Password encryption with BCrypt
 * - CORS configuration for frontend integration
 * - Session management (stateless)
 * - Comprehensive URL access control
 * 
 * Authentication Flow:
 * 1. User submits credentials to /api/auth/signin
 * 2. System validates credentials and generates JWT token
 * 3. Client includes JWT token in Authorization header
 * 4. JwtAuthenticationFilter validates token for each request
 * 5. UserPrincipal provides authentication context
 * 
 * Authorization Rules:
 * - Public endpoints: /api/auth/** (authentication endpoints)
 * - Admin endpoints: /api/admin/** (user management, configuration)
 * - Analyst endpoints: /api/analyst/** (data upload, ML operations)
 * - Viewer endpoints: /api/viewer/** (dashboard, reports)
 * - Template endpoints: /api/templates/** (template management)
 * - Estimation endpoints: /api/estimations/** (estimation operations)
 * 
 * @author Security Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    /**
     * Constructor with dependency injection
     * 
     * @param customUserDetailsService Service for loading user details
     * @param unauthorizedHandler Handler for unauthorized access attempts
     */
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    /**
     * Creates JWT authentication filter bean
     * 
     * This bean provides the JWT authentication filter that intercepts
     * all HTTP requests to validate JWT tokens and set authentication
     * context for authorized users.
     * 
     * Filter Features:
     * - Intercepts all HTTP requests
     * - Extracts and validates JWT tokens
     * - Sets authentication context
     * - Handles token parsing errors
     * 
     * @return JwtAuthenticationFilter bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider(), customUserDetailsService);
    }

    /**
     * Creates JWT token provider bean
     * 
     * This bean provides JWT token operations including generation,
     * validation, and parsing for authentication purposes.
     * 
     * @return JwtTokenProvider bean
     */
    @Bean
    public JwtTokenProvider tokenProvider() {
        return new JwtTokenProvider();
    }

    /**
     * Configures authentication manager with user details service
     * 
     * This method configures the authentication manager to use the
     * custom user details service for loading user information during
     * the authentication process.
     * 
     * Authentication Configuration:
     * - Uses CustomUserDetailsService for user loading
     * - Supports username/email authentication
     * - Integrates with password encoder
     * - Handles authentication failures
     * 
     * @param auth Authentication manager builder
     * @throws Exception if configuration fails
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * Creates authentication manager bean
     * 
     * This bean provides the authentication manager for handling
     * user authentication requests and managing authentication context.
     * 
     * @return AuthenticationManager bean
     * @throws Exception if bean creation fails
     */
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Creates password encoder bean
     * 
     * This bean provides BCrypt password encoding for secure password
     * storage and validation. BCrypt is a strong hashing algorithm
     * that includes salt generation for enhanced security.
     * 
     * Security Features:
     * - BCrypt algorithm with configurable strength
     * - Automatic salt generation
     * - Resistance to rainbow table attacks
     * - Configurable work factor for performance/security balance
     * 
     * @return BCryptPasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures HTTP security settings
     * 
     * This method configures comprehensive HTTP security including
     * authentication, authorization, CORS, session management, and
     * URL access control for the demand forecasting application.
     * 
     * Security Configuration:
     * - Stateless session management for JWT authentication
     * - CORS configuration for frontend integration
     * - Unauthorized access handling
     * - JWT authentication filter integration
     * - URL-based access control
     * 
     * URL Access Control:
     * - Public: Authentication endpoints (/api/auth/**)
     * - Admin: User management and configuration (/api/admin/**)
     * - Analyst: Data operations and ML (/api/analyst/**)
     * - Viewer: Dashboard and reports (/api/viewer/**)
     * - Templates: Template management (/api/templates/**)
     * - Estimations: Estimation operations (/api/estimations/**)
     * 
     * @param http HttpSecurity configuration object
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                    .and()
                .csrf()
                    .disable()
                .exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler)
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .authorizeRequests()
                    .antMatchers("/",
                            "/favicon.ico",
                            "/**/*.png",
                            "/**/*.gif",
                            "/**/*.svg",
                            "/**/*.jpg",
                            "/**/*.html",
                            "/**/*.css",
                            "/**/*.js")
                        .permitAll()
                    .antMatchers("/api/auth/**")
                        .permitAll()
                    .antMatchers("/api/admin/**")
                        .hasAnyRole("ADMIN")
                    .antMatchers("/api/analyst/**")
                        .hasAnyRole("ADMIN", "ANALYST")
                    .antMatchers("/api/viewer/**")
                        .hasAnyRole("ADMIN", "ANALYST", "VIEWER")
                    .antMatchers("/api/templates/**")
                        .hasAnyRole("ADMIN", "ANALYST")
                    .antMatchers("/api/estimations/**")
                        .hasAnyRole("ADMIN", "ANALYST")
                    .anyRequest()
                        .authenticated();

        // Add JWT authentication filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
} 