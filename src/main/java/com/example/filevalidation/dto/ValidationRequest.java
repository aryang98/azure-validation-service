package com.example.filevalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * DTO for file validation request
 * Contains the file metadata ID to validate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {
    
    @NotNull(message = "File metadata ID is required")
    private Long fileMetadataId;
} 