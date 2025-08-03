package com.example.filevalidation.exception;

import com.example.filevalidation.dto.ValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application
 * Provides centralized error handling and response formatting
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles FileValidationException
     */
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ValidationResponse> handleFileValidationException(FileValidationException ex) {
        log.error("File validation error: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest()
                .body(ValidationResponse.failure(ex.getMessage()));
    }
    
    /**
     * Handles validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.error("Validation error: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(ValidationResponse.failure("Validation failed: " + errorMessage));
    }
    
    /**
     * Handles constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        log.error("Constraint violation: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(ValidationResponse.failure("Constraint violation: " + errorMessage));
    }
    
    /**
     * Handles general runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ValidationResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ValidationResponse.failure("Internal server error: " + ex.getMessage()));
    }
    
    /**
     * Handles all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ValidationResponse.failure("An unexpected error occurred"));
    }
} 