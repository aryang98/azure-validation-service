package com.example.filevalidation.service;

import com.example.filevalidation.dto.ValidationResponse;
import com.example.filevalidation.exception.FileValidationException;
import com.example.filevalidation.model.FileMetadata;
import com.example.filevalidation.model.SalesRecord;
import com.example.filevalidation.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main service for file validation
 * Orchestrates the entire validation process including file download, validation, and upload
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {
    
    private final FileMetadataRepository fileMetadataRepository;
    private final BlobStorageService blobStorageService;
    private final ExcelProcessingService excelProcessingService;
    private final Validator validator;
    
    /**
     * Validate Excel file by file metadata ID
     * @param fileMetadataId The ID of the file metadata to validate
     * @return ValidationResponse with results
     */
    public ValidationResponse validateFile(Long fileMetadataId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Starting validation for file metadata ID: {}", fileMetadataId);
            
            // Get file metadata from database
            FileMetadata fileMetadata = getFileMetadata(fileMetadataId);
            
            // Download file from blob storage
            InputStream fileInputStream = blobStorageService.downloadFile(fileMetadata.getFileUrl());
            
            // Read and validate Excel file
            List<SalesRecord> salesRecords = excelProcessingService.readExcelFile(fileInputStream);
            
            // Validate sales records
            List<SalesRecord> validatedRecords = validateSalesRecords(salesRecords);
            
            // Count validation results
            int totalRows = validatedRecords.size();
            int validRows = (int) validatedRecords.stream()
                    .filter(record -> record.getErrorMessage() == null)
                    .count();
            int invalidRows = totalRows - validRows;
            
            // Generate updated file if there are errors
            String updatedFileUrl = null;
            if (invalidRows > 0) {
                updatedFileUrl = createUpdatedFile(fileMetadata, validatedRecords);
            }
            
            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;
            String processingTimeStr = String.format("%.2f seconds", processingTime / 1000.0);
            
            log.info("Validation completed. Total: {}, Valid: {}, Invalid: {}", 
                    totalRows, validRows, invalidRows);
            
            // Return appropriate response
            if (invalidRows == 0) {
                return ValidationResponse.success(updatedFileUrl, totalRows, validRows, invalidRows, processingTimeStr);
            } else {
                return ValidationResponse.withErrors(updatedFileUrl, totalRows, validRows, invalidRows, processingTimeStr);
            }
            
        } catch (Exception e) {
            log.error("Error during validation for file metadata ID: {}", fileMetadataId, e);
            throw new FileValidationException("Validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get file metadata by ID
     * @param fileMetadataId The file metadata ID
     * @return FileMetadata object
     */
    private FileMetadata getFileMetadata(Long fileMetadataId) {
        return fileMetadataRepository.findById(fileMetadataId)
                .orElseThrow(() -> new FileValidationException(
                    "File metadata not found with ID: " + fileMetadataId));
    }
    
    /**
     * Validate sales records and add error messages
     * @param salesRecords List of sales records to validate
     * @return List of validated sales records with error messages
     */
    private List<SalesRecord> validateSalesRecords(List<SalesRecord> salesRecords) {
        return salesRecords.stream()
                .map(this::validateSalesRecord)
                .collect(Collectors.toList());
    }
    
    /**
     * Validate individual sales record
     * @param record The sales record to validate
     * @return Validated sales record with error message if any
     */
    private SalesRecord validateSalesRecord(SalesRecord record) {
        StringBuilder errorMessages = new StringBuilder();
        
        // Validate using Bean Validation annotations
        Set<ConstraintViolation<SalesRecord>> violations = validator.validate(record);
        for (ConstraintViolation<SalesRecord> violation : violations) {
            if (errorMessages.length() > 0) {
                errorMessages.append("; ");
            }
            errorMessages.append(violation.getPropertyPath()).append(": ").append(violation.getMessage());
        }
        
        // Validate sale mode
        if (record.getSaleMode() != null && !record.isValidSaleMode()) {
            if (errorMessages.length() > 0) {
                errorMessages.append("; ");
            }
            errorMessages.append(record.getSaleModeErrorMessage());
        }
        
        // Validate sale date (not in the past)
        if (record.getSaleDate() != null && record.getSaleDate().isBefore(LocalDateTime.now().toLocalDate())) {
            if (errorMessages.length() > 0) {
                errorMessages.append("; ");
            }
            errorMessages.append("Sale date cannot be in the past");
        }
        
        // Set error message if any validation failed
        if (errorMessages.length() > 0) {
            record.setErrorMessage(errorMessages.toString());
        }
        
        return record;
    }
    
    /**
     * Create updated file with error messages and upload to blob storage
     * @param fileMetadata Original file metadata
     * @param validatedRecords Validated sales records with error messages
     * @return URL of the updated file
     */
    private String createUpdatedFile(FileMetadata fileMetadata, List<SalesRecord> validatedRecords) {
        try {
            // Create updated Excel file with error messages
            byte[] updatedFileData = excelProcessingService.writeExcelFileWithErrors(validatedRecords);
            
            // Generate new filename with timestamp
            String originalFileName = fileMetadata.getFileName();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileNameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            String updatedFileName = fileNameWithoutExtension + "_validated_" + 
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                    fileExtension;
            
            // Upload updated file to blob storage
            String updatedFileUrl = blobStorageService.uploadFile(updatedFileName, updatedFileData);
            
            log.info("Created updated file: {} with URL: {}", updatedFileName, updatedFileUrl);
            return updatedFileUrl;
            
        } catch (Exception e) {
            log.error("Error creating updated file", e);
            throw new FileValidationException("Failed to create updated file: " + e.getMessage(), e);
        }
    }
} 