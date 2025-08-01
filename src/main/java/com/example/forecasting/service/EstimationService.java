package com.example.forecasting.service;

import com.example.forecasting.model.EstimationResult;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

/**
 * Estimation Service Interface for Demand Forecasting System
 * 
 * This interface defines the contract for demand forecasting estimation
 * operations including Excel processing, ML model execution, and result management.
 * 
 * Business Operations:
 * - Excel file upload and parsing
 * - Data validation and preprocessing
 * - ML model execution and estimation
 * - Result storage and retrieval
 * 
 * Estimation Process:
 * 1. Excel file upload and validation
 * 2. Data extraction and preprocessing
 * 3. ML model selection and execution
 * 4. Result generation and storage
 * 5. Response delivery to client
 * 
 * @author Estimation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface EstimationService {
    
    /**
     * Processes uploaded Excel file and generates demand forecasting estimation
     * 
     * This method handles the complete estimation workflow from file upload
     * to result generation. It validates the uploaded file, extracts data,
     * runs ML models, and stores the results for later retrieval.
     * 
     * @param file MultipartFile containing the Excel data file
     * @param requestId ID of the user request associated with this estimation
     * @return EstimationResult containing the forecasting results, or null if processing fails
     */
    EstimationResult processUploadedExcel(MultipartFile file, Long requestId);
    
    /**
     * Retrieves estimation results for a specific user request
     * 
     * @param requestId ID of the user request to retrieve results for
     * @return EstimationResult containing the forecasting results, or null if not found
     */
    EstimationResult getResult(Long requestId);
    
    /**
     * Retrieves all estimation results
     * 
     * @return List of all estimation results
     */
    List<EstimationResult> getAllResults();
    
    /**
     * Finds an estimation result by its ID
     * 
     * @param resultId Result ID to search for
     * @return Optional containing the result if found
     */
    Optional<EstimationResult> findById(Long resultId);
    
    /**
     * Creates a new estimation result
     * 
     * @param result EstimationResult object to create
     * @return Created result with generated ID
     */
    EstimationResult createResult(EstimationResult result);
    
    /**
     * Updates an existing estimation result
     * 
     * @param resultId ID of the result to update
     * @param result Updated result information
     * @return Updated result object
     */
    EstimationResult updateResult(Long resultId, EstimationResult result);
    
    /**
     * Deletes an estimation result
     * 
     * @param resultId ID of the result to delete
     */
    void deleteResult(Long resultId);
    
    /**
     * Validates Excel file format and structure
     * 
     * @param file Excel file to validate
     * @return true if file is valid, false otherwise
     */
    boolean validateExcelFile(MultipartFile file);
    
    /**
     * Extracts data from Excel file for processing
     * 
     * @param file Excel file to extract data from
     * @return Extracted data as structured format
     */
    Object extractDataFromExcel(MultipartFile file);
} 