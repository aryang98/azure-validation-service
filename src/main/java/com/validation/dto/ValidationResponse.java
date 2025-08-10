package com.validation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResponse {
    
    private String status;
    private String message;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private String downloadUrl; // Only included if errors exist
    private Long executionTimeMs; // Execution time in milliseconds
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    public ValidationResponse() {}
    
    // Constructor for successful validation with no errors
    public ValidationResponse(String status, String message, int totalRows, Long executionTimeMs) {
        this.status = status;
        this.message = message;
        this.totalRows = totalRows;
        this.validRows = totalRows;
        this.invalidRows = 0;
        this.executionTimeMs = executionTimeMs;
        this.processedAt = LocalDateTime.now();
    }
    
    // Constructor for validation with errors
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
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getTotalRows() {
        return totalRows;
    }
    
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
    
    public int getValidRows() {
        return validRows;
    }
    
    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }
    
    public int getInvalidRows() {
        return invalidRows;
    }
    
    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
} 