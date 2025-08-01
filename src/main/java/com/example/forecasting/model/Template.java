package com.example.forecasting.model;

import lombok.*;
import javax.persistence.*;

/**
 * Template Entity for Demand Forecasting System
 * 
 * This entity represents Excel templates used for demand forecasting.
 * Templates are predefined Excel files with specific structures and formats
 * that users can download, fill with data, and upload for estimation.
 * 
 * Database Schema:
 * - templates table with template metadata
 * - File path references to actual Excel files in resources
 * - Template categorization and description for user selection
 * 
 * Business Rules:
 * - Template names must be unique across the system
 * - File paths must reference valid Excel files in resources
 * - Templates are categorized by industry and complexity
 * - Descriptions help users understand template purpose
 * 
 * Template Categories:
 * - Retail: Sales data, inventory management, seasonal patterns
 * - CPG: Consumer goods, supply chain, market trends
 * - Logistics: Transportation, warehousing, distribution
 * 
 * @author Data Model Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {
    
    /**
     * Primary key for the template entity
     * Auto-generated using identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique template name for identification
     * Must be unique across the system
     * Example: "Retail_Forecast_Template_v1.0"
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Detailed description of the template
     * Explains template purpose, structure, and usage instructions
     */
    private String description;

    /**
     * File path to the template Excel file
     * Relative path to resources directory
     * Example: "templates/retail_forecast_template.xlsx"
     */
    @Column(nullable = false)
    private String filePath;
} 