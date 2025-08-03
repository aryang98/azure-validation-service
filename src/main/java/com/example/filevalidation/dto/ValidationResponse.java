package com.example.filevalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for file validation response
 * Contains validation results and statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    
    private String status;
    private String message;
    private String updatedFileUrl;
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private LocalDateTime validationDate;
    private String processingTime;
    
    /**
     * Creates a successful validation response
     */
    public static ValidationResponse success(String updatedFileUrl, int totalRows, int validRows, 
                                          int invalidRows, String processingTime) {
        return new ValidationResponse(
            "SUCCESS",
            "File validated successfully",
            updatedFileUrl,
            totalRows,
            validRows,
            invalidRows,
            LocalDateTime.now(),
            processingTime
        );
    }
    
    /**
     * Creates a response with validation errors
     */
    public static ValidationResponse withErrors(String updatedFileUrl, int totalRows, int validRows, 
                                             int invalidRows, String processingTime) {
        return new ValidationResponse(
            "VALIDATED_WITH_ERRORS",
            "File validated with errors. Check the updated file for details.",
            updatedFileUrl,
            totalRows,
            validRows,
            invalidRows,
            LocalDateTime.now(),
            processingTime
        );
    }
    
    /**
     * Creates a failure response
     */
    public static ValidationResponse failure(String message) {
        return new ValidationResponse(
            "FAILED",
            message,
            null,
            0,
            0,
            0,
            LocalDateTime.now(),
            null
        );
    }
} 