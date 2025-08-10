package com.validation.service;

import com.validation.entity.ValidationMetadata;

public interface ValidationMetadataService {
    
    ValidationMetadata saveValidationMetadata(ValidationMetadata metadata);
    
    ValidationMetadata findById(Long id);
    
    ValidationMetadata findByFileName(String fileName);
} 