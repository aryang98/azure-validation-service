import React, { useState } from 'react';
import axios from 'axios';
import './FileUpload.css';

/**
 * File Upload Component
 * 
 * This React component provides a user interface for uploading Excel files
 * to the Spring Boot backend for validation. It includes:
 * - File selection and upload
 * - Progress tracking
 * - Validation result display
 * - Error handling
 * - Download links for processed files
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
const FileUpload = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [validationResult, setValidationResult] = useState(null);
    const [error, setError] = useState(null);
    const [uploadProgress, setUploadProgress] = useState(0);

    /**
     * Handles file selection
     * 
     * @param {Event} event - File input change event
     */
    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        if (file) {
            // Validate file type
            const allowedTypes = [
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                'application/vnd.ms-excel'
            ];
            
            if (!allowedTypes.includes(file.type)) {
                setError('Please select a valid Excel file (.xlsx or .xls)');
                setSelectedFile(null);
                return;
            }

            // Validate file size (10MB limit)
            const maxSize = 10 * 1024 * 1024; // 10MB
            if (file.size > maxSize) {
                setError('File size must be less than 10MB');
                setSelectedFile(null);
                return;
            }

            setSelectedFile(file);
            setError(null);
            setValidationResult(null);
        }
    };

    /**
     * Handles file upload and validation
     */
    const handleUpload = async () => {
        if (!selectedFile) {
            setError('Please select a file to upload');
            return;
        }

        setUploading(true);
        setError(null);
        setUploadProgress(0);

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);

            const response = await axios.post('/api/validate-file', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
                onUploadProgress: (progressEvent) => {
                    const progress = Math.round(
                        (progressEvent.loaded * 100) / progressEvent.total
                    );
                    setUploadProgress(progress);
                },
            });

            setValidationResult(response.data);
            setUploadProgress(100);

        } catch (err) {
            console.error('Upload error:', err);
            setError(err.response?.data?.error || 'Upload failed. Please try again.');
        } finally {
            setUploading(false);
        }
    };

    /**
     * Handles file download
     * 
     * @param {string} url - Download URL
     * @param {string} filename - Filename for download
     */
    const handleDownload = (url, filename) => {
        if (url) {
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    };

    /**
     * Resets the form
     */
    const handleReset = () => {
        setSelectedFile(null);
        setValidationResult(null);
        setError(null);
        setUploadProgress(0);
        // Reset file input
        const fileInput = document.getElementById('file-input');
        if (fileInput) {
            fileInput.value = '';
        }
    };

    return (
        <div className="file-upload-container">
            <div className="upload-header">
                <h2>Excel File Validation</h2>
                <p>Upload an Excel file to validate email, date, name, and ID fields</p>
            </div>

            <div className="upload-section">
                <div className="file-input-container">
                    <input
                        id="file-input"
                        type="file"
                        accept=".xlsx,.xls"
                        onChange={handleFileSelect}
                        disabled={uploading}
                        className="file-input"
                    />
                    <label htmlFor="file-input" className="file-input-label">
                        {selectedFile ? selectedFile.name : 'Choose Excel file (.xlsx, .xls)'}
                    </label>
                </div>

                {selectedFile && (
                    <div className="file-info">
                        <p><strong>Selected File:</strong> {selectedFile.name}</p>
                        <p><strong>Size:</strong> {(selectedFile.size / 1024 / 1024).toFixed(2)} MB</p>
                    </div>
                )}

                {error && (
                    <div className="error-message">
                        <p>{error}</p>
                    </div>
                )}

                <div className="upload-actions">
                    <button
                        onClick={handleUpload}
                        disabled={!selectedFile || uploading}
                        className="upload-button"
                    >
                        {uploading ? 'Uploading...' : 'Upload & Validate'}
                    </button>
                    
                    <button
                        onClick={handleReset}
                        disabled={uploading}
                        className="reset-button"
                    >
                        Reset
                    </button>
                </div>

                {uploading && (
                    <div className="progress-container">
                        <div className="progress-bar">
                            <div 
                                className="progress-fill"
                                style={{ width: `${uploadProgress}%` }}
                            ></div>
                        </div>
                        <p className="progress-text">{uploadProgress}%</p>
                    </div>
                )}
            </div>

            {validationResult && (
                <div className="validation-result">
                    <h3>Validation Results</h3>
                    
                    <div className="result-summary">
                        <div className="result-item">
                            <span className="label">Status:</span>
                            <span className={`value status-${validationResult.status.toLowerCase()}`}>
                                {validationResult.status}
                            </span>
                        </div>
                        
                        <div className="result-item">
                            <span className="label">Rows Processed:</span>
                            <span className="value">{validationResult.rowsProcessed}</span>
                        </div>
                        
                        <div className="result-item">
                            <span className="label">Rows with Errors:</span>
                            <span className="value error-count">{validationResult.rowsWithErrors}</span>
                        </div>
                    </div>

                    {validationResult.errors && validationResult.errors.length > 0 && (
                        <div className="error-details">
                            <h4>Validation Errors</h4>
                            <div className="error-list">
                                {validationResult.errors.map((error, index) => (
                                    <div key={index} className="error-item">
                                        <p><strong>Row {error.rowNumber}:</strong> {error.columnName}</p>
                                        <p><strong>Error:</strong> {error.errorMessage}</p>
                                        <p><strong>Invalid Value:</strong> {error.invalidValue}</p>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="download-section">
                        {validationResult.downloadUrl && (
                            <button
                                onClick={() => handleDownload(validationResult.downloadUrl, `${validationResult.filename}_processed.xlsx`)}
                                className="download-button"
                            >
                                Download Processed File
                            </button>
                        )}
                        
                        {validationResult.errorFileUrl && validationResult.rowsWithErrors > 0 && (
                            <div className="error-file-section">
                                <h4>📋 Error File Available</h4>
                                <p className="error-instructions">
                                    Download the error file below, fix the highlighted errors, and re-upload the corrected file.
                                </p>
                                <button
                                    onClick={() => handleDownload(validationResult.errorFileUrl, `${validationResult.filename}_errors.xlsx`)}
                                    className="download-button error"
                                >
                                    📥 Download Error File for Correction
                                </button>
                                <div className="error-file-info">
                                    <p><strong>Instructions:</strong></p>
                                    <ul>
                                        <li>Download the error file</li>
                                        <li>Fix the validation errors in the highlighted cells</li>
                                        <li>Save the file</li>
                                        <li>Re-upload the corrected file</li>
                                    </ul>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default FileUpload; 