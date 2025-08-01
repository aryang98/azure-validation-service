package com.example.filevalidation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Application Class for File Validation Service
 * 
 * This Spring Boot application provides file validation functionality
 * with Azure Functions HTTP trigger, Azure Blob Storage for file storage,
 * and Azure SQL Database for metadata storage.
 * 
 * Features:
 * - Excel file validation with row-level error tracking
 * - Azure Blob Storage integration for file storage
 * - Azure SQL Database for metadata persistence
 * - Comprehensive logging and error handling
 * - Modular architecture with proper separation of concerns
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.filevalidation")
public class FileValidationApplication {

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FileValidationApplication.class, args);
    }
} 