package com.example.filevalidation.service.impl;

import com.example.filevalidation.exception.FileValidationException;
import com.example.filevalidation.model.FileMetadata;
import com.example.filevalidation.model.ValidationResult;
import com.example.filevalidation.repository.FileMetadataRepository;
import com.example.filevalidation.service.AzureBlobService;
import com.example.filevalidation.service.FileProcessingService;
import com.example.filevalidation.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * File Processing Service Implementation
 * 
 * This class implements the FileProcessingService interface and orchestrates
 * the complete file validation workflow including Azure Blob Storage and
 * Azure SQL Database integration.
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
@Service
@Slf4j
public class FileProcessingServiceImpl implements FileProcessingService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AzureBlobService azureBlobService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Override
    public ValidationResult processFile(MultipartFile file) {
        log.info("Starting file processing workflow for file: {}", file.getOriginalFilename());

        try {
            // Step 1: Upload file to Azure Blob Storage
            String blobName = azureBlobService.uploadFile(file, null);
            log.info("File uploaded to Azure Blob Storage with blob name: {}", blobName);

            // Step 2: Validate the file content
            ValidationResult validationResult = validationService.validateFile(file);
            log.info("File validation completed. Rows processed: {}, Rows with errors: {}", 
                    validationResult.getRowsProcessed(), validationResult.getRowsWithErrors());

            // Step 3: Create file metadata
            FileMetadata fileMetadata = createFileMetadata(file, blobName, validationResult);
            FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);
            log.info("File metadata saved with ID: {}", savedMetadata.getId());

            // Step 4: Generate download URLs and upload error files
            String downloadUrl = azureBlobService.getDownloadUrl(blobName);
            validationResult.setDownloadUrl(downloadUrl);

            // If there are errors, create and upload error file
            if (validationResult.getRowsWithErrors() > 0 && validationResult.getErrorWorkbook() != null) {
                String errorBlobName = generateErrorBlobName(blobName);
                uploadErrorWorkbook(validationResult.getErrorWorkbook(), errorBlobName);
                String errorFileUrl = azureBlobService.getDownloadUrl(errorBlobName);
                validationResult.setErrorFileUrl(errorFileUrl);
                
                // Update metadata with error file URL
                savedMetadata.setErrorFileUrl(errorFileUrl);
                fileMetadataRepository.save(savedMetadata);
                
                log.info("Error file uploaded with blob name: {}", errorBlobName);
            }

            log.info("File processing workflow completed successfully");
            return validationResult;

        } catch (Exception e) {
            log.error("Error in file processing workflow: {}", e.getMessage(), e);
            throw new FileValidationException("File processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public FileMetadata getFileMetadata(Long fileId) {
        log.debug("Retrieving file metadata for ID: {}", fileId);
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileValidationException("File metadata not found for ID: " + fileId));
    }

    @Override
    public FileMetadata getFileMetadataByBlobName(String blobName) {
        log.debug("Retrieving file metadata for blob name: {}", blobName);
        return fileMetadataRepository.findByBlobName(blobName)
                .orElseThrow(() -> new FileValidationException("File metadata not found for blob name: " + blobName));
    }

    @Override
    public List<FileMetadata> getAllFileMetadata() {
        log.debug("Retrieving all file metadata");
        return fileMetadataRepository.findAll();
    }

    @Override
    public List<FileMetadata> getFileMetadataByStatus(FileMetadata.ValidationStatus status) {
        log.debug("Retrieving file metadata by status: {}", status);
        return fileMetadataRepository.findByValidationStatus(status);
    }

    @Override
    public List<FileMetadata> getFileMetadataWithErrors() {
        log.debug("Retrieving file metadata with errors");
        return fileMetadataRepository.findFilesWithErrors();
    }

    @Override
    public boolean deleteFile(Long fileId) {
        log.info("Deleting file with ID: {}", fileId);
        
        try {
            FileMetadata fileMetadata = getFileMetadata(fileId);
            
            // Delete from Azure Blob Storage
            boolean blobDeleted = azureBlobService.deleteFile(fileMetadata.getBlobName());
            log.info("Blob deletion result: {}", blobDeleted);
            
            // Delete from database
            fileMetadataRepository.deleteById(fileId);
            log.info("File metadata deleted from database");
            
            return true;
        } catch (Exception e) {
            log.error("Error deleting file with ID {}: {}", fileId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProcessedFileDownloadUrl(Long fileId) {
        log.debug("Generating download URL for file ID: {}", fileId);
        
        try {
            FileMetadata fileMetadata = getFileMetadata(fileId);
            return azureBlobService.getDownloadUrl(fileMetadata.getBlobName());
        } catch (Exception e) {
            log.error("Error generating download URL for file ID {}: {}", fileId, e.getMessage(), e);
            throw new FileValidationException("Failed to generate download URL: " + e.getMessage(), e);
        }
    }

    @Override
    public String getErrorFileDownloadUrl(Long fileId) {
        log.debug("Generating error file download URL for file ID: {}", fileId);
        
        try {
            FileMetadata fileMetadata = getFileMetadata(fileId);
            if (fileMetadata.getErrorFileUrl() != null) {
                return fileMetadata.getErrorFileUrl();
            } else {
                throw new FileValidationException("No error file available for file ID: " + fileId);
            }
        } catch (Exception e) {
            log.error("Error generating error file download URL for file ID {}: {}", fileId, e.getMessage(), e);
            throw new FileValidationException("Failed to generate error file download URL: " + e.getMessage(), e);
        }
    }

    @Override
    public ProcessingStatistics getProcessingStatistics() {
        log.debug("Generating processing statistics");
        
        try {
            long totalFilesProcessed = fileMetadataRepository.count();
            long filesWithErrors = fileMetadataRepository.countByValidationStatus(FileMetadata.ValidationStatus.VALIDATED_WITH_ERRORS) +
                                 fileMetadataRepository.countByValidationStatus(FileMetadata.ValidationStatus.FAILED);
            long filesSuccessfullyProcessed = fileMetadataRepository.countByValidationStatus(FileMetadata.ValidationStatus.VALIDATED);
            
            // Calculate total rows processed and with errors
            long totalRowsProcessed = 0;
            long totalRowsWithErrors = 0;
            
            List<FileMetadata> allFiles = fileMetadataRepository.findAll();
            for (FileMetadata file : allFiles) {
                if (file.getRowsProcessed() != null) {
                    totalRowsProcessed += file.getRowsProcessed();
                }
                if (file.getRowsWithErrors() != null) {
                    totalRowsWithErrors += file.getRowsWithErrors();
                }
            }
            
            return new ProcessingStatistics(
                totalFilesProcessed,
                filesWithErrors,
                filesSuccessfullyProcessed,
                totalRowsProcessed,
                totalRowsWithErrors
            );
            
        } catch (Exception e) {
            log.error("Error generating processing statistics: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to generate processing statistics: " + e.getMessage(), e);
        }
    }

    /**
     * Creates FileMetadata object from file and validation result
     * 
     * @param file The uploaded file
     * @param blobName The blob name in Azure Storage
     * @param validationResult The validation result
     * @return FileMetadata object
     */
    private FileMetadata createFileMetadata(MultipartFile file, String blobName, ValidationResult validationResult) {
        FileMetadata.ValidationStatus status;
        if (validationResult.getRowsWithErrors() == 0) {
            status = FileMetadata.ValidationStatus.VALIDATED;
        } else if (validationResult.getRowsWithErrors() > 0) {
            status = FileMetadata.ValidationStatus.VALIDATED_WITH_ERRORS;
        } else {
            status = FileMetadata.ValidationStatus.FAILED;
        }

        return FileMetadata.builder()
                .filename(file.getOriginalFilename())
                .containerName("file-uploads") // Default container
                .blobName(blobName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .validationStatus(status)
                .rowsProcessed(validationResult.getRowsProcessed())
                .rowsWithErrors(validationResult.getRowsWithErrors())
                .downloadUrl(azureBlobService.getDownloadUrl(blobName))
                .build();
    }

    /**
     * Generates error blob name for files with validation errors
     * 
     * @param blobName The original blob name
     * @return Error blob name
     */
    private String generateErrorBlobName(String blobName) {
        // Create error file blob name by adding "_errors" suffix
        return blobName.replaceFirst("\\.(xlsx?)$", "_errors.$1");
    }

    /**
     * Uploads error workbook to Azure Blob Storage
     * 
     * @param errorWorkbook The error workbook to upload
     * @param errorBlobName The blob name for the error file
     */
    private void uploadErrorWorkbook(Workbook errorWorkbook, String errorBlobName) {
        try {
            // Convert workbook to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            errorWorkbook.write(outputStream);
            byte[] workbookBytes = outputStream.toByteArray();
            
            // Create MultipartFile from byte array
            org.springframework.mock.web.MockMultipartFile errorFile = 
                new org.springframework.mock.web.MockMultipartFile(
                    "error-file",
                    errorBlobName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    workbookBytes
                );
            
            // Upload to Azure Blob Storage
            azureBlobService.uploadFile(errorFile, errorBlobName, null);
            
            log.info("Error workbook uploaded successfully with blob name: {}", errorBlobName);
            
        } catch (Exception e) {
            log.error("Error uploading error workbook: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to upload error workbook: " + e.getMessage(), e);
        }
    }
} 