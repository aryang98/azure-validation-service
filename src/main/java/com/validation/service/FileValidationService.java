package com.validation.service;

import com.validation.dto.ValidationRequest;
import com.validation.dto.ValidationResponse;

public interface FileValidationService {
    
    ValidationResponse validateFile(ValidationRequest request);
    
    boolean isFileSupported(String filename);
    
    String getFileExtension(String filename);
} 