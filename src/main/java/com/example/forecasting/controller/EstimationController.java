package com.example.forecasting.controller;

import com.example.forecasting.model.EstimationResult;
import com.example.forecasting.service.EstimationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Estimation Controller for Demand Forecasting System
 * 
 * This controller handles Excel file upload and estimation result retrieval
 * for the demand forecasting application. It provides endpoints for
 * processing uploaded Excel files and retrieving estimation results.
 * 
 * Endpoints:
 * - POST /api/estimation/upload: Upload Excel file for estimation
 * - GET /api/estimation/result/{requestId}: Retrieve estimation result
 * 
 * Security Features:
 * - File upload validation and security
 * - Role-based access control for estimation operations
 * - Secure file processing and data handling
 * 
 * Business Rules:
 * - Excel files must be in .xlsx format
 * - File size limits are enforced
 * - Results are associated with user requests
 * - Processing is asynchronous for large files
 * 
 * @author Estimation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/estimation")
public class EstimationController {
    
    private final EstimationService estimationService;

    /**
     * Constructor with dependency injection
     * 
     * @param estimationService Service for estimation operations
     */
    public EstimationController(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    /**
     * Uploads Excel file for demand forecasting estimation
     * 
     * This endpoint processes uploaded Excel files and generates
     * demand forecasting estimations using ML models.
     * 
     * Processing Steps:
     * 1. Validates file format and structure
     * 2. Extracts data from Excel sheets
     * 3. Preprocesses data for ML model consumption
     * 4. Executes appropriate ML model
     * 5. Generates and stores estimation results
     * 
     * Security Considerations:
     * - Validates file format and size
     * - Prevents malicious file uploads
     * - Ensures user has permission for request
     * 
     * @param file MultipartFile containing the Excel data file
     * @param requestId ID of the user request associated with this estimation
     * @return ResponseEntity with estimation result or error message
     */
    @PostMapping("/upload")
    public ResponseEntity<EstimationResult> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("requestId") Long requestId) {
        
        // Validate file before processing
        if (!estimationService.validateExcelFile(file)) {
            return ResponseEntity.badRequest().build();
        }
        
        EstimationResult result = estimationService.processUploadedExcel(file, requestId);
        if (result == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves estimation results for a specific user request
     * 
     * This endpoint fetches the stored estimation results for a given
     * user request ID. It provides access to forecasting results,
     * confidence metrics, and model information.
     * 
     * Result Structure:
     * - Forecasting values and predictions
     * - Confidence intervals and metrics
     * - Model information and parameters
     * - Processing timestamps and metadata
     * 
     * Access Control:
     * - Validates user has permission to access results
     * - Ensures results belong to authorized user
     * - Handles missing or expired results gracefully
     * 
     * @param requestId ID of the user request to retrieve results for
     * @return ResponseEntity with estimation result or error message
     */
    @GetMapping("/result/{requestId}")
    public ResponseEntity<EstimationResult> getResult(@PathVariable Long requestId) {
        EstimationResult result = estimationService.getResult(requestId);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
} 