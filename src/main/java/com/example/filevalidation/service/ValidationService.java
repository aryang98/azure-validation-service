package com.example.filevalidation.service;

import com.example.filevalidation.model.ValidationResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Validation Service Interface
 * 
 * This interface defines the contract for file validation operations.
 * It provides methods for validating Excel files with various data types
 * including email, date, name, and ID validation.
 * 
 * Business Operations:
 * - Excel file validation with row-level error tracking
 * - Data type validation (email, date, name, ID)
 * - Error reporting with detailed information
 * - Optional column handling
 * 
 * Validation Rules:
 * - Email: Must be a valid email format
 * - Date: Must be a valid date format
 * - Name: Must contain only letters and spaces
 * - ID: Must be alphanumeric
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface ValidationService {

    /**
     * Validates an uploaded Excel file
     * 
     * This method processes the uploaded file, validates each row according to
     * the specified validation rules, and returns a comprehensive validation result.
     * 
     * @param file The uploaded Excel file to validate
     * @return ValidationResult containing validation status and error details
     * @throws com.example.filevalidation.exception.FileValidationException if validation fails
     */
    ValidationResult validateFile(MultipartFile file);

    /**
     * Validates a specific column in the Excel file
     * 
     * @param columnName Name of the column to validate
     * @param columnData List of values in the column
     * @param validationType Type of validation to perform (EMAIL, DATE, NAME, ID)
     * @return List of validation errors for the column
     */
    List<ValidationResult.ValidationError> validateColumn(String columnName, 
                                                        List<String> columnData, 
                                                        ValidationType validationType);

    /**
     * Validates an email address
     * 
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    boolean isValidEmail(String email);

    /**
     * Validates a date string
     * 
     * @param date Date string to validate
     * @param format Expected date format (e.g., "yyyy-MM-dd")
     * @return true if valid, false otherwise
     */
    boolean isValidDate(String date, String format);

    /**
     * Validates a name string
     * 
     * @param name Name string to validate
     * @return true if valid, false otherwise
     */
    boolean isValidName(String name);

    /**
     * Validates an ID string
     * 
     * @param id ID string to validate
     * @return true if valid, false otherwise
     */
    boolean isValidId(String id);

    /**
     * Gets the list of required columns for validation
     * 
     * @return List of required column names
     */
    List<String> getRequiredColumns();

    /**
     * Gets the list of optional columns (not validated)
     * 
     * @return List of optional column names
     */
    List<String> getOptionalColumns();

    /**
     * Validation Type Enumeration
     */
    enum ValidationType {
        EMAIL,
        DATE,
        NAME,
        ID,
        OPTIONAL
    }
} 