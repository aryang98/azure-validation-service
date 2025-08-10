package com.validation.function;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Azure Function handler that bridges Spring Cloud Function with Azure Functions runtime.
 * 
 * This class acts as an adapter between Azure Functions and Spring Cloud Function.
 * It handles the Azure Functions specific annotations and delegates to the Spring Cloud Function.
 * 
 * The handler:
 * - Receives Azure Functions HTTP requests
 * - Converts them to Spring Cloud Function format
 * - Delegates to FileValidationFunction
 * - Converts Spring responses back to Azure Functions format
 */
@Component
public class FileValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationHandler.class);
    
    private final FileValidationFunction fileValidationFunction;
    private final ValidationHandler validationHandler;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param fileValidationFunction The Spring Cloud Function for file validation
     * @param validationHandler Handler for validation business logic
     */
    @Autowired
    public FileValidationHandler(FileValidationFunction fileValidationFunction, ValidationHandler validationHandler) {
        this.fileValidationFunction = fileValidationFunction;
        this.validationHandler = validationHandler;
    }
    
    /**
     * Azure Function entry point for file validation.
     * 
     * This method handles the Azure Functions HTTP trigger and delegates to the Spring Cloud Function.
     * It provides the bridge between Azure Functions runtime and Spring Cloud Function.
     * 
     * @param request Azure Functions HTTP request
     * @param context Azure Functions execution context
     * @return Azure Functions HTTP response
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
        
        logger.info("Azure Function triggered for file validation");
        
        try {
            // Step 1: Extract request body
            String requestBody = request.getBody().orElseThrow(() -> 
                new ValidationException("Request body is required"));
            
            // Step 2: Parse request body to get filename
            ValidationRequest validationRequest = validationHandler.parseRequest(requestBody);
            
            // Step 3: Convert to Spring Cloud Function format
            Map<String, Object> springRequest = Map.of("filename", validationRequest.getFilename());
            
            // Step 4: Delegate to Spring Cloud Function
            ResponseEntity<Object> springResponse = fileValidationFunction.apply(springRequest);
            
            // Step 5: Convert Spring response to Azure Functions response
            return createAzureResponse(request, springResponse);
            
        } catch (ValidationException e) {
            logger.error("Validation error: {}", e.getMessage());
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
            
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", e.getMessage());
            return createErrorResponse(request, HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", e.getMessage());
            
        } catch (UnsupportedFileTypeException e) {
            logger.error("Unsupported file type: {}", e.getMessage());
            return createErrorResponse(request, HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_TYPE", e.getMessage());
            
        } catch (Exception e) {
            logger.error("Unexpected error during file validation", e);
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", 
                    "An unexpected error occurred while processing the request");
        }
    }
    
    /**
     * Converts Spring ResponseEntity to Azure Functions HttpResponseMessage.
     * 
     * @param azureRequest The original Azure Functions request
     * @param springResponse The Spring ResponseEntity
     * @return Azure Functions HttpResponseMessage
     */
    private HttpResponseMessage createAzureResponse(HttpRequestMessage<Optional<String>> azureRequest, 
                                                   ResponseEntity<Object> springResponse) {
        return azureRequest.createResponseBuilder(springResponse.getStatusCode())
                .header("Content-Type", "application/json")
                .body(springResponse.getBody())
                .build();
    }
    
    /**
     * Creates a standardized error response for Azure Functions.
     * 
     * @param request The Azure Functions request
     * @param status HTTP status code
     * @param errorCode Application-specific error code
     * @param message Error message
     * @return Azure Functions error response
     */
    private HttpResponseMessage createErrorResponse(HttpRequestMessage<Optional<String>> request, 
                                                   HttpStatus status, String errorCode, String message) {
        try {
            ErrorResponse errorResponse = new ErrorResponse(errorCode, message);
            return request.createResponseBuilder(status)
                    .header("Content-Type", "application/json")
                    .body(errorResponse)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create error response", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
                    .build();
        }
    }
    
    /**
     * Internal class for standardized error responses.
     */
    private static class ErrorResponse {
        private final String errorCode;
        private final String message;
        private final String timestamp;
        
        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
        
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }
} 