package com.validation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for file validation requests.
 * 
 * This class represents the input request for the file validation service.
 * It contains validation annotations to ensure proper request format.
 * 
 * Usage:
 * - Send as JSON in the request body to the /api/validate endpoint
 * - The filename must reference a file already stored in Azure Blob Storage
 * - Only .xlsx and .csv files are supported
 */
public class ValidationRequest {
    
    /**
     * The filename of the file to validate.
     * 
     * Validation Rules:
     * - Must not be blank (null, empty, or whitespace-only)
     * - Must contain only alphanumeric characters, dots, underscores, and hyphens
     * - Must reference a file that exists in the configured Azure Blob Storage container
     * 
     * Examples:
     * - "sales_data.xlsx" ✅ Valid
     * - "customer_data.csv" ✅ Valid
     * - "report_2023-12-01.xlsx" ✅ Valid
     * - "" ❌ Invalid (blank)
     * - "file with spaces.xlsx" ❌ Invalid (contains spaces)
     * - "file@name.xlsx" ❌ Invalid (contains special characters)
     */
    @NotBlank(message = "Filename is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Filename contains invalid characters")
    private String filename;
    
    /**
     * Default constructor required for JSON deserialization.
     */
    public ValidationRequest() {}
    
    /**
     * Constructor with filename parameter.
     * 
     * @param filename The name of the file to validate
     */
    public ValidationRequest(String filename) {
        this.filename = filename;
    }
    
    /**
     * Gets the filename to validate.
     * 
     * @return The filename
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Sets the filename to validate.
     * 
     * @param filename The filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
} 