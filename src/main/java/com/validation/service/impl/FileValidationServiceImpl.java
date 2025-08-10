package com.validation.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.validation.dto.ValidationRequest;
import com.validation.dto.ValidationResponse;
import com.validation.entity.ValidationMetadata;
import com.validation.exception.UnsupportedFileTypeException;
import com.validation.exception.ValidationException;
import com.validation.service.BlobStorageService;
import com.validation.service.FileValidationService;
import com.validation.service.ValidationMetadataService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of the file validation service that processes Excel and CSV files.
 * 
 * This service provides the following functionality:
 * - Streaming file processing to handle large files efficiently
 * - Validation of required fields and data types
 * - Visual error highlighting in Excel files
 * - Error details column addition for both Excel and CSV
 * - Conditional file upload (only when errors are found)
 * - Performance tracking and metadata persistence
 * 
 * Key Features:
 * - Preserves all original data - no data is removed or modified
 * - Only adds ErrorDetails column and highlighting when errors exist
 * - Returns simple response for successful validations
 * - Returns detailed response with download URL for failed validations
 */
@Service
public class FileValidationServiceImpl implements FileValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationServiceImpl.class);
    
    // Regex pattern to validate numeric fields (ID must be numeric)
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    
    // Date format expected for date fields (yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Service dependencies
    private final BlobStorageService blobStorageService;
    private final ValidationMetadataService metadataService;
    
    // Configuration properties injected from application.properties
    @Value("${file.validation.required-columns}")
    private String requiredColumnsConfig; // Comma-separated list of required columns
    
    @Value("${file.validation.max-size-mb}")
    private int maxFileSizeMb; // Maximum file size in MB
    
    @Value("${azure.blob.sas-expiry-hours}")
    private int sasExpiryHours; // SAS URL expiry time in hours
    
    /**
     * Constructor with dependency injection
     * 
     * @param blobStorageService Service for Azure Blob Storage operations
     * @param metadataService Service for persisting validation metadata
     */
    @Autowired
    public FileValidationServiceImpl(BlobStorageService blobStorageService, 
                                   ValidationMetadataService metadataService) {
        this.blobStorageService = blobStorageService;
        this.metadataService = metadataService;
    }
    
    /**
     * Main validation method that orchestrates the entire validation process.
     * 
     * Process Flow:
     * 1. Validate file type and size
     * 2. Stream process the file (Excel or CSV)
     * 3. If no errors: Return simple success response
     * 4. If errors found: Upload modified file and return download URL
     * 5. Persist metadata for audit trail
     * 
     * @param request Validation request containing the filename
     * @return ValidationResponse with results and optional download URL
     */
    @Override
    public ValidationResponse validateFile(ValidationRequest request) {
        // Track execution time for performance monitoring
        long startTime = System.currentTimeMillis();
        String filename = request.getFilename();
        
        // Step 1: Validate file type support
        if (!isFileSupported(filename)) {
            throw new UnsupportedFileTypeException(getFileExtension(filename));
        }
        
        // Step 2: Check file size limits
        long fileSize = blobStorageService.getFileSize(filename);
        if (fileSize > maxFileSizeMb * 1024 * 1024) {
            throw new ValidationException("File size exceeds maximum allowed size of " + maxFileSizeMb + "MB");
        }
        
        // Step 3: Determine file type and process accordingly
        String fileExtension = getFileExtension(filename);
        ValidationResult result;
        
        try (InputStream inputStream = blobStorageService.downloadFile(filename)) {
            // Process based on file type
            if (fileExtension.equalsIgnoreCase("xlsx")) {
                result = validateExcelFile(inputStream);
            } else {
                result = validateCsvFile(inputStream);
            }
            
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Step 4: Handle response based on validation results
            if (result.getInvalidRows() == 0) {
                // No errors found - return simple success response
                return handleSuccessfulValidation(filename, result, executionTime);
            } else {
                // Errors found - upload modified file and return download URL
                return handleValidationWithErrors(filename, fileExtension, result, executionTime);
            }
            
        } catch (Exception e) {
            // Handle any unexpected errors
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error validating file: {}", filename, e);
            
            // Save error metadata for audit trail
            saveErrorMetadata(filename, e.getMessage());
            
            throw new ValidationException("Failed to validate file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handles successful validation with no errors.
     * Saves metadata and returns simple success response.
     */
    private ValidationResponse handleSuccessfulValidation(String filename, ValidationResult result, long executionTime) {
        // Save metadata for successful validation
        ValidationMetadata metadata = new ValidationMetadata(
                filename,
                result.getTotalRows(),
                result.getValidRows(),
                result.getInvalidRows(),
                "SUCCESS",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null, // No output file for successful validation
                null  // No error message
        );
        metadataService.saveValidationMetadata(metadata);
        
        // Return simple success response (no download URL needed)
        return new ValidationResponse(
                "SUCCESS",
                "File validation completed successfully - no errors found",
                result.getTotalRows(),
                executionTime
        );
    }
    
    /**
     * Handles validation with errors.
     * Uploads modified file and returns download URL.
     */
    private ValidationResponse handleValidationWithErrors(String filename, String fileExtension, 
                                                        ValidationResult result, long executionTime) {
        // Generate output filename with timestamp
        String outputFilename = generateOutputFilename(filename, fileExtension);
        
        // Upload the modified file to blob storage
        try (InputStream processedStream = new ByteArrayInputStream(result.getProcessedData())) {
            blobStorageService.uploadFile(outputFilename, processedStream, result.getProcessedData().length);
        }
        
        // Generate SAS URL for secure download
        String downloadUrl = blobStorageService.generateSasUrl(outputFilename, sasExpiryHours);
        
        // Save metadata for validation with errors
        ValidationMetadata metadata = new ValidationMetadata(
                filename,
                result.getTotalRows(),
                result.getValidRows(),
                result.getInvalidRows(),
                "COMPLETED_WITH_ERRORS",
                LocalDateTime.now(),
                LocalDateTime.now(),
                outputFilename,
                null
        );
        metadataService.saveValidationMetadata(metadata);
        
        // Return detailed response with download URL
        return new ValidationResponse(
                "COMPLETED_WITH_ERRORS",
                "File validation completed with errors. Download the corrected file using the provided URL.",
                result.getTotalRows(),
                result.getValidRows(),
                result.getInvalidRows(),
                downloadUrl,
                executionTime
        );
    }
    
    /**
     * Saves error metadata when validation fails completely.
     */
    private void saveErrorMetadata(String filename, String errorMessage) {
        ValidationMetadata errorMetadata = new ValidationMetadata(
                filename,
                0,
                0,
                0,
                "ERROR",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                errorMessage
        );
        metadataService.saveValidationMetadata(errorMetadata);
    }
    
    /**
     * Checks if the file type is supported by the service.
     * Currently supports .xlsx and .csv files.
     * 
     * @param filename Name of the file to check
     * @return true if file type is supported, false otherwise
     */
    @Override
    public boolean isFileSupported(String filename) {
        String extension = getFileExtension(filename);
        return extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("csv");
    }
    
    /**
     * Extracts the file extension from the filename.
     * 
     * @param filename Name of the file
     * @return File extension (without the dot) or empty string if no extension
     */
    @Override
    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    /**
     * Validates Excel (.xlsx) files using Apache POI.
     * 
     * Process:
     * 1. Read workbook and get first sheet
     * 2. Parse headers and create column mapping
     * 3. Validate required columns exist
     * 4. Process each row and validate data
     * 5. Highlight invalid cells in yellow
     * 6. Add ErrorDetails column with error messages
     * 7. Preserve all original data
     * 
     * @param inputStream Stream containing the Excel file
     * @return ValidationResult with statistics and processed data
     * @throws IOException If file reading fails
     */
    private ValidationResult validateExcelFile(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            List<String> requiredColumns = Arrays.asList(requiredColumnsConfig.split(","));
            
            // Step 1: Read and validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ValidationException("Excel file is empty or has no headers");
            }
            
            // Create mapping of column names to their indices
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = cell.getStringCellValue().trim();
                    columnIndexMap.put(headerName, i);
                }
            }
            
            // Step 2: Validate that all required columns exist
            for (String requiredColumn : requiredColumns) {
                if (!columnIndexMap.containsKey(requiredColumn)) {
                    throw new ValidationException("Required column not found: " + requiredColumn);
                }
            }
            
            // Step 3: Process data rows
            int totalRows = sheet.getLastRowNum();
            int validRows = 0;
            int invalidRows = 0;
            boolean hasErrors = false;
            
            // Create cell style for highlighting invalid cells
            CellStyle yellowStyle = workbook.createCellStyle();
            yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Process each data row (skip header row)
            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue; // Skip empty rows
                
                // Validate the current row
                List<String> errors = validateRow(row, columnIndexMap, requiredColumns);
                
                if (errors.isEmpty()) {
                    validRows++;
                } else {
                    invalidRows++;
                    hasErrors = true;
                    
                    // Highlight invalid cells in yellow
                    for (String error : errors) {
                        String[] parts = error.split(":");
                        if (parts.length > 1) {
                            String columnName = parts[0].trim();
                            Integer colIndex = columnIndexMap.get(columnName);
                            if (colIndex != null) {
                                Cell cell = row.getCell(colIndex);
                                if (cell != null) {
                                    cell.setCellStyle(yellowStyle);
                                }
                            }
                        }
                    }
                }
            }
            
            // Step 4: Add ErrorDetails column if errors were found
            if (hasErrors) {
                // Add ErrorDetails header
                Cell errorHeaderCell = headerRow.createCell(headerRow.getLastCellNum());
                errorHeaderCell.setCellValue("ErrorDetails");
                
                // Add error details for each row
                for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    
                    // Re-validate to get error details
                    List<String> errors = validateRow(row, columnIndexMap, requiredColumns);
                    Cell errorCell = row.createCell(headerRow.getLastCellNum());
                    
                    if (!errors.isEmpty()) {
                        errorCell.setCellValue(String.join(", ", errors));
                    } else {
                        errorCell.setCellValue(""); // Empty for valid rows
                    }
                }
            }
            
            // Step 5: Prepare result
            byte[] processedData = null;
            if (hasErrors) {
                // Only create byte array if there were errors
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                processedData = outputStream.toByteArray();
            }
            
            return new ValidationResult(totalRows, validRows, invalidRows, processedData);
        }
    }
    
    /**
     * Validates CSV files using OpenCSV.
     * 
     * Process:
     * 1. Read CSV headers
     * 2. Parse headers and create column mapping
     * 3. Validate required columns exist
     * 4. Process each row and validate data
     * 5. Add ErrorDetails column with error messages
     * 6. Preserve all original data
     * 
     * @param inputStream Stream containing the CSV file
     * @return ValidationResult with statistics and processed data
     * @throws IOException If file reading fails
     * @throws CsvValidationException If CSV parsing fails
     */
    private ValidationResult validateCsvFile(InputStream inputStream) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            // Step 1: Read and validate headers
            String[] headers = reader.readNext();
            if (headers == null) {
                throw new ValidationException("CSV file is empty or has no headers");
            }
            
            List<String> requiredColumns = Arrays.asList(requiredColumnsConfig.split(","));
            Map<String, Integer> columnIndexMap = new HashMap<>();
            
            // Create mapping of column names to their indices
            for (int i = 0; i < headers.length; i++) {
                columnIndexMap.put(headers[i].trim(), i);
            }
            
            // Step 2: Validate that all required columns exist
            for (String requiredColumn : requiredColumns) {
                if (!columnIndexMap.containsKey(requiredColumn)) {
                    throw new ValidationException("Required column not found: " + requiredColumn);
                }
            }
            
            // Step 3: Process data rows
            List<String[]> allRows = new ArrayList<>();
            allRows.add(headers); // Add headers back to the list
            
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            boolean hasErrors = false;
            
            // Process each data row
            String[] row;
            while ((row = reader.readNext()) != null) {
                totalRows++;
                List<String> errors = validateCsvRow(row, columnIndexMap, requiredColumns);
                
                if (errors.isEmpty()) {
                    validRows++;
                } else {
                    invalidRows++;
                    hasErrors = true;
                }
                
                // Always add the row (preserve all data)
                allRows.add(row);
            }
            
            // Step 4: Add ErrorDetails column if errors were found
            if (hasErrors) {
                // Add ErrorDetails header
                String[] newHeaders = Arrays.copyOf(headers, headers.length + 1);
                newHeaders[headers.length] = "ErrorDetails";
                allRows.set(0, newHeaders);
                
                // Add error details for each data row
                for (int i = 1; i < allRows.size(); i++) {
                    String[] originalRow = allRows.get(i);
                    List<String> errors = validateCsvRow(originalRow, columnIndexMap, requiredColumns);
                    
                    String[] newRow = Arrays.copyOf(originalRow, originalRow.length + 1);
                    if (!errors.isEmpty()) {
                        newRow[originalRow.length] = String.join(", ", errors);
                    } else {
                        newRow[originalRow.length] = ""; // Empty for valid rows
                    }
                    allRows.set(i, newRow);
                }
            }
            
            // Step 5: Prepare result
            byte[] processedData = null;
            if (hasErrors) {
                // Only create byte array if there were errors
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
                    writer.writeAll(allRows);
                }
                processedData = outputStream.toByteArray();
            }
            
            return new ValidationResult(totalRows, validRows, invalidRows, processedData);
        }
    }
    
    /**
     * Validates a single row in an Excel file.
     * 
     * Validation Rules:
     * - All required fields must be non-empty
     * - ID field must be numeric
     * - Date field must be in yyyy-MM-dd format
     * - SellType field must be non-empty
     * - Other required fields must be non-empty
     * 
     * @param row Excel row to validate
     * @param columnIndexMap Mapping of column names to indices
     * @param requiredColumns List of required column names
     * @return List of error messages (empty if no errors)
     */
    private List<String> validateRow(Row row, Map<String, Integer> columnIndexMap, List<String> requiredColumns) {
        List<String> errors = new ArrayList<>();
        
        // Validate each required column
        for (String columnName : requiredColumns) {
            Integer colIndex = columnIndexMap.get(columnName);
            if (colIndex == null) continue; // Skip if column not found
            
            Cell cell = row.getCell(colIndex);
            String value = getCellValueAsString(cell);
            
            // Check if field is empty
            if (value == null || value.trim().isEmpty()) {
                errors.add(columnName + ": Required field is empty");
                continue;
            }
            
            // Apply specific validation rules based on column type
            switch (columnName.toLowerCase()) {
                case "id":
                    // ID must be numeric
                    if (!NUMERIC_PATTERN.matcher(value.trim()).matches()) {
                        errors.add(columnName + ": Must be numeric");
                    }
                    break;
                case "date":
                    // Date must be in yyyy-MM-dd format
                    try {
                        LocalDate.parse(value.trim(), DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add(columnName + ": Invalid date format (expected yyyy-MM-dd)");
                    }
                    break;
                case "selltype":
                    // SellType must not be empty (already checked above, but double-check)
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Sell type is required");
                    }
                    break;
                default:
                    // For other required fields, just check if not empty (already checked above)
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Required field is empty");
                    }
                    break;
            }
        }
        
        return errors;
    }
    
    /**
     * Validates a single row in a CSV file.
     * 
     * Validation Rules (same as Excel):
     * - All required fields must be non-empty
     * - ID field must be numeric
     * - Date field must be in yyyy-MM-dd format
     * - SellType field must be non-empty
     * - Other required fields must be non-empty
     * 
     * @param row CSV row (string array) to validate
     * @param columnIndexMap Mapping of column names to indices
     * @param requiredColumns List of required column names
     * @return List of error messages (empty if no errors)
     */
    private List<String> validateCsvRow(String[] row, Map<String, Integer> columnIndexMap, List<String> requiredColumns) {
        List<String> errors = new ArrayList<>();
        
        // Validate each required column
        for (String columnName : requiredColumns) {
            Integer colIndex = columnIndexMap.get(columnName);
            if (colIndex == null || colIndex >= row.length) continue; // Skip if column not found or out of bounds
            
            String value = row[colIndex];
            
            // Check if field is empty
            if (value == null || value.trim().isEmpty()) {
                errors.add(columnName + ": Required field is empty");
                continue;
            }
            
            // Apply specific validation rules based on column type (same as Excel)
            switch (columnName.toLowerCase()) {
                case "id":
                    // ID must be numeric
                    if (!NUMERIC_PATTERN.matcher(value.trim()).matches()) {
                        errors.add(columnName + ": Must be numeric");
                    }
                    break;
                case "date":
                    // Date must be in yyyy-MM-dd format
                    try {
                        LocalDate.parse(value.trim(), DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add(columnName + ": Invalid date format (expected yyyy-MM-dd)");
                    }
                    break;
                case "selltype":
                    // SellType must not be empty (already checked above, but double-check)
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Sell type is required");
                    }
                    break;
                default:
                    // For other required fields, just check if not empty (already checked above)
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Required field is empty");
                    }
                    break;
            }
        }
        
        return errors;
    }
    
    /**
     * Converts an Excel cell value to a string representation.
     * Handles different cell types (string, numeric, boolean, formula, etc.).
     * 
     * @param cell Excel cell to convert
     * @return String representation of the cell value, or null if cell is null
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Convert date to yyyy-MM-dd format
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                } else {
                    // Convert numeric to string (avoid decimal places for integers)
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula(); // Get the formula text
            default:
                return null; // Handle other cell types as null
        }
    }
    
    /**
     * Generates a unique output filename with timestamp.
     * Format: originalName_validated_YYYYMMDD_HHMMSS.extension
     * 
     * @param originalFilename Original filename
     * @param extension File extension
     * @return Generated filename with timestamp
     */
    private String generateOutputFilename(String originalFilename, String extension) {
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return baseName + "_validated_" + timestamp + "." + extension;
    }
    
    /**
     * Internal class to hold validation results.
     * Contains statistics and processed data (if errors were found).
     */
    private static class ValidationResult {
        private final int totalRows;      // Total number of data rows processed
        private final int validRows;      // Number of rows with no validation errors
        private final int invalidRows;    // Number of rows with validation errors
        private final byte[] processedData; // Modified file data (null if no errors)
        
        public ValidationResult(int totalRows, int validRows, int invalidRows, byte[] processedData) {
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.invalidRows = invalidRows;
            this.processedData = processedData;
        }
        
        // Getters
        public int getTotalRows() { return totalRows; }
        public int getValidRows() { return validRows; }
        public int getInvalidRows() { return invalidRows; }
        public byte[] getProcessedData() { return processedData; }
    }
} 