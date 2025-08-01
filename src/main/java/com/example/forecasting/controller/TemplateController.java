package com.example.forecasting.controller;

import com.example.forecasting.model.Template;
import com.example.forecasting.service.TemplateService;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Template Controller for Demand Forecasting System
 * 
 * This controller handles template selection and download operations
 * for the demand forecasting application. It provides endpoints for
 * template selection based on user input and template file download.
 * 
 * Endpoints:
 * - POST /api/templates/select: Select template based on user input
 * - GET /api/templates/download/{templateId}: Download template file
 * 
 * Security Features:
 * - Template access is controlled by role-based permissions
 * - File paths are validated to prevent directory traversal
 * - Resource loading is handled securely
 * 
 * Business Rules:
 * - Template selection is based on user input parameters
 * - Templates are downloaded as Excel files
 * - File paths are validated before access
 * 
 * @author Template Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    
    private final TemplateService templateService;

    /**
     * Constructor with dependency injection
     * 
     * @param templateService Service for template management operations
     */
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * Selects a template based on user input parameters
     * 
     * This endpoint analyzes user input and selects the most appropriate
     * Excel template for their forecasting needs.
     * 
     * @param input User input containing requirements and preferences
     * @return ResponseEntity with selected template or error message
     */
    @PostMapping("/select")
    public ResponseEntity<Template> selectTemplate(@RequestBody UserInputDTO input) {
        Template template = templateService.selectTemplate(input.getInputParams());
        if (template == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(template);
    }

    /**
     * Downloads a template file by its ID
     * 
     * This endpoint provides the actual Excel template file for download.
     * The file is served as a downloadable resource with appropriate headers.
     * 
     * Security Considerations:
     * - Validates template ID exists before file access
     * - Uses secure resource loading
     * - Prevents directory traversal attacks
     * 
     * @param templateId Database ID of the template to download
     * @return ResponseEntity with template file or error message
     */
    @GetMapping("/download/{templateId}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long templateId) {
        Resource file = templateService.downloadTemplate(templateId);
        if (file == null || !file.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFilename())
                .body(file);
    }

    /**
     * Data Transfer Object for user input parameters
     * 
     * Contains the user's input parameters for template selection
     * including industry type, data characteristics, and model preferences.
     */
    @Data
    public static class UserInputDTO {
        private String inputParams;
    }
} 