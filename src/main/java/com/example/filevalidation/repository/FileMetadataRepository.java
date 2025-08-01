package com.example.filevalidation.repository;

import com.example.filevalidation.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * File Metadata Repository
 * 
 * This repository interface provides data access methods for FileMetadata entity.
 * It extends JpaRepository to inherit basic CRUD operations and adds custom
 * query methods for specific business requirements.
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Find file metadata by blob name
     * 
     * @param blobName Azure Blob Storage blob name
     * @return Optional containing the file metadata if found
     */
    Optional<FileMetadata> findByBlobName(String blobName);

    /**
     * Find file metadata by filename
     * 
     * @param filename Original filename
     * @return Optional containing the file metadata if found
     */
    Optional<FileMetadata> findByFilename(String filename);

    /**
     * Find all files by validation status
     * 
     * @param status Validation status to search for
     * @return List of file metadata with the specified status
     */
    List<FileMetadata> findByValidationStatus(FileMetadata.ValidationStatus status);

    /**
     * Find files created within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of file metadata created within the date range
     */
    List<FileMetadata> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find files with errors (validation status is VALIDATED_WITH_ERRORS or FAILED)
     * 
     * @return List of file metadata with errors
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.validationStatus IN ('VALIDATED_WITH_ERRORS', 'FAILED')")
    List<FileMetadata> findFilesWithErrors();

    /**
     * Count files by validation status
     * 
     * @param status Validation status to count
     * @return Number of files with the specified status
     */
    long countByValidationStatus(FileMetadata.ValidationStatus status);

    /**
     * Find files by container name
     * 
     * @param containerName Azure Blob Storage container name
     * @return List of file metadata in the specified container
     */
    List<FileMetadata> findByContainerName(String containerName);

    /**
     * Find files with error count greater than specified value
     * 
     * @param errorCount Minimum number of errors
     * @return List of file metadata with error count greater than specified
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.rowsWithErrors > :errorCount")
    List<FileMetadata> findFilesWithErrorCountGreaterThan(@Param("errorCount") Integer errorCount);

    /**
     * Find files processed in the last N days
     * 
     * @param days Number of days to look back
     * @return List of file metadata processed in the last N days
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.createdAt >= :startDate")
    List<FileMetadata> findFilesProcessedInLastDays(@Param("startDate") LocalDateTime startDate);

    /**
     * Find files by content type
     * 
     * @param contentType MIME type of the file
     * @return List of file metadata with the specified content type
     */
    List<FileMetadata> findByContentType(String contentType);
} 