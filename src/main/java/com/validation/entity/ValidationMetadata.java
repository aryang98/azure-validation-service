package com.validation.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_metadata")
public class ValidationMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;
    
    @Column(name = "valid_rows", nullable = false)
    private Integer validRows;
    
    @Column(name = "invalid_rows", nullable = false)
    private Integer invalidRows;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
    
    @Column(name = "output_file_name")
    private String outputFileName;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    public ValidationMetadata() {}
    
    public ValidationMetadata(String fileName, Integer totalRows, Integer validRows, Integer invalidRows, 
                            String status, LocalDateTime uploadedAt, LocalDateTime processedAt, 
                            String outputFileName, String errorMessage) {
        this.fileName = fileName;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.invalidRows = invalidRows;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.processedAt = processedAt;
        this.outputFileName = outputFileName;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Integer getTotalRows() {
        return totalRows;
    }
    
    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }
    
    public Integer getValidRows() {
        return validRows;
    }
    
    public void setValidRows(Integer validRows) {
        this.validRows = validRows;
    }
    
    public Integer getInvalidRows() {
        return invalidRows;
    }
    
    public void setInvalidRows(Integer invalidRows) {
        this.invalidRows = invalidRows;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getOutputFileName() {
        return outputFileName;
    }
    
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 