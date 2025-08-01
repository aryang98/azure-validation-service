package com.example.forecasting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Demand Forecasting System
 * 
 * This application provides a comprehensive backend for AI/ML-driven demand forecasting
 * with role-based access control, template management, and estimation services.
 * 
 * Key Features:
 * - JWT-based authentication and authorization
 * - Role-based access control (ADMIN, ANALYST, VIEWER)
 * - Excel template management and processing
 * - Demand forecasting estimation services
 * - User management and system configuration
 * 
 * @author Demand Forecasting Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
public class DemandForecastingApplication {

    /**
     * Main application entry point
     * 
     * Starts the Spring Boot application with all configured components:
     * - Security configuration with JWT authentication
     * - JPA/Hibernate for database operations
     * - REST API controllers for all business operations
     * - Excel processing utilities
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(DemandForecastingApplication.class, args);
    }
} 