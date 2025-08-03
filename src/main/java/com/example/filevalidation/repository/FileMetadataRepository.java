package com.example.filevalidation.repository;

import com.example.filevalidation.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for FileMetadata entity
 * Provides data access methods for file metadata operations
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    /**
     * Find file metadata by file URL
     * @param fileUrl The file URL to search for
     * @return Optional containing the file metadata if found
     */
    Optional<FileMetadata> findByFileUrl(String fileUrl);
    
    /**
     * Find file metadata by file name
     * @param fileName The file name to search for
     * @return Optional containing the file metadata if found
     */
    Optional<FileMetadata> findByFileName(String fileName);
    
    /**
     * Check if file metadata exists by file URL
     * @param fileUrl The file URL to check
     * @return true if exists, false otherwise
     */
    boolean existsByFileUrl(String fileUrl);
} 