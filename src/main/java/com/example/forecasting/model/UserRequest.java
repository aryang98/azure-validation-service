package com.example.forecasting.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * User Request Entity for Demand Forecasting System
 * 
 * This entity represents a user's request for demand forecasting estimation.
 * It stores the user input parameters, selected template, and request status
 * throughout the estimation workflow.
 * 
 * Database Schema:
 * - user_requests table with request tracking information
 * - Foreign key relationship with users and templates tables
 * - Status tracking for request lifecycle management
 * - Audit timestamps for compliance and debugging
 * 
 * Business Rules:
 * - Each request is associated with a specific user
 * - Template selection is based on user input parameters
 * - Status tracks request progress (PENDING, PROCESSING, COMPLETED, FAILED)
 * - Input parameters are stored as JSON for flexibility
 * 
 * Workflow States:
 * - PENDING: Request created, waiting for template selection
 * - PROCESSING: Template selected, estimation in progress
 * - COMPLETED: Estimation completed successfully
 * - FAILED: Estimation failed due to errors
 * 
 * @author Data Model Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "user_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    
    /**
     * Primary key for the user request entity
     * Auto-generated using identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User input parameters stored as JSON string
     * Contains industry type, data characteristics, and model preferences
     * Example: {"industry": "retail", "dataVolume": "large", "model": "timeSeries"}
     */
    @Column(nullable = false, length = 1000)
    private String inputParams;

    /**
     * Selected template for this request
     * Many-to-one relationship with Template entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    /**
     * Current status of the request
     * Tracks the progress through the estimation workflow
     */
    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    /**
     * Timestamp when request was created
     * Automatically set on entity creation
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when request was last updated
     * Automatically updated on entity modification
     */
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback for entity creation
     * Sets creation and update timestamps
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    /**
     * JPA lifecycle callback for entity updates
     * Updates the modification timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 