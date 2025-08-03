package com.example.filevalidation.function;

import com.example.filevalidation.dto.ValidationRequest;
import com.example.filevalidation.dto.ValidationResponse;
import com.example.filevalidation.service.ValidationService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Azure Function HTTP trigger for file validation
 * Provides REST endpoint for validating Excel files stored in Azure Blob Storage
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileValidationFunction {
    
    private final ValidationService validationService;
    
    /**
     * HTTP trigger function for file validation
     * 
     * @param request HTTP request containing validation parameters
     * @param context Execution context
     * @return HTTP response with validation results
     */
    @FunctionName("validateFile")
    public HttpResponseMessage validateFile(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "validate"
            ) HttpRequestMessage<Optional<ValidationRequest>> request,
            final ExecutionContext context) {
        
        try {
            log.info("File validation request received");
            
            // Extract request body
            Optional<ValidationRequest> requestBody = request.getBody();
            if (requestBody.isEmpty()) {
                log.error("Request body is empty");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(ValidationResponse.failure("Request body is required"))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            ValidationRequest validationRequest = requestBody.get();
            
            // Validate request
            if (validationRequest.getFileMetadataId() == null) {
                log.error("File metadata ID is required");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(ValidationResponse.failure("File metadata ID is required"))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            log.info("Starting validation for file metadata ID: {}", validationRequest.getFileMetadataId());
            
            // Perform validation
            ValidationResponse response = validationService.validateFile(validationRequest.getFileMetadataId());
            
            log.info("Validation completed successfully. Status: {}", response.getStatus());
            
            // Return success response
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(response)
                    .header("Content-Type", "application/json")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during file validation", e);
            
            // Return error response
            ValidationResponse errorResponse = ValidationResponse.failure(
                "Validation failed: " + e.getMessage());
            
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse)
                    .header("Content-Type", "application/json")
                    .build();
        }
    }
    
    /**
     * Health check endpoint
     * 
     * @param request HTTP request
     * @param context Execution context
     * @return HTTP response with health status
     */
    @FunctionName("health")
    public HttpResponseMessage health(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "health"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        log.info("Health check request received");
        
        return request.createResponseBuilder(HttpStatus.OK)
                .body("File Validation Service is running")
                .header("Content-Type", "text/plain")
                .build();
    }
} 