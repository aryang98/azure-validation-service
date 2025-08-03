package com.example.filevalidation.service;

import com.example.filevalidation.dto.ValidationResponse;
import com.example.filevalidation.exception.FileValidationException;
import com.example.filevalidation.model.FileMetadata;
import com.example.filevalidation.model.SalesRecord;
import com.example.filevalidation.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ValidationService
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {
    
    @Mock
    private FileMetadataRepository fileMetadataRepository;
    
    @Mock
    private BlobStorageService blobStorageService;
    
    @Mock
    private ExcelProcessingService excelProcessingService;
    
    @Mock
    private Validator validator;
    
    @InjectMocks
    private ValidationService validationService;
    
    private FileMetadata testFileMetadata;
    private List<SalesRecord> testSalesRecords;
    
    @BeforeEach
    void setUp() {
        testFileMetadata = new FileMetadata();
        testFileMetadata.setId(1L);
        testFileMetadata.setFileName("test_sales.xlsx");
        testFileMetadata.setFileUrl("https://storage.blob.core.windows.net/container/test_sales.xlsx");
        
        testSalesRecords = Arrays.asList(
            new SalesRecord("ID001", LocalDate.now(), "dine_in", new BigDecimal("25.50"), null),
            new SalesRecord("ID002", LocalDate.now(), "take_away", new BigDecimal("15.75"), null),
            new SalesRecord("ID003", LocalDate.now(), "dine_in", new BigDecimal("30.00"), null)
        );
    }
    
    @Test
    void validateFile_Success() {
        // Arrange
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        when(blobStorageService.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(excelProcessingService.readExcelFile(any())).thenReturn(testSalesRecords);
        when(validator.validate(any(SalesRecord.class))).thenReturn(Collections.emptySet());
        
        // Act
        ValidationResponse response = validationService.validateFile(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("File validated successfully", response.getMessage());
        assertEquals(3, response.getTotalRows());
        assertEquals(3, response.getValidRows());
        assertEquals(0, response.getInvalidRows());
        assertNull(response.getUpdatedFileUrl());
        
        verify(fileMetadataRepository).findById(1L);
        verify(blobStorageService).downloadFile(testFileMetadata.getFileUrl());
        verify(excelProcessingService).readExcelFile(any());
    }
    
    @Test
    void validateFile_WithValidationErrors() {
        // Arrange
        List<SalesRecord> recordsWithErrors = Arrays.asList(
            new SalesRecord("ID001", LocalDate.now(), "dine_in", new BigDecimal("25.50"), null),
            new SalesRecord("ID002", LocalDate.now(), "invalid_mode", new BigDecimal("15.75"), null),
            new SalesRecord("ID003", LocalDate.now(), "take_away", new BigDecimal("30.00"), null)
        );
        
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        when(blobStorageService.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(excelProcessingService.readExcelFile(any())).thenReturn(recordsWithErrors);
        when(validator.validate(any(SalesRecord.class))).thenReturn(Collections.emptySet());
        when(blobStorageService.uploadFile(anyString(), any())).thenReturn("https://storage.blob.core.windows.net/container/updated_file.xlsx");
        
        // Act
        ValidationResponse response = validationService.validateFile(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals("VALIDATED_WITH_ERRORS", response.getStatus());
        assertEquals("File validated with errors. Check the updated file for details.", response.getMessage());
        assertEquals(3, response.getTotalRows());
        assertEquals(2, response.getValidRows());
        assertEquals(1, response.getInvalidRows());
        assertNotNull(response.getUpdatedFileUrl());
        
        verify(blobStorageService).uploadFile(anyString(), any());
    }
    
    @Test
    void validateFile_FileMetadataNotFound() {
        // Arrange
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        FileValidationException exception = assertThrows(FileValidationException.class, 
            () -> validationService.validateFile(1L));
        
        assertEquals("File metadata not found with ID: 1", exception.getMessage());
        verify(fileMetadataRepository).findById(1L);
        verifyNoInteractions(blobStorageService, excelProcessingService);
    }
    
    @Test
    void validateFile_DownloadError() {
        // Arrange
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        when(blobStorageService.downloadFile(anyString())).thenThrow(new FileValidationException("Download failed"));
        
        // Act & Assert
        FileValidationException exception = assertThrows(FileValidationException.class, 
            () -> validationService.validateFile(1L));
        
        assertEquals("Validation failed: Download failed", exception.getMessage());
        verify(fileMetadataRepository).findById(1L);
        verify(blobStorageService).downloadFile(testFileMetadata.getFileUrl());
        verifyNoInteractions(excelProcessingService);
    }
    
    @Test
    void validateFile_ExcelProcessingError() {
        // Arrange
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        when(blobStorageService.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(excelProcessingService.readExcelFile(any())).thenThrow(new FileValidationException("Excel processing failed"));
        
        // Act & Assert
        FileValidationException exception = assertThrows(FileValidationException.class, 
            () -> validationService.validateFile(1L));
        
        assertEquals("Validation failed: Excel processing failed", exception.getMessage());
        verify(fileMetadataRepository).findById(1L);
        verify(blobStorageService).downloadFile(testFileMetadata.getFileUrl());
        verify(excelProcessingService).readExcelFile(any());
    }
    
    @Test
    void validateFile_WithBeanValidationErrors() {
        // Arrange
        Set<ConstraintViolation<SalesRecord>> violations = new HashSet<>();
        ConstraintViolation<SalesRecord> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(javax.validation.Path.class));
        when(violation.getMessage()).thenReturn("ID is required");
        violations.add(violation);
        
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        when(blobStorageService.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(excelProcessingService.readExcelFile(any())).thenReturn(testSalesRecords);
        when(validator.validate(any(SalesRecord.class))).thenReturn(violations);
        when(blobStorageService.uploadFile(anyString(), any())).thenReturn("https://storage.blob.core.windows.net/container/updated_file.xlsx");
        
        // Act
        ValidationResponse response = validationService.validateFile(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals("VALIDATED_WITH_ERRORS", response.getStatus());
        assertEquals(3, response.getTotalRows());
        assertEquals(0, response.getValidRows());
        assertEquals(3, response.getInvalidRows());
        
        verify(blobStorageService).uploadFile(anyString(), any());
    }
} 