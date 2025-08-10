package com.validation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for file validation responses.
 * 
 * This class represents the response from the file validation service.
 * It provides different response formats based on validation results:
 * 
 * 1. SUCCESS (no errors): Simple response with status, message, total rows, and execution time
 * 2. COMPLETED_WITH_ERRORS: Detailed response with validation statistics and download URL
 * 
 * The response includes performance metrics and audit information for monitoring.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResponse {
    
    /**
     * The status of the validation operation.
     * 
     * Possible values:
     * - "SUCCESS": File validation completed successfully with no errors
     * - "COMPLETED_WITH_ERRORS": File validation completed but errors were found
     */
    private String status;
    
    /**
     * Human-readable message describing the validation result.
     * 
     * Examples:
     * - "File validation completed successfully - no errors found"
     * - "File validation completed with errors. Download the corrected file using the provided URL."
     */
    private String message;
    
    /**
     * Total number of data rows processed (excluding header row).
     */
    private int totalRows;
    
    /**
     * Number of rows that passed all validation rules.
     */
    private int validRows;
    
    /**
     * Number of rows that failed one or more validation rules.
     */
    private int invalidRows;
    
    /**
     * Secure download URL for the processed file (only included when errors exist).
     * 
     * This is a SAS (Shared Access Signature) URL that provides secure,
     * time-limited access to download the modified file from Azure Blob Storage.
     * 
     * Format: https://storage.blob.core.windows.net/container/filename?sv=...&sig=...
     */
    private String downloadUrl; // Only included if errors exist
    
    /**
     * Execution time in milliseconds.
     * 
     * This metric helps monitor performance and identify bottlenecks.
     * Includes time for file download, validation, and upload (if applicable).
     */
    private Long executionTimeMs; // Execution time in milliseconds
    
    /**
     * Timestamp when the validation was completed.
     * 
     * Format: yyyy-MM-dd'T'HH:mm:ss
     * Example: "2023-12-01T14:30:22"
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    /**
     * Default constructor required for JSON serialization.
     */
    public ValidationResponse() {}
    
    /**
     * Constructor for successful validation with no errors.
     * 
     * This constructor is used when the file passes all validation rules.
     * No download URL is provided since no file modification is needed.
     * 
     * @param status The status (typically "SUCCESS")
     * @param message Success message
     * @param totalRows Total number of rows processed
     * @param executionTimeMs Execution time in milliseconds
     */
    public ValidationResponse(String status, String message, int totalRows, Long executionTimeMs) {
        this.status = status;
        this.message = message;
        this.totalRows = totalRows;
        this.validRows = totalRows; // All rows are valid when no errors
        this.invalidRows = 0;       // No invalid rows
        this.executionTimeMs = executionTimeMs;
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for validation with errors.
     * 
     * This constructor is used when the file contains validation errors.
     * A download URL is provided for the modified file with error highlighting.
     * 
     * @param status The status (typically "COMPLETED_WITH_ERRORS")
     * @param message Message indicating errors were found
     * @param totalRows Total number of rows processed
     * @param validRows Number of valid rows
     * @param invalidRows Number of invalid rows
     * @param downloadUrl SAS URL for downloading the modified file
     * @param executionTimeMs Execution time in milliseconds
     */
    public ValidationResponse(String status, String message, int totalRows, int validRows, int invalidRows, String downloadUrl, Long executionTimeMs) {
        this.status = status;
        this.message = message;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.invalidRows = invalidRows;
        this.downloadUrl = downloadUrl;
        this.executionTimeMs = executionTimeMs;
        this.processedAt = LocalDateTime.now();
    }
    
    // Getters and Setters with documentation
    
    /**
     * Gets the validation status.
     * 
     * @return The status string
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Sets the validation status.
     * 
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Gets the validation message.
     * 
     * @return The message string
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the validation message.
     * 
     * @param message The message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the total number of rows processed.
     * 
     * @return Total row count
     */
    public int getTotalRows() {
        return totalRows;
    }
    
    /**
     * Sets the total number of rows processed.
     * 
     * @param totalRows The total row count
     */
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
    
    /**
     * Gets the number of valid rows.
     * 
     * @return Valid row count
     */
    public int getValidRows() {
        return validRows;
    }
    
    /**
     * Sets the number of valid rows.
     * 
     * @param validRows The valid row count
     */
    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }
    
    /**
     * Gets the number of invalid rows.
     * 
     * @return Invalid row count
     */
    public int getInvalidRows() {
        return invalidRows;
    }
    
    /**
     * Sets the number of invalid rows.
     * 
     * @param invalidRows The invalid row count
     */
    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }
    
    /**
     * Gets the download URL for the modified file.
     * 
     * @return SAS URL for file download (null if no errors)
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    /**
     * Sets the download URL for the modified file.
     * 
     * @param downloadUrl The SAS URL
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    /**
     * Gets the execution time in milliseconds.
     * 
     * @return Execution time in ms
     */
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Sets the execution time in milliseconds.
     * 
     * @param executionTimeMs The execution time in ms
     */
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    /**
     * Gets the processing timestamp.
     * 
     * @return When the validation was completed
     */
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    /**
     * Sets the processing timestamp.
     * 
     * @param processedAt The processing timestamp
     */
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
} 