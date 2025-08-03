package com.example.filevalidation.service;

import com.example.filevalidation.exception.FileValidationException;
import com.example.filevalidation.model.SalesRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Excel file processing
 * Handles reading and writing Excel files with validation
 */
@Service
@Slf4j
public class ExcelProcessingService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Read Excel file and extract sales records
     * @param inputStream The input stream containing the Excel file
     * @return List of sales records
     */
    public List<SalesRecord> readExcelFile(InputStream inputStream) {
        List<SalesRecord> salesRecords = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Read first sheet
            
            // Skip header row and process data rows
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    SalesRecord record = extractSalesRecord(row, rowIndex);
                    if (record != null) {
                        salesRecords.add(record);
                    }
                }
            }
            
            log.info("Successfully read {} sales records from Excel file", salesRecords.size());
            return salesRecords;
            
        } catch (Exception e) {
            log.error("Error reading Excel file", e);
            throw new FileValidationException("Failed to read Excel file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Write sales records to Excel file with error messages
     * @param salesRecords List of sales records with validation results
     * @return Byte array containing the Excel file
     */
    public byte[] writeExcelFileWithErrors(List<SalesRecord> salesRecords) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Sales Data");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "ID");
            createHeaderCell(headerRow, 1, "Sale Date");
            createHeaderCell(headerRow, 2, "Sale Mode");
            createHeaderCell(headerRow, 3, "MIN_SALE");
            createHeaderCell(headerRow, 4, "Error Message");
            
            // Create data rows
            for (int i = 0; i < salesRecords.size(); i++) {
                SalesRecord record = salesRecords.get(i);
                Row dataRow = sheet.createRow(i + 1);
                
                createCell(dataRow, 0, record.getId());
                createCell(dataRow, 1, record.getSaleDate() != null ? 
                    record.getSaleDate().format(DATE_FORMATTER) : "");
                createCell(dataRow, 2, record.getSaleMode());
                createCell(dataRow, 3, record.getMinSale() != null ? 
                    record.getMinSale().toString() : "");
                createCell(dataRow, 4, record.getErrorMessage() != null ? 
                    record.getErrorMessage() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            log.info("Successfully created Excel file with {} records", salesRecords.size());
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Error writing Excel file", e);
            throw new FileValidationException("Failed to write Excel file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract sales record from Excel row
     * @param row The Excel row
     * @param rowIndex The row index for error reporting
     * @return SalesRecord object
     */
    private SalesRecord extractSalesRecord(Row row, int rowIndex) {
        try {
            String id = getCellValueAsString(row.getCell(0));
            LocalDate saleDate = getCellValueAsDate(row.getCell(1));
            String saleMode = getCellValueAsString(row.getCell(2));
            BigDecimal minSale = getCellValueAsBigDecimal(row.getCell(3));
            
            return new SalesRecord(id, saleDate, saleMode, minSale, null);
            
        } catch (Exception e) {
            log.warn("Error extracting data from row {}: {}", rowIndex, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    /**
     * Get cell value as date
     */
    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                return LocalDate.parse(cell.getStringCellValue(), DATE_FORMATTER);
            }
        } catch (Exception e) {
            log.warn("Error parsing date from cell: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get cell value as BigDecimal
     */
    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                return new BigDecimal(cell.getStringCellValue());
            }
        } catch (Exception e) {
            log.warn("Error parsing BigDecimal from cell: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Create header cell with styling
     */
    private void createHeaderCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        
        CellStyle headerStyle = row.getSheet().getWorkbook().createCellStyle();
        Font headerFont = row.getSheet().getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        cell.setCellStyle(headerStyle);
    }
    
    /**
     * Create data cell
     */
    private void createCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "");
    }
} 