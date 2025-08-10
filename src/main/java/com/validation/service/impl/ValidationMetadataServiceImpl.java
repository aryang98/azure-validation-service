package com.validation.service.impl;

import com.validation.dao.ValidationMetadataRepository;
import com.validation.entity.ValidationMetadata;
import com.validation.service.ValidationMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationMetadataServiceImpl implements ValidationMetadataService {
    
    private final ValidationMetadataRepository repository;
    
    @Autowired
    public ValidationMetadataServiceImpl(ValidationMetadataRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public ValidationMetadata saveValidationMetadata(ValidationMetadata metadata) {
        return repository.save(metadata);
    }
    
    @Override
    public ValidationMetadata findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    
    @Override
    public ValidationMetadata findByFileName(String fileName) {
        return repository.findByFileNameOrderByProcessedAtDesc(fileName).stream()
                .findFirst()
                .orElse(null);
    }
} 