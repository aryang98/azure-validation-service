package com.example.filevalidation.exception;

/**
 * File Validation Exception
 * 
 * This is the main exception class for file validation errors.
 * It provides a centralized way to handle validation-related exceptions
 * with proper error messages and context information.
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class FileValidationException extends RuntimeException {

    /**
     * Error code for the exception
     */
    private final String errorCode;

    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public FileValidationException(String message) {
        super(message);
        this.errorCode = "FILE_VALIDATION_ERROR";
    }

    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Root cause of the exception
     */
    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FILE_VALIDATION_ERROR";
    }

    /**
     * Constructor with message and error code
     * 
     * @param message Error message
     * @param errorCode Specific error code
     */
    public FileValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message, cause, and error code
     * 
     * @param message Error message
     * @param cause Root cause of the exception
     * @param errorCode Specific error code
     */
    public FileValidationException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Get the error code
     * 
     * @return Error code
     */
    public String getErrorCode() {
        return errorCode;
    }
} 