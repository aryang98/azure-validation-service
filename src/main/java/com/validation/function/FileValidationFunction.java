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

@Component
public class FileValidationFunction {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationFunction.class);
    
    private final ValidationHandler validationHandler;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public FileValidationFunction(ValidationHandler validationHandler, ObjectMapper objectMapper) {
        this.validationHandler = validationHandler;
        this.objectMapper = objectMapper;
    }
    
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
            // Parse request body
            String requestBody = request.getBody().orElseThrow(() -> 
                new ValidationException("Request body is required"));
            
            ValidationRequest validationRequest = validationHandler.parseRequest(requestBody);
            
            // Process validation
            ValidationResponse response = validationHandler.handleValidationRequest(validationRequest);
            
            // Return success response
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
                    
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