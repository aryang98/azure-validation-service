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

@Service
public class FileValidationServiceImpl implements FileValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationServiceImpl.class);
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final BlobStorageService blobStorageService;
    private final ValidationMetadataService metadataService;
    
    @Value("${file.validation.required-columns}")
    private String requiredColumnsConfig;
    
    @Value("${file.validation.max-size-mb}")
    private int maxFileSizeMb;
    
    @Value("${azure.blob.sas-expiry-hours}")
    private int sasExpiryHours;
    
    @Autowired
    public FileValidationServiceImpl(BlobStorageService blobStorageService, 
                                   ValidationMetadataService metadataService) {
        this.blobStorageService = blobStorageService;
        this.metadataService = metadataService;
    }
    
    @Override
    public ValidationResponse validateFile(ValidationRequest request) {
        long startTime = System.currentTimeMillis();
        String filename = request.getFilename();
        
        if (!isFileSupported(filename)) {
            throw new UnsupportedFileTypeException(getFileExtension(filename));
        }
        
        // Check file size
        long fileSize = blobStorageService.getFileSize(filename);
        if (fileSize > maxFileSizeMb * 1024 * 1024) {
            throw new ValidationException("File size exceeds maximum allowed size of " + maxFileSizeMb + "MB");
        }
        
        String fileExtension = getFileExtension(filename);
        ValidationResult result;
        
        try (InputStream inputStream = blobStorageService.downloadFile(filename)) {
            if (fileExtension.equalsIgnoreCase("xlsx")) {
                result = validateExcelFile(inputStream);
            } else {
                result = validateCsvFile(inputStream);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // If no errors, return simple success response
            if (result.getInvalidRows() == 0) {
                // Save metadata for successful validation
                ValidationMetadata metadata = new ValidationMetadata(
                        filename,
                        result.getTotalRows(),
                        result.getValidRows(),
                        result.getInvalidRows(),
                        "SUCCESS",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null,
                        null
                );
                metadataService.saveValidationMetadata(metadata);
                
                return new ValidationResponse(
                        "SUCCESS",
                        "File validation completed successfully - no errors found",
                        result.getTotalRows(),
                        executionTime
                );
            } else {
                // Errors found - upload modified file and return download URL
                String outputFilename = generateOutputFilename(filename, fileExtension);
                try (InputStream processedStream = new ByteArrayInputStream(result.getProcessedData())) {
                    blobStorageService.uploadFile(outputFilename, processedStream, result.getProcessedData().length);
                }
                
                // Generate SAS URL
                String downloadUrl = blobStorageService.generateSasUrl(outputFilename, sasExpiryHours);
                
                // Save metadata
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
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error validating file: {}", filename, e);
            
            // Save error metadata
            ValidationMetadata errorMetadata = new ValidationMetadata(
                    filename,
                    0,
                    0,
                    0,
                    "ERROR",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null,
                    e.getMessage()
            );
            metadataService.saveValidationMetadata(errorMetadata);
            
            throw new ValidationException("Failed to validate file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isFileSupported(String filename) {
        String extension = getFileExtension(filename);
        return extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("csv");
    }
    
    @Override
    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    private ValidationResult validateExcelFile(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> requiredColumns = Arrays.asList(requiredColumnsConfig.split(","));
            
            // Read headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ValidationException("Excel file is empty or has no headers");
            }
            
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = cell.getStringCellValue().trim();
                    columnIndexMap.put(headerName, i);
                }
            }
            
            // Validate required columns exist
            for (String requiredColumn : requiredColumns) {
                if (!columnIndexMap.containsKey(requiredColumn)) {
                    throw new ValidationException("Required column not found: " + requiredColumn);
                }
            }
            
            int totalRows = sheet.getLastRowNum();
            int validRows = 0;
            int invalidRows = 0;
            boolean hasErrors = false;
            
            // Process each row
            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                List<String> errors = validateRow(row, columnIndexMap, requiredColumns);
                
                if (errors.isEmpty()) {
                    validRows++;
                } else {
                    invalidRows++;
                    hasErrors = true;
                    // Add error details to the last column
                    Cell errorCell = row.createCell(headerRow.getLastCellNum());
                    errorCell.setCellValue(String.join("; ", errors));
                    
                    // Highlight invalid cells in yellow
                    CellStyle yellowStyle = workbook.createCellStyle();
                    yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    
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
            
            // Only add ErrorDetails header if there were errors
            if (hasErrors) {
                Cell errorHeaderCell = headerRow.createCell(headerRow.getLastCellNum());
                errorHeaderCell.setCellValue("ErrorDetails");
            }
            
            // Write to byte array only if there were errors
            byte[] processedData = null;
            if (hasErrors) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                processedData = outputStream.toByteArray();
            }
            
            return new ValidationResult(totalRows, validRows, invalidRows, processedData);
        }
    }
    
    private ValidationResult validateCsvFile(InputStream inputStream) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                throw new ValidationException("CSV file is empty or has no headers");
            }
            
            List<String> requiredColumns = Arrays.asList(requiredColumnsConfig.split(","));
            Map<String, Integer> columnIndexMap = new HashMap<>();
            
            for (int i = 0; i < headers.length; i++) {
                columnIndexMap.put(headers[i].trim(), i);
            }
            
            // Validate required columns exist
            for (String requiredColumn : requiredColumns) {
                if (!columnIndexMap.containsKey(requiredColumn)) {
                    throw new ValidationException("Required column not found: " + requiredColumn);
                }
            }
            
            List<String[]> allRows = new ArrayList<>();
            allRows.add(headers); // Add headers back
            
            int totalRows = 0;
            int validRows = 0;
            int invalidRows = 0;
            boolean hasErrors = false;
            
            String[] row;
            while ((row = reader.readNext()) != null) {
                totalRows++;
                List<String> errors = validateCsvRow(row, columnIndexMap, requiredColumns);
                
                if (errors.isEmpty()) {
                    validRows++;
                    allRows.add(row);
                } else {
                    invalidRows++;
                    hasErrors = true;
                    // Add error details to the last column
                    String[] newRow = Arrays.copyOf(row, row.length + 1);
                    newRow[row.length] = String.join("; ", errors);
                    allRows.add(newRow);
                }
            }
            
            // Only add ErrorDetails header if there were errors
            if (hasErrors) {
                String[] newHeaders = Arrays.copyOf(headers, headers.length + 1);
                newHeaders[headers.length] = "ErrorDetails";
                allRows.set(0, newHeaders);
            }
            
            // Write to byte array only if there were errors
            byte[] processedData = null;
            if (hasErrors) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
                    writer.writeAll(allRows);
                }
                processedData = outputStream.toByteArray();
            }
            
            return new ValidationResult(totalRows, validRows, invalidRows, processedData);
        }
    }
    
    private List<String> validateRow(Row row, Map<String, Integer> columnIndexMap, List<String> requiredColumns) {
        List<String> errors = new ArrayList<>();
        
        for (String columnName : requiredColumns) {
            Integer colIndex = columnIndexMap.get(columnName);
            if (colIndex == null) continue;
            
            Cell cell = row.getCell(colIndex);
            String value = getCellValueAsString(cell);
            
            if (value == null || value.trim().isEmpty()) {
                errors.add(columnName + ": Required field is empty");
                continue;
            }
            
            // Validate based on column type
            switch (columnName.toLowerCase()) {
                case "id":
                    if (!NUMERIC_PATTERN.matcher(value.trim()).matches()) {
                        errors.add(columnName + ": Must be numeric");
                    }
                    break;
                case "date":
                    try {
                        LocalDate.parse(value.trim(), DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add(columnName + ": Invalid date format (expected yyyy-MM-dd)");
                    }
                    break;
                case "selltype":
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Sell type is required");
                    }
                    break;
                default:
                    // For other required fields, just check if not empty
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Required field is empty");
                    }
                    break;
            }
        }
        
        return errors;
    }
    
    private List<String> validateCsvRow(String[] row, Map<String, Integer> columnIndexMap, List<String> requiredColumns) {
        List<String> errors = new ArrayList<>();
        
        for (String columnName : requiredColumns) {
            Integer colIndex = columnIndexMap.get(columnName);
            if (colIndex == null || colIndex >= row.length) continue;
            
            String value = row[colIndex];
            
            if (value == null || value.trim().isEmpty()) {
                errors.add(columnName + ": Required field is empty");
                continue;
            }
            
            // Validate based on column type
            switch (columnName.toLowerCase()) {
                case "id":
                    if (!NUMERIC_PATTERN.matcher(value.trim()).matches()) {
                        errors.add(columnName + ": Must be numeric");
                    }
                    break;
                case "date":
                    try {
                        LocalDate.parse(value.trim(), DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add(columnName + ": Invalid date format (expected yyyy-MM-dd)");
                    }
                    break;
                case "selltype":
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Sell type is required");
                    }
                    break;
                default:
                    // For other required fields, just check if not empty
                    if (value.trim().isEmpty()) {
                        errors.add(columnName + ": Required field is empty");
                    }
                    break;
            }
        }
        
        return errors;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private String generateOutputFilename(String originalFilename, String extension) {
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return baseName + "_validated_" + timestamp + "." + extension;
    }
    
    private static class ValidationResult {
        private final int totalRows;
        private final int validRows;
        private final int invalidRows;
        private final byte[] processedData; // null if no errors
        
        public ValidationResult(int totalRows, int validRows, int invalidRows, byte[] processedData) {
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.invalidRows = invalidRows;
            this.processedData = processedData;
        }
        
        public int getTotalRows() { return totalRows; }
        public int getValidRows() { return validRows; }
        public int getInvalidRows() { return invalidRows; }
        public byte[] getProcessedData() { return processedData; }
    }
} 