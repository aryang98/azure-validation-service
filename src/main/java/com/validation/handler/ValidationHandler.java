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

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationHandler.class);
    
    private final FileValidationService fileValidationService;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public ValidationHandler(FileValidationService fileValidationService, 
                           Validator validator, 
                           ObjectMapper objectMapper) {
        this.fileValidationService = fileValidationService;
        this.validator = validator;
        this.objectMapper = objectMapper;
    }
    
    public ValidationResponse handleValidationRequest(ValidationRequest request) {
        logger.info("Processing validation request for file: {}", request.getFilename());
        
        // Validate request
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
    
    public ValidationRequest parseRequest(String requestBody) {
        try {
            return objectMapper.readValue(requestBody, ValidationRequest.class);
        } catch (Exception e) {
            logger.error("Failed to parse request body", e);
            throw new ValidationException("Invalid request format: " + e.getMessage());
        }
    }
} 