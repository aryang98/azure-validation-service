package com.example.filevalidation.function;

import com.example.filevalidation.model.ValidationResult;
import com.example.filevalidation.service.FileProcessingService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions HTTP Trigger for File Validation
 * 
 * This class provides the Azure Functions HTTP trigger endpoint for file validation.
 * It handles file uploads, processes them through the validation workflow, and returns
 * comprehensive validation results.
 * 
 * Features:
 * - HTTP POST endpoint for file uploads
 * - Multipart file handling
 * - Comprehensive error handling
 * - JSON response formatting
 * - Detailed logging
 * 
 * Endpoints:
 * - POST /api/validate-file: Upload and validate Excel files
 * - GET /api/health: Health check endpoint
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@Slf4j
public class FileValidationFunction {

    @Autowired
    private FileProcessingService fileProcessingService;

    /**
     * HTTP trigger for file validation
     * 
     * This function handles file uploads and validation requests.
     * It accepts multipart form data containing Excel files and returns
     * comprehensive validation results.
     * 
     * @param request HTTP request containing the file to validate
     * @param context Azure Functions execution context
     * @return HTTP response with validation results
     */
    @FunctionName("validateFile")
    public HttpResponseMessage validateFile(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/validate-file"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        log.info("File validation request received. Request ID: {}", context.getInvocationId());

        try {
            // Extract file from multipart request
            MultipartFile uploadedFile = extractFileFromRequest(request);
            
            if (uploadedFile == null || uploadedFile.isEmpty()) {
                log.warn("No file provided in request");
                return createErrorResponse(request, HttpStatus.BAD_REQUEST, 
                    "No file provided in request");
            }

            log.info("Processing file: {} ({} bytes)", 
                    uploadedFile.getOriginalFilename(), uploadedFile.getSize());

            // Process and validate the file
            ValidationResult result = fileProcessingService.processFile(uploadedFile);

            // Create success response
            Map<String, Object> responseBody = createSuccessResponse(result);
            
            log.info("File validation completed successfully. Rows processed: {}, Rows with errors: {}", 
                    result.getRowsProcessed(), result.getRowsWithErrors());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();

        } catch (Exception e) {
            log.error("Error processing file validation request: {}", e.getMessage(), e);
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error processing file: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     * 
     * @param request HTTP request
     * @param context Azure Functions execution context
     * @return HTTP response with health status
     */
    @FunctionName("healthCheck")
    public HttpResponseMessage healthCheck(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/health"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        log.info("Health check request received. Request ID: {}", context.getInvocationId());

        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "healthy");
        healthResponse.put("timestamp", System.currentTimeMillis());
        healthResponse.put("service", "file-validation-service");
        healthResponse.put("version", "1.0.0");

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(healthResponse)
                .build();
    }

    /**
     * Statistics endpoint
     * 
     * @param request HTTP request
     * @param context Azure Functions execution context
     * @return HTTP response with processing statistics
     */
    @FunctionName("getStatistics")
    public HttpResponseMessage getStatistics(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/statistics"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        log.info("Statistics request received. Request ID: {}", context.getInvocationId());

        try {
            FileProcessingService.ProcessingStatistics stats = fileProcessingService.getProcessingStatistics();
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("totalFilesProcessed", stats.getTotalFilesProcessed());
            responseBody.put("filesWithErrors", stats.getFilesWithErrors());
            responseBody.put("filesSuccessfullyProcessed", stats.getFilesSuccessfullyProcessed());
            responseBody.put("totalRowsProcessed", stats.getTotalRowsProcessed());
            responseBody.put("totalRowsWithErrors", stats.getTotalRowsWithErrors());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving statistics: {}", e.getMessage(), e);
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving statistics: " + e.getMessage());
        }
    }

    /**
     * Extracts file from multipart HTTP request
     * 
     * @param request HTTP request
     * @return MultipartFile if found, null otherwise
     */
    private MultipartFile extractFileFromRequest(HttpRequestMessage<Optional<String>> request) {
        try {
            // This is a simplified implementation
            // In a real Azure Functions environment, you would need to handle multipart parsing differently
            // For now, we'll return null and handle the actual implementation based on the environment
            
            log.debug("Extracting file from request");
            // TODO: Implement proper multipart file extraction for Azure Functions
            return null;
            
        } catch (Exception e) {
            log.error("Error extracting file from request: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a success response with validation results
     * 
     * @param result Validation result
     * @return Map containing response data
     */
    private Map<String, Object> createSuccessResponse(ValidationResult result) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", result.isSuccess());
        responseBody.put("status", result.getStatus().toString());
        responseBody.put("filename", result.getFilename());
        responseBody.put("rowsProcessed", result.getRowsProcessed());
        responseBody.put("rowsWithErrors", result.getRowsWithErrors());
        responseBody.put("downloadUrl", result.getDownloadUrl());
        responseBody.put("errorFileUrl", result.getErrorFileUrl());
        
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            responseBody.put("errors", result.getErrors());
        }
        
        return responseBody;
    }

    /**
     * Creates an error response
     * 
     * @param request HTTP request
     * @param status HTTP status code
     * @param message Error message
     * @return HTTP response with error details
     */
    private HttpResponseMessage createErrorResponse(HttpRequestMessage<Optional<String>> request, 
                                                 HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(errorResponse)
                .build();
    }
} 