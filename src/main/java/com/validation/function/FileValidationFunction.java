package com.validation.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.validation.dto.ValidationRequest;
import com.validation.dto.ValidationResponse;
import com.validation.exception.FileNotFoundException;
import com.validation.exception.UnsupportedFileTypeException;
import com.validation.exception.ValidationException;
import com.validation.handler.ValidationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Azure Function for file validation service.
 * 
 * This is the main entry point for the file validation service. It handles HTTP requests
 * to validate Excel (.xlsx) and CSV files stored in Azure Blob Storage.
 * 
 * Function Details:
 * - HTTP Trigger: POST /api/validate
 * - Authorization: Function level (requires function key)
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
public class FileValidationFunction {
    
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
     * Main Azure Function entry point for file validation.
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
     * @param context Azure Functions execution context
     * @return HTTP response with validation results or error details
     */
    @FunctionName("validateFile")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "validate"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        logger.info("File validation function triggered");
        
        try {
            // Step 1: Extract and validate request body
            String requestBody = request.getBody().orElseThrow(() -> 
                new ValidationException("Request body is required"));
            
            // Step 2: Parse request body to ValidationRequest object
            ValidationRequest validationRequest = validationHandler.parseRequest(requestBody);
            
            // Step 3: Process validation using the handler
            ValidationResponse response = validationHandler.handleValidationRequest(validationRequest);
            
            // Step 4: Return success response
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
                    
        } catch (ValidationException e) {
            // Handle validation errors (invalid request format, validation failures)
            logger.error("Validation error: {}", e.getMessage());
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
            
        } catch (FileNotFoundException e) {
            // Handle file not found errors
            logger.error("File not found: {}", e.getMessage());
            return createErrorResponse(request, HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", e.getMessage());
            
        } catch (UnsupportedFileTypeException e) {
            // Handle unsupported file type errors
            logger.error("Unsupported file type: {}", e.getMessage());
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_TYPE", e.getMessage());
            
        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error during file validation", e);
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", 
                    "An unexpected error occurred while processing the request");
        }
    }
    
    /**
     * Creates a standardized error response with proper HTTP status code and error details.
     * 
     * This method ensures consistent error response format across all error scenarios.
     * It includes error code, message, and timestamp for proper error tracking and debugging.
     * 
     * @param request The original HTTP request
     * @param status HTTP status code for the error
     * @param errorCode Application-specific error code
     * @param message Human-readable error message
     * @return HTTP response with error details
     */
    private HttpResponseMessage createErrorResponse(HttpRequestMessage<Optional<String>> request, 
                                                  HttpStatus status, String errorCode, String message) {
        try {
            // Create standardized error response object
            ErrorResponse errorResponse = new ErrorResponse(errorCode, message);
            
            // Build HTTP response with error details
            return request.createResponseBuilder(status)
                    .header("Content-Type", "application/json")
                    .body(errorResponse)
                    .build();
        } catch (Exception e) {
            // Fallback error response if error response creation fails
            logger.error("Failed to create error response", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
                    .build();
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