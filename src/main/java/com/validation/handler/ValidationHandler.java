package com.validation.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.validation.dto.ValidationRequest;
import com.validation.dto.ValidationResponse;
import com.validation.exception.ValidationException;
import com.validation.service.FileValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handler layer that acts as an adapter between the Azure Function and the service layer.
 * 
 * This handler provides:
 * - Request validation and parsing
 * - Business logic coordination
 * - Error handling and transformation
 * - Logging and monitoring
 */
@Component
public class ValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationHandler.class);
    
    private final FileValidationService fileValidationService;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param fileValidationService Service for file validation operations
     * @param validator Bean validation validator
     * @param objectMapper Jackson ObjectMapper for JSON processing
     */
    @Autowired
    public ValidationHandler(FileValidationService fileValidationService, 
                           Validator validator, 
                           ObjectMapper objectMapper) {
        this.fileValidationService = fileValidationService;
        this.validator = validator;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Handles validation requests by coordinating between the function and service layers.
     * 
     * This method:
     * 1. Validates the request using Bean Validation
     * 2. Checks if the file type is supported
     * 3. Delegates to the service layer for processing
     * 4. Returns the validation response
     * 
     * @param request The validation request to process
     * @return ValidationResponse with results
     * @throws ValidationException if validation fails
     */
    public ValidationResponse handleValidationRequest(ValidationRequest request) {
        logger.info("Processing validation request for file: {}", request.getFilename());
        
        // Validate request using Bean Validation
        Set<ConstraintViolation<ValidationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Request validation failed: " + errorMessage);
        }
        
        // Check if file is supported
        if (!fileValidationService.isFileSupported(request.getFilename())) {
            throw new ValidationException("Unsupported file type. Only .xlsx and .csv files are supported.");
        }
        
        // Process validation
        ValidationResponse response = fileValidationService.validateFile(request);
        
        logger.info("Validation completed for file: {}. Total: {}, Valid: {}, Invalid: {}", 
                request.getFilename(), response.getTotalRows(), response.getValidRows(), response.getInvalidRows());
        
        return response;
    }
    
    /**
     * Parses JSON request body into ValidationRequest object.
     * 
     * @param requestBody JSON string containing the request
     * @return Parsed ValidationRequest object
     * @throws ValidationException if parsing fails
     */
    public ValidationRequest parseRequest(String requestBody) {
        try {
            return objectMapper.readValue(requestBody, ValidationRequest.class);
        } catch (Exception e) {
            logger.error("Failed to parse request body", e);
            throw new ValidationException("Invalid request format: " + e.getMessage());
        }
    }
} 