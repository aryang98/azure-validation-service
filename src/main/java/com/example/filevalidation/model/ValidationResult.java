package com.example.filevalidation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * Validation Result Model
 * 
 * This class represents the result of file validation operations,
 * including validation status, error details, and file information.
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * Whether the validation was successful
     */
    private boolean success;

    /**
     * Validation status
     */
    private ValidationStatus status;

    /**
     * Original filename
     */
    private String filename;

    /**
     * Number of rows processed
     */
    private int rowsProcessed;

    /**
     * Number of rows with errors
     */
    private int rowsWithErrors;

    /**
     * List of validation errors
     */
    private List<ValidationError> errors;

    /**
     * Download URL for processed file (if successful)
     */
    private String downloadUrl;

    /**
     * Download URL for error file (if errors exist)
     */
    private String errorFileUrl;

    /**
     * Error message if validation failed completely
     */
    private String errorMessage;

    /**
     * Processed workbook with error column
     */
    private transient Workbook processedWorkbook;

    /**
     * Error workbook with only rows that have errors
     */
    private transient Workbook errorWorkbook;

    /**
     * Validation Status Enumeration
     */
    public enum ValidationStatus {
        SUCCESS,
        SUCCESS_WITH_ERRORS,
        FAILED
    }

    /**
     * Validation Error Model
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * Row number where the error occurred
         */
        private int rowNumber;

        /**
         * Column name where the error occurred
         */
        private String columnName;

        /**
         * Error message
         */
        private String errorMessage;

        /**
         * Invalid value that caused the error
         */
        private String invalidValue;
    }
} 