package com.example.filevalidation.service.impl;

import com.example.filevalidation.exception.ValidationRuleException;
import com.example.filevalidation.model.ValidationResult;
import com.example.filevalidation.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * Validation Service Implementation
 * 
 * This class implements the ValidationService interface and provides
 * comprehensive validation logic for Excel files with various data types.
 * 
 * Features:
 * - Row-level validation with detailed error tracking
 * - Email validation using regex patterns
 * - Date validation with configurable formats
 * - Name validation for alphabetic characters only
 * - ID validation for alphanumeric characters
 * - Optional column handling
 * - Error column addition to Excel files
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s]+$"
    );
    
    private static final Pattern ID_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]+$"
    );

    // Default date format
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    // Required columns for validation
    private static final List<String> REQUIRED_COLUMNS = Arrays.asList(
        "Email", "Date", "Name", "ID"
    );

    // Optional columns (not validated)
    private static final List<String> OPTIONAL_COLUMNS = Arrays.asList(
        "Notes", "Comments", "Description"
    );

    @Value("${file.validation.error-column-name:Validation_Errors}")
    private String errorColumnName;

    @Value("${file.validation.max-rows:10000}")
    private int maxRows;

    private final Validator validator;

    public ValidationServiceImpl() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Override
    public ValidationResult validateFile(MultipartFile file) {
        log.info("Starting file validation for file: {}", file.getOriginalFilename());
        
        try {
            // Validate file format
            validateFileFormat(file);
            
            // Read Excel file
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ValidationRuleException("Excel file must have a header row");
            }
            
            Map<String, Integer> columnMap = createColumnMap(headerRow);
            validateRequiredColumns(columnMap);
            
            // Process rows and collect errors
            List<ValidationResult.ValidationError> allErrors = new ArrayList<>();
            int rowsProcessed = 0;
            int rowsWithErrors = 0;
            
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                rowsProcessed++;
                List<ValidationResult.ValidationError> rowErrors = validateRow(row, columnMap, rowIndex + 1);
                
                if (!rowErrors.isEmpty()) {
                    rowsWithErrors++;
                    allErrors.addAll(rowErrors);
                }
                
                // Add error column to the row
                addErrorColumnToRow(row, rowErrors);
            }
            
            // Update workbook with error column
            addErrorColumnHeader(workbook, columnMap.size());
            
            // Create error file workbook if there are errors
            Workbook errorWorkbook = null;
            if (rowsWithErrors > 0) {
                errorWorkbook = createErrorFile(workbook, allErrors);
            }
            
            // Create result
            ValidationResult result = ValidationResult.builder()
                .success(rowsWithErrors == 0)
                .status(rowsWithErrors == 0 ? ValidationResult.ValidationStatus.SUCCESS : ValidationResult.ValidationStatus.SUCCESS_WITH_ERRORS)
                .filename(file.getOriginalFilename())
                .rowsProcessed(rowsProcessed)
                .rowsWithErrors(rowsWithErrors)
                .errors(allErrors)
                .build();
            
            // Store workbooks for later use
            result.setProcessedWorkbook(workbook);
            if (errorWorkbook != null) {
                result.setErrorWorkbook(errorWorkbook);
            }
            
            log.info("File validation completed. Rows processed: {}, Rows with errors: {}", 
                    rowsProcessed, rowsWithErrors);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error during file validation: {}", e.getMessage(), e);
            throw new ValidationRuleException("File validation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ValidationResult.ValidationError> validateColumn(String columnName, 
                                                               List<String> columnData, 
                                                               ValidationType validationType) {
        log.debug("Validating column: {} with type: {}", columnName, validationType);
        
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        for (int i = 0; i < columnData.size(); i++) {
            String value = columnData.get(i);
            if (value == null || value.trim().isEmpty()) {
                continue; // Skip empty values for optional columns
            }
            
            boolean isValid = false;
            String errorMessage = "";
            
            switch (validationType) {
                case EMAIL:
                    isValid = isValidEmail(value);
                    errorMessage = "Invalid email format";
                    break;
                case DATE:
                    isValid = isValidDate(value, DEFAULT_DATE_FORMAT);
                    errorMessage = "Invalid date format (expected: " + DEFAULT_DATE_FORMAT + ")";
                    break;
                case NAME:
                    isValid = isValidName(value);
                    errorMessage = "Name must contain only letters and spaces";
                    break;
                case ID:
                    isValid = isValidId(value);
                    errorMessage = "ID must be alphanumeric";
                    break;
                case OPTIONAL:
                    isValid = true; // Optional columns are not validated
                    break;
            }
            
            if (!isValid) {
                errors.add(ValidationResult.ValidationError.builder()
                    .rowNumber(i + 2) // +2 because Excel is 1-indexed and we skip header
                    .columnName(columnName)
                    .errorMessage(errorMessage)
                    .invalidValue(value)
                    .build());
            }
        }
        
        log.debug("Column validation completed. Errors found: {}", errors.size());
        return errors;
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @Override
    public boolean isValidDate(String date, String format) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(date.trim(), formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    @Override
    public boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return ID_PATTERN.matcher(id.trim()).matches();
    }

    @Override
    public List<String> getRequiredColumns() {
        return new ArrayList<>(REQUIRED_COLUMNS);
    }

    @Override
    public List<String> getOptionalColumns() {
        return new ArrayList<>(OPTIONAL_COLUMNS);
    }

    /**
     * Validates the file format
     * 
     * @param file The uploaded file
     * @throws ValidationRuleException if file format is invalid
     */
    private void validateFileFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new ValidationRuleException("Filename cannot be empty");
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList("xlsx", "xls").contains(extension)) {
            throw new ValidationRuleException(filename, "xlsx, xls");
        }
        
        if (file.isEmpty()) {
            throw new ValidationRuleException("Uploaded file is empty");
        }
    }

    /**
     * Creates a map of column names to their indices
     * 
     * @param headerRow The header row of the Excel file
     * @return Map of column names to indices
     */
    private Map<String, Integer> createColumnMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = cell.getStringCellValue().trim();
                if (!columnName.isEmpty()) {
                    columnMap.put(columnName, i);
                }
            }
        }
        
        return columnMap;
    }

    /**
     * Validates that all required columns are present
     * 
     * @param columnMap Map of column names to indices
     * @throws ValidationRuleException if required columns are missing
     */
    private void validateRequiredColumns(Map<String, Integer> columnMap) {
        List<String> missingColumns = new ArrayList<>();
        
        for (String requiredColumn : REQUIRED_COLUMNS) {
            if (!columnMap.containsKey(requiredColumn)) {
                missingColumns.add(requiredColumn);
            }
        }
        
        if (!missingColumns.isEmpty()) {
            throw new ValidationRuleException(
                "Missing required columns: " + String.join(", ", missingColumns)
            );
        }
    }

    /**
     * Validates a single row in the Excel file
     * 
     * @param row The row to validate
     * @param columnMap Map of column names to indices
     * @param rowNumber The row number (1-indexed)
     * @return List of validation errors for the row
     */
    private List<ValidationResult.ValidationError> validateRow(Row row, 
                                                             Map<String, Integer> columnMap, 
                                                             int rowNumber) {
        List<ValidationResult.ValidationError> rowErrors = new ArrayList<>();
        
        // Validate each required column
        for (String columnName : REQUIRED_COLUMNS) {
            Integer columnIndex = columnMap.get(columnName);
            if (columnIndex == null) continue;
            
            Cell cell = row.getCell(columnIndex);
            String value = getCellValueAsString(cell);
            
            ValidationType validationType = getValidationTypeForColumn(columnName);
            if (validationType != ValidationType.OPTIONAL) {
                boolean isValid = validateValue(value, validationType);
                if (!isValid) {
                    rowErrors.add(ValidationResult.ValidationError.builder()
                        .rowNumber(rowNumber)
                        .columnName(columnName)
                        .errorMessage(getErrorMessageForValidationType(validationType))
                        .invalidValue(value)
                        .build());
                }
            }
        }
        
        return rowErrors;
    }

    /**
     * Gets the validation type for a specific column
     * 
     * @param columnName Name of the column
     * @return ValidationType for the column
     */
    private ValidationType getValidationTypeForColumn(String columnName) {
        switch (columnName.toLowerCase()) {
            case "email":
                return ValidationType.EMAIL;
            case "date":
                return ValidationType.DATE;
            case "name":
                return ValidationType.NAME;
            case "id":
                return ValidationType.ID;
            default:
                return ValidationType.OPTIONAL;
        }
    }

    /**
     * Validates a single value against a validation type
     * 
     * @param value The value to validate
     * @param validationType The type of validation to perform
     * @return true if valid, false otherwise
     */
    private boolean validateValue(String value, ValidationType validationType) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        switch (validationType) {
            case EMAIL:
                return isValidEmail(value);
            case DATE:
                return isValidDate(value, DEFAULT_DATE_FORMAT);
            case NAME:
                return isValidName(value);
            case ID:
                return isValidId(value);
            case OPTIONAL:
                return true;
            default:
                return true;
        }
    }

    /**
     * Gets the error message for a validation type
     * 
     * @param validationType The validation type
     * @return Error message
     */
    private String getErrorMessageForValidationType(ValidationType validationType) {
        switch (validationType) {
            case EMAIL:
                return "Invalid email format";
            case DATE:
                return "Invalid date format (expected: " + DEFAULT_DATE_FORMAT + ")";
            case NAME:
                return "Name must contain only letters and spaces";
            case ID:
                return "ID must be alphanumeric";
            default:
                return "Invalid value";
        }
    }

    /**
     * Gets the string value from a cell
     * 
     * @param cell The cell to get the value from
     * @return String value of the cell
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Adds error column header to the workbook
     * 
     * @param workbook The workbook to modify
     * @param lastColumnIndex Index of the last column
     */
    private void addErrorColumnHeader(Workbook workbook, int lastColumnIndex) {
        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        
        Cell errorHeaderCell = headerRow.createCell(lastColumnIndex);
        errorHeaderCell.setCellValue(errorColumnName);
    }

    /**
     * Adds error information to a row
     * 
     * @param row The row to modify
     * @param rowErrors List of errors for the row
     */
    private void addErrorColumnToRow(Row row, List<ValidationResult.ValidationError> rowErrors) {
        int errorColumnIndex = row.getLastCellNum();
        Cell errorCell = row.createCell(errorColumnIndex);
        
        if (!rowErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (int i = 0; i < rowErrors.size(); i++) {
                ValidationResult.ValidationError error = rowErrors.get(i);
                errorMessage.append(error.getColumnName())
                          .append(": ")
                          .append(error.getErrorMessage());
                
                if (i < rowErrors.size() - 1) {
                    errorMessage.append("; ");
                }
            }
            errorCell.setCellValue(errorMessage.toString());
        } else {
            errorCell.setCellValue("Valid");
        }
    }

    /**
     * Creates an error file workbook with only rows that have errors
     * 
     * @param originalWorkbook The original workbook
     * @param allErrors List of all validation errors
     * @return Error workbook
     */
    private Workbook createErrorFile(Workbook originalWorkbook, List<ValidationResult.ValidationError> allErrors) {
        try {
            // Create new workbook for error file
            Workbook errorWorkbook = new XSSFWorkbook();
            Sheet originalSheet = originalWorkbook.getSheetAt(0);
            Sheet errorSheet = errorWorkbook.createSheet("Validation Errors");
            
            // Copy header row
            Row originalHeaderRow = originalSheet.getRow(0);
            Row errorHeaderRow = errorSheet.createRow(0);
            
            for (int i = 0; i < originalHeaderRow.getLastCellNum(); i++) {
                Cell originalCell = originalHeaderRow.getCell(i);
                Cell errorCell = errorHeaderRow.createCell(i);
                
                if (originalCell != null) {
                    errorCell.setCellValue(originalCell.getStringCellValue());
                }
            }
            
            // Add instruction row
            Row instructionRow = errorSheet.createRow(1);
            Cell instructionCell = instructionRow.createCell(0);
            instructionCell.setCellValue("INSTRUCTIONS: Please fix the errors in the highlighted cells and re-upload this file.");
            
            // Get unique row numbers with errors
            Set<Integer> errorRowNumbers = allErrors.stream()
                .map(ValidationResult.ValidationError::getRowNumber)
                .collect(java.util.stream.Collectors.toSet());
            
            // Copy rows with errors (adjust for 1-based to 0-based indexing)
            int errorRowIndex = 2; // Start after header and instruction rows
            for (Integer rowNum : errorRowNumbers) {
                Row originalRow = originalSheet.getRow(rowNum - 1); // Convert to 0-based
                if (originalRow != null) {
                    Row errorRow = errorSheet.createRow(errorRowIndex);
                    
                    // Copy all cells from original row
                    for (int i = 0; i < originalRow.getLastCellNum(); i++) {
                        Cell originalCell = originalRow.getCell(i);
                        Cell errorCell = errorRow.createCell(i);
                        
                        if (originalCell != null) {
                            copyCellValue(originalCell, errorCell);
                        }
                    }
                    
                    errorRowIndex++;
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < errorHeaderRow.getLastCellNum(); i++) {
                errorSheet.autoSizeColumn(i);
            }
            
            log.info("Error file created with {} rows containing errors", errorRowNumbers.size());
            return errorWorkbook;
            
        } catch (Exception e) {
            log.error("Error creating error file: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Copies cell value from source to destination cell
     * 
     * @param source Source cell
     * @param destination Destination cell
     */
    private void copyCellValue(Cell source, Cell destination) {
        switch (source.getCellType()) {
            case STRING:
                destination.setCellValue(source.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(source)) {
                    destination.setCellValue(source.getDateCellValue());
                } else {
                    destination.setCellValue(source.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                destination.setCellValue(source.getBooleanCellValue());
                break;
            case FORMULA:
                destination.setCellValue(source.getCellFormula());
                break;
            default:
                destination.setCellValue("");
        }
    }
} 