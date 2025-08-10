package com.validation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ValidationResponse {
    
    private String status;
    private String message;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private String downloadUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
    
    public ValidationResponse() {}
    
    public ValidationResponse(String status, String message, int totalRows, int validRows, int invalidRows, String downloadUrl) {
        this.status = status;
        this.message = message;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.invalidRows = invalidRows;
        this.downloadUrl = downloadUrl;
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
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
} 