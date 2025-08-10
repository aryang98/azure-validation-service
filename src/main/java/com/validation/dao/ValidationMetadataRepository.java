package com.validation.dao;

import com.validation.entity.ValidationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ValidationMetadataRepository extends JpaRepository<ValidationMetadata, Long> {
    
    List<ValidationMetadata> findByFileNameOrderByProcessedAtDesc(String fileName);
    
    List<ValidationMetadata> findByStatusOrderByProcessedAtDesc(String status);
    
    List<ValidationMetadata> findByProcessedAtBetweenOrderByProcessedAtDesc(LocalDateTime start, LocalDateTime end);
    
    List<ValidationMetadata> findByTotalRowsGreaterThanOrderByProcessedAtDesc(Integer totalRows);
} 