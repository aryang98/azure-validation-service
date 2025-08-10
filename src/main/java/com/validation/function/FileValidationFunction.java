package com.validation.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.validation.dto.ValidationRequest;
import com.validation.dto.ValidationResponse;
import com.validation.exception.FileNotFoundException;
import com.validation.exception.UnsupportedFileTypeException;
import com.validation.exception.ValidationException;
import com.validation.handler.ValidationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;

import java.util.Map;
import java.util.function.Function;

/**
 * Azure Function using Spring Cloud Function approach.
 * 
 * This function uses Spring Cloud Function Adapter for Azure, which provides:
 * - Better Spring Boot integration
 * - Easier testing capabilities
 * - Simplified function development
 * - Native Spring dependency injection
 * 
 * Function Details:
 * - HTTP Trigger: POST /api/validate
 * - Input: JSON request body with filename
 * - Output: JSON response with validation results
 * 
 * Request Flow:
 * 1. Parse and validate request body
 * 2. Delegate to ValidationHandler for business logic
 * 3. Return appropriate HTTP response with results
 * 4. Handle errors with proper HTTP status codes
 * 
 * Error Handling:
 * - 400 Bad Request: Invalid request format, validation errors, unsupported file types
 * - 404 Not Found: File not found in blob storage
 * - 500 Internal Server Error: Unexpected errors during processing
 */
@Component
public class FileValidationFunction implements Function<Map<String, Object>, ResponseEntity<Object>> {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationFunction.class);
    
    // Service dependencies
    private final ValidationHandler validationHandler;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param validationHandler Handler for validation business logic
     * @param objectMapper Jackson ObjectMapper for JSON processing
     */
    @Autowired
    public FileValidationFunction(ValidationHandler validationHandler, ObjectMapper objectMapper) {
        this.validationHandler = validationHandler;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Main function entry point for file validation.
     * 
     * This function handles HTTP POST requests to validate files stored in Azure Blob Storage.
     * It processes the request, delegates to the validation handler, and returns appropriate
     * HTTP responses based on the validation results.
     * 
     * Request Format:
     * ```json
     * {
     *   "filename": "sales_data.xlsx"
     * }
     * ```
     * 
     * Response Formats:
     * 
     * Success (no errors):
     * ```json
     * {
     *   "status": "SUCCESS",
     *   "message": "File validation completed successfully - no errors found",
     *   "totalRows": 100,
     *   "validRows": 100,
     *   "invalidRows": 0,
     *   "executionTimeMs": 1250,
     *   "processedAt": "2023-12-01T14:30:22"
     * }
     * ```
     * 
     * Success with errors:
     * ```json
     * {
     *   "status": "COMPLETED_WITH_ERRORS",
     *   "message": "File validation completed with errors. Download the corrected file using the provided URL.",
     *   "totalRows": 100,
     *   "validRows": 95,
     *   "invalidRows": 5,
     *   "downloadUrl": "https://storage.blob.core.windows.net/container/filename?sv=...",
     *   "executionTimeMs": 1850,
     *   "processedAt": "2023-12-01T14:30:22"
     * }
     * ```
     * 
     * Error:
     * ```json
     * {
     *   "errorCode": "FILE_NOT_FOUND",
     *   "message": "File not found: nonexistent_file.xlsx",
     *   "timestamp": "2023-12-01T14:30:22"
     * }
     * ```
     * 
     * @param request HTTP request containing the validation request
     * @return ResponseEntity with validation results or error details
     */
    @Override
    public ResponseEntity<Object> apply(Map<String, Object> request) {
        logger.info("File validation function triggered");
        
        try {
            // Step 1: Extract and validate request body
            if (request == null || !request.containsKey("filename")) {
                throw new ValidationException("Request body is required and must contain 'filename' field");
            }
            
            String filename = (String) request.get("filename");
            if (filename == null || filename.trim().isEmpty()) {
                throw new ValidationException("Filename is required");
            }
            
            // Step 2: Create ValidationRequest object
            ValidationRequest validationRequest = new ValidationRequest(filename);
            
            // Step 3: Process validation using the handler
            ValidationResponse response = validationHandler.handleValidationRequest(validationRequest);
            
            // Step 4: Return success response
            return ResponseEntity.ok(response);
                    
        } catch (ValidationException e) {
            // Handle validation errors (invalid request format, validation failures)
            logger.error("Validation error: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
            
        } catch (FileNotFoundException e) {
            // Handle file not found errors
            logger.error("File not found: {}", e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", e.getMessage());
            
        } catch (UnsupportedFileTypeException e) {
            // Handle unsupported file type errors
            logger.error("Unsupported file type: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_TYPE", e.getMessage());
            
        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error during file validation", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", 
                    "An unexpected error occurred while processing the request");
        }
    }
    
    /**
     * Creates a standardized error response with proper HTTP status code and error details.
     * 
     * This method ensures consistent error response format across all error scenarios.
     * It includes error code, message, and timestamp for proper error tracking and debugging.
     * 
     * @param status HTTP status code for the error
     * @param errorCode Application-specific error code
     * @param message Human-readable error message
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String errorCode, String message) {
        try {
            // Create standardized error response object
            ErrorResponse errorResponse = new ErrorResponse(errorCode, message);
            
            // Return HTTP response with error details
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            // Fallback error response if error response creation fails
            logger.error("Failed to create error response", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
    
    /**
     * Internal class for standardized error responses.
     * 
     * This class provides a consistent structure for error responses across
     * all error scenarios in the application.
     */
    private static class ErrorResponse {
        private final String errorCode;    // Application-specific error code
        private final String message;      // Human-readable error message
        private final String timestamp;    // When the error occurred
        
        /**
         * Constructor for error response.
         * 
         * @param errorCode Application-specific error code
         * @param message Human-readable error message
         */
        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
        
        /**
         * Gets the application-specific error code.
         * 
         * @return Error code string
         */
        public String getErrorCode() { 
            return errorCode; 
        }
        
        /**
         * Gets the human-readable error message.
         * 
         * @return Error message string
         */
        public String getMessage() { 
            return message; 
        }
        
        /**
         * Gets the timestamp when the error occurred.
         * 
         * @return Error timestamp string
         */
        public String getTimestamp() { 
            return timestamp; 
        }
    }
} 