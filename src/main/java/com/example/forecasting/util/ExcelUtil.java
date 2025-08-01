package com.example.forecasting.util;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class ExcelUtil {
    public static Workbook loadTemplate(String filePath) throws Exception {
        Resource resource = new ClassPathResource(filePath);
        try (InputStream is = resource.getInputStream()) {
            return new XSSFWorkbook(is);
        }
    }

    public static void validateAndParse(Workbook workbook) {
        // TODO: Implement validation and parsing logic for uploaded Excel
    }

    public static Workbook generateTemplate() {
        // TODO: Implement template generation logic if needed
        return new XSSFWorkbook();
    }
} 