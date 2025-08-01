package com.example.filevalidation.exception;

/**
 * Invalid File Format Exception
 * 
 * This exception is thrown when the uploaded file format is not supported
 * or when the file structure is invalid for processing.
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class InvalidFileFormatException extends FileValidationException {

    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public InvalidFileFormatException(String message) {
        super(message, "INVALID_FILE_FORMAT");
    }

    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Root cause of the exception
     */
    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause, "INVALID_FILE_FORMAT");
    }

    /**
     * Constructor for unsupported file format
     * 
     * @param filename Name of the file
     * @param supportedFormats List of supported formats
     */
    public InvalidFileFormatException(String filename, String supportedFormats) {
        super(String.format("File '%s' has an unsupported format. Supported formats: %s", 
                          filename, supportedFormats), "INVALID_FILE_FORMAT");
    }
} 