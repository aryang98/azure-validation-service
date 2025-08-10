package com.validation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for storing validation metadata in SQL Server.
 * 
 * This entity represents the validation_metadata table that stores:
 * - File validation results and statistics
 * - Processing timestamps
 * - Error information
 * - Output file references
 * 
 * The table provides an audit trail for all file validation operations.
 */
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
    
    /**
     * Default constructor required by JPA.
     */
    public ValidationMetadata() {}
    
    /**
     * Constructor with all required fields.
     * 
     * @param fileName Name of the file being validated
     * @param totalRows Total number of rows in the file
     * @param validRows Number of rows that passed validation
     * @param invalidRows Number of rows that failed validation
     * @param status Status of the validation operation
     * @param uploadedAt When the file was uploaded
     * @param processedAt When the validation was completed
     * @param outputFileName Name of the output file (if errors were found)
     * @param errorMessage Error message (if validation failed)
     */
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
    
    /**
     * Gets the unique identifier.
     * 
     * @return The ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets the unique identifier.
     * 
     * @param id The ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the filename.
     * 
     * @return The filename
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Sets the filename.
     * 
     * @param fileName The filename to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * Gets the total number of rows.
     * 
     * @return Total row count
     */
    public Integer getTotalRows() {
        return totalRows;
    }
    
    /**
     * Sets the total number of rows.
     * 
     * @param totalRows The total row count
     */
    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }
    
    /**
     * Gets the number of valid rows.
     * 
     * @return Valid row count
     */
    public Integer getValidRows() {
        return validRows;
    }
    
    /**
     * Sets the number of valid rows.
     * 
     * @param validRows The valid row count
     */
    public void setValidRows(Integer validRows) {
        this.validRows = validRows;
    }
    
    /**
     * Gets the number of invalid rows.
     * 
     * @return Invalid row count
     */
    public Integer getInvalidRows() {
        return invalidRows;
    }
    
    /**
     * Sets the number of invalid rows.
     * 
     * @param invalidRows The invalid row count
     */
    public void setInvalidRows(Integer invalidRows) {
        this.invalidRows = invalidRows;
    }
    
    /**
     * Gets the validation status.
     * 
     * @return The status
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
     * Gets the upload timestamp.
     * 
     * @return When the file was uploaded
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    /**
     * Sets the upload timestamp.
     * 
     * @param uploadedAt The upload timestamp
     */
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
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
    
    /**
     * Gets the output filename.
     * 
     * @return The output filename (null if no errors)
     */
    public String getOutputFileName() {
        return outputFileName;
    }
    
    /**
     * Sets the output filename.
     * 
     * @param outputFileName The output filename
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    
    /**
     * Gets the error message.
     * 
     * @return The error message (null if no errors)
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Sets the error message.
     * 
     * @param errorMessage The error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 