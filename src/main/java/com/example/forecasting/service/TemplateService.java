package com.example.forecasting.service;

import com.example.forecasting.model.Template;
import org.springframework.core.io.Resource;
import java.util.List;
import java.util.Optional;

/**
 * Template Service Interface for Demand Forecasting System
 * 
 * This interface defines the contract for template management operations
 * including template selection, download, and metadata management.
 * 
 * Business Operations:
 * - Template selection based on user requirements
 * - Template file download and delivery
 * - Template metadata management
 * - Template validation and versioning
 * 
 * Template Selection Logic:
 * - Analyzes user input parameters
 * - Matches requirements to available templates
 * - Returns appropriate template for user needs
 * 
 * @author Template Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface TemplateService {
    
    /**
     * Selects an appropriate template based on user input parameters
     * 
     * This method analyzes the user's input parameters and selects
     * the most suitable Excel template for their forecasting needs.
     * 
     * @param inputParams JSON string containing user input parameters
     * @return Selected template or null if no suitable template found
     */
    Template selectTemplate(String inputParams);
    
    /**
     * Downloads a template file by its ID
     * 
     * @param templateId Database ID of the template to download
     * @return Resource object containing the template file, or null if not found
     */
    Resource downloadTemplate(Long templateId);
    
    /**
     * Retrieves all available templates
     * 
     * @return List of all templates in the system
     */
    List<Template> getAllTemplates();
    
    /**
     * Finds a template by its ID
     * 
     * @param templateId Template ID to search for
     * @return Optional containing the template if found
     */
    Optional<Template> findById(Long templateId);
    
    /**
     * Finds a template by its name
     * 
     * @param name Template name to search for
     * @return Optional containing the template if found
     */
    Optional<Template> findByName(String name);
    
    /**
     * Creates a new template
     * 
     * @param template Template object to create
     * @return Created template with generated ID
     */
    Template createTemplate(Template template);
    
    /**
     * Updates an existing template
     * 
     * @param templateId ID of the template to update
     * @param template Updated template information
     * @return Updated template object
     */
    Template updateTemplate(Long templateId, Template template);
    
    /**
     * Deletes a template
     * 
     * @param templateId ID of the template to delete
     */
    void deleteTemplate(Long templateId);
} 