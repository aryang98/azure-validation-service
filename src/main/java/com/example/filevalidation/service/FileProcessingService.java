package com.example.filevalidation.service;

import com.example.filevalidation.model.FileMetadata;
import com.example.filevalidation.model.ValidationResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File Processing Service Interface
 * 
 * This interface defines the contract for the main file processing operations
 * that orchestrates the entire file validation workflow including Azure Blob Storage
 * and Azure SQL Database integration.
 * 
 * Business Operations:
 * - Complete file validation workflow orchestration
 * - Azure Blob Storage integration for file storage
 * - Azure SQL Database integration for metadata storage
 * - Error handling and reporting
 * - File download URL generation
 * 
 * Workflow:
 * 1. Upload file to Azure Blob Storage
 * 2. Validate file content with row-level error tracking
 * 3. Store metadata in Azure SQL Database
 * 4. Generate download URLs for processed files
 * 5. Return comprehensive validation results
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface FileProcessingService {

    /**
     * Processes and validates an uploaded file
     * 
     * This method orchestrates the complete file processing workflow:
     * - Uploads the file to Azure Blob Storage
     * - Validates the file content with row-level error tracking
     * - Stores metadata in Azure SQL Database
     * - Generates download URLs for processed files
     * 
     * @param file The uploaded file to process
     * @return ValidationResult containing comprehensive validation results
     * @throws com.example.filevalidation.exception.FileValidationException if processing fails
     */
    ValidationResult processFile(MultipartFile file);

    /**
     * Gets file metadata by ID
     * 
     * @param fileId The ID of the file metadata
     * @return FileMetadata object if found
     */
    FileMetadata getFileMetadata(Long fileId);

    /**
     * Gets file metadata by blob name
     * 
     * @param blobName The blob name of the file
     * @return FileMetadata object if found
     */
    FileMetadata getFileMetadataByBlobName(String blobName);

    /**
     * Gets all file metadata
     * 
     * @return List of all file metadata
     */
    List<FileMetadata> getAllFileMetadata();

    /**
     * Gets file metadata by validation status
     * 
     * @param status The validation status to filter by
     * @return List of file metadata with the specified status
     */
    List<FileMetadata> getFileMetadataByStatus(FileMetadata.ValidationStatus status);

    /**
     * Gets file metadata with errors
     * 
     * @return List of file metadata that have validation errors
     */
    List<FileMetadata> getFileMetadataWithErrors();

    /**
     * Deletes a file and its metadata
     * 
     * @param fileId The ID of the file to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteFile(Long fileId);

    /**
     * Gets the download URL for a processed file
     * 
     * @param fileId The ID of the file
     * @return Download URL for the processed file
     */
    String getProcessedFileDownloadUrl(Long fileId);

    /**
     * Gets the download URL for an error file (if validation had errors)
     * 
     * @param fileId The ID of the file
     * @return Download URL for the error file
     */
    String getErrorFileDownloadUrl(Long fileId);

    /**
     * Gets processing statistics
     * 
     * @return ProcessingStatistics containing summary information
     */
    ProcessingStatistics getProcessingStatistics();

    /**
     * Processing Statistics Model
     */
    class ProcessingStatistics {
        private long totalFilesProcessed;
        private long filesWithErrors;
        private long filesSuccessfullyProcessed;
        private long totalRowsProcessed;
        private long totalRowsWithErrors;

        // Constructors
        public ProcessingStatistics() {}

        public ProcessingStatistics(long totalFilesProcessed, long filesWithErrors, 
                                 long filesSuccessfullyProcessed, long totalRowsProcessed, 
                                 long totalRowsWithErrors) {
            this.totalFilesProcessed = totalFilesProcessed;
            this.filesWithErrors = filesWithErrors;
            this.filesSuccessfullyProcessed = filesSuccessfullyProcessed;
            this.totalRowsProcessed = totalRowsProcessed;
            this.totalRowsWithErrors = totalRowsWithErrors;
        }

        // Getters and Setters
        public long getTotalFilesProcessed() { return totalFilesProcessed; }
        public void setTotalFilesProcessed(long totalFilesProcessed) { this.totalFilesProcessed = totalFilesProcessed; }

        public long getFilesWithErrors() { return filesWithErrors; }
        public void setFilesWithErrors(long filesWithErrors) { this.filesWithErrors = filesWithErrors; }

        public long getFilesSuccessfullyProcessed() { return filesSuccessfullyProcessed; }
        public void setFilesSuccessfullyProcessed(long filesSuccessfullyProcessed) { this.filesSuccessfullyProcessed = filesSuccessfullyProcessed; }

        public long getTotalRowsProcessed() { return totalRowsProcessed; }
        public void setTotalRowsProcessed(long totalRowsProcessed) { this.totalRowsProcessed = totalRowsProcessed; }

        public long getTotalRowsWithErrors() { return totalRowsWithErrors; }
        public void setTotalRowsWithErrors(long totalRowsWithErrors) { this.totalRowsWithErrors = totalRowsWithErrors; }
    }
} 