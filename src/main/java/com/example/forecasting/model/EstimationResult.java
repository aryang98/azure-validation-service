package com.example.forecasting.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Estimation Result Entity for Demand Forecasting System
 * 
 * This entity stores the results of demand forecasting estimations.
 * It maintains a one-to-one relationship with UserRequest and contains
 * the forecasting results, confidence metrics, and model information.
 * 
 * Database Schema:
 * - estimation_results table with result data
 * - One-to-one relationship with user_requests table
 * - Large object storage for detailed result data
 * - Audit timestamps for result tracking
 * 
 * Business Rules:
 * - Each result is associated with exactly one user request
 * - Result data is stored as JSON for flexibility
 * - Results include forecasting values and confidence metrics
 * - Model information is preserved for analysis
 * 
 * Result Data Structure:
 * - Forecasting values and predictions
 * - Confidence intervals and accuracy metrics
 * - Model parameters and configuration
 * - Processing metadata and timestamps
 * 
 * @author Data Model Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "estimation_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimationResult {
    
    /**
     * Primary key for the estimation result entity
     * Auto-generated using identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Associated user request for this result
     * One-to-one relationship with UserRequest entity
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_request_id")
    private UserRequest userRequest;

    /**
     * Detailed result data stored as JSON string
     * Contains forecasting values, confidence metrics, and model information
     * Example: {"predictions": [...], "confidence": 0.85, "model": "timeSeries"}
     */
    @Lob
    private String resultData;

    /**
     * Timestamp when result was created
     * Automatically set on entity creation
     */
    private LocalDateTime createdAt;

    /**
     * JPA lifecycle callback for entity creation
     * Sets creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 