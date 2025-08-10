package com.validation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class ValidationRequest {
    
    @NotBlank(message = "Filename is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Filename contains invalid characters")
    private String filename;
    
    public ValidationRequest() {}
    
    public ValidationRequest(String filename) {
        this.filename = filename;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
} 