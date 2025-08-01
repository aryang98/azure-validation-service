package com.example.filevalidation.service;

import com.example.filevalidation.exception.ValidationRuleException;
import com.example.filevalidation.model.ValidationResult;
import com.example.filevalidation.service.impl.ValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for ValidationService
 * 
 * This test class provides comprehensive unit tests for the ValidationService
 * implementation, covering all validation scenarios and edge cases.
 * 
 * Test Coverage:
 * - Email validation (valid and invalid formats)
 * - Date validation (valid and invalid formats)
 * - Name validation (valid and invalid formats)
 * - ID validation (valid and invalid formats)
 * - File format validation
 * - Error handling and exception scenarios
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @InjectMocks
    private ValidationServiceImpl validationService;

    private MockMultipartFile validExcelFile;
    private MockMultipartFile invalidFileFormat;

    @BeforeEach
    void setUp() {
        // Create a valid Excel file content for testing
        byte[] excelContent = createValidExcelContent();
        validExcelFile = new MockMultipartFile(
            "file", 
            "test-data.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            excelContent
        );

        // Create an invalid file format
        invalidFileFormat = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "invalid content".getBytes()
        );
    }

    @Test
    void testIsValidEmail_ValidEmails() {
        // Test valid email formats
        assertTrue(validationService.isValidEmail("test@example.com"));
        assertTrue(validationService.isValidEmail("user.name@domain.co.uk"));
        assertTrue(validationService.isValidEmail("user+tag@example.org"));
        assertTrue(validationService.isValidEmail("123@numbers.com"));
    }

    @Test
    void testIsValidEmail_InvalidEmails() {
        // Test invalid email formats
        assertFalse(validationService.isValidEmail("invalid-email"));
        assertFalse(validationService.isValidEmail("@example.com"));
        assertFalse(validationService.isValidEmail("user@"));
        assertFalse(validationService.isValidEmail("user@.com"));
        assertFalse(validationService.isValidEmail(""));
        assertFalse(validationService.isValidEmail(null));
    }

    @Test
    void testIsValidDate_ValidDates() {
        // Test valid date formats
        assertTrue(validationService.isValidDate("2024-01-01", "yyyy-MM-dd"));
        assertTrue(validationService.isValidDate("2024-12-31", "yyyy-MM-dd"));
        assertTrue(validationService.isValidDate("2024-02-29", "yyyy-MM-dd")); // Leap year
    }

    @Test
    void testIsValidDate_InvalidDates() {
        // Test invalid date formats
        assertFalse(validationService.isValidDate("2024-13-01", "yyyy-MM-dd")); // Invalid month
        assertFalse(validationService.isValidDate("2024-02-30", "yyyy-MM-dd")); // Invalid day
        assertFalse(validationService.isValidDate("invalid-date", "yyyy-MM-dd"));
        assertFalse(validationService.isValidDate("", "yyyy-MM-dd"));
        assertFalse(validationService.isValidDate(null, "yyyy-MM-dd"));
    }

    @Test
    void testIsValidName_ValidNames() {
        // Test valid name formats
        assertTrue(validationService.isValidName("John Doe"));
        assertTrue(validationService.isValidName("Mary Jane"));
        assertTrue(validationService.isValidName("O'Connor"));
        assertTrue(validationService.isValidName("José María"));
    }

    @Test
    void testIsValidName_InvalidNames() {
        // Test invalid name formats
        assertFalse(validationService.isValidName("John123"));
        assertFalse(validationService.isValidName("Doe@"));
        assertFalse(validationService.isValidName(""));
        assertFalse(validationService.isValidName(null));
    }

    @Test
    void testIsValidId_ValidIds() {
        // Test valid ID formats
        assertTrue(validationService.isValidId("ABC123"));
        assertTrue(validationService.isValidId("123456"));
        assertTrue(validationService.isValidId("abc123"));
        assertTrue(validationService.isValidId("ID001"));
    }

    @Test
    void testIsValidId_InvalidIds() {
        // Test invalid ID formats
        assertFalse(validationService.isValidId("ABC-123"));
        assertFalse(validationService.isValidId("ID 001"));
        assertFalse(validationService.isValidId(""));
        assertFalse(validationService.isValidId(null));
    }

    @Test
    void testGetRequiredColumns() {
        List<String> requiredColumns = validationService.getRequiredColumns();
        
        assertNotNull(requiredColumns);
        assertEquals(4, requiredColumns.size());
        assertTrue(requiredColumns.contains("Email"));
        assertTrue(requiredColumns.contains("Date"));
        assertTrue(requiredColumns.contains("Name"));
        assertTrue(requiredColumns.contains("ID"));
    }

    @Test
    void testGetOptionalColumns() {
        List<String> optionalColumns = validationService.getOptionalColumns();
        
        assertNotNull(optionalColumns);
        assertEquals(3, optionalColumns.size());
        assertTrue(optionalColumns.contains("Notes"));
        assertTrue(optionalColumns.contains("Comments"));
        assertTrue(optionalColumns.contains("Description"));
    }

    @Test
    void testValidateColumn_EmailValidation() {
        List<String> emailData = Arrays.asList(
            "valid@example.com",
            "invalid-email",
            "another@test.org",
            "not-an-email"
        );

        List<ValidationResult.ValidationError> errors = validationService.validateColumn(
            "Email", emailData, ValidationService.ValidationType.EMAIL
        );

        assertEquals(2, errors.size());
        assertEquals("invalid-email", errors.get(0).getInvalidValue());
        assertEquals("not-an-email", errors.get(1).getInvalidValue());
    }

    @Test
    void testValidateColumn_DateValidation() {
        List<String> dateData = Arrays.asList(
            "2024-01-01",
            "2024-13-01",
            "2024-02-29",
            "invalid-date"
        );

        List<ValidationResult.ValidationError> errors = validationService.validateColumn(
            "Date", dateData, ValidationService.ValidationType.DATE
        );

        assertEquals(2, errors.size());
        assertEquals("2024-13-01", errors.get(0).getInvalidValue());
        assertEquals("invalid-date", errors.get(1).getInvalidValue());
    }

    @Test
    void testValidateColumn_NameValidation() {
        List<String> nameData = Arrays.asList(
            "John Doe",
            "Mary123",
            "Jane Smith",
            "Bob@"
        );

        List<ValidationResult.ValidationError> errors = validationService.validateColumn(
            "Name", nameData, ValidationService.ValidationType.NAME
        );

        assertEquals(2, errors.size());
        assertEquals("Mary123", errors.get(0).getInvalidValue());
        assertEquals("Bob@", errors.get(1).getInvalidValue());
    }

    @Test
    void testValidateColumn_IdValidation() {
        List<String> idData = Arrays.asList(
            "ABC123",
            "ID-001",
            "123456",
            "ID 002"
        );

        List<ValidationResult.ValidationError> errors = validationService.validateColumn(
            "ID", idData, ValidationService.ValidationType.ID
        );

        assertEquals(2, errors.size());
        assertEquals("ID-001", errors.get(0).getInvalidValue());
        assertEquals("ID 002", errors.get(1).getInvalidValue());
    }

    @Test
    void testValidateFile_InvalidFileFormat() {
        assertThrows(ValidationRuleException.class, () -> {
            validationService.validateFile(invalidFileFormat);
        });
    }

    @Test
    void testValidateFile_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            new byte[0]
        );

        assertThrows(ValidationRuleException.class, () -> {
            validationService.validateFile(emptyFile);
        });
    }

    @Test
    void testValidateFile_NullFilename() {
        MockMultipartFile nullFilenameFile = new MockMultipartFile(
            "file", 
            null, 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "content".getBytes()
        );

        assertThrows(ValidationRuleException.class, () -> {
            validationService.validateFile(nullFilenameFile);
        });
    }

    /**
     * Creates a valid Excel file content for testing
     * 
     * @return byte array containing Excel file content
     */
    private byte[] createValidExcelContent() {
        // This is a simplified implementation
        // In a real test, you would create an actual Excel file with test data
        // For now, we'll return a minimal Excel file structure
        
        try {
            // Create a simple Excel workbook with test data
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Test Data");
            
            // Create header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Email");
            headerRow.createCell(1).setCellValue("Date");
            headerRow.createCell(2).setCellValue("Name");
            headerRow.createCell(3).setCellValue("ID");
            
            // Create data rows
            org.apache.poi.ss.usermodel.Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("test@example.com");
            dataRow1.createCell(1).setCellValue("2024-01-01");
            dataRow1.createCell(2).setCellValue("John Doe");
            dataRow1.createCell(3).setCellValue("ID001");
            
            org.apache.poi.ss.usermodel.Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("invalid-email");
            dataRow2.createCell(1).setCellValue("2024-13-01");
            dataRow2.createCell(2).setCellValue("Jane123");
            dataRow2.createCell(3).setCellValue("ID-002");
            
            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            // Return a minimal Excel file if creation fails
            return new byte[]{0x50, 0x4B, 0x03, 0x04}; // ZIP header (Excel files are ZIP archives)
        }
    }
} 