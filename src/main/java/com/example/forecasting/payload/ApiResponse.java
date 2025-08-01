package com.example.forecasting.payload;

/**
 * API Response Wrapper for Demand Forecasting System
 * 
 * This class provides a standardized response structure for all API endpoints
 * in the demand forecasting application. It ensures consistent response format
 * across all controllers and provides a unified way to handle success and error
 * responses.
 * 
 * Response Structure:
 * - success: Boolean indicating operation success/failure
 * - message: Human-readable message describing the result
 * - data: Optional payload data for successful operations
 * - timestamp: Response generation timestamp
 * - errorCode: Optional error code for failed operations
 * 
 * Usage Patterns:
 * - Success responses: new ApiResponse(true, "Operation successful", data)
 * - Error responses: new ApiResponse(false, "Error message", null)
 * - Simple confirmations: new ApiResponse(true, "Action completed")
 * 
 * Benefits:
 * - Consistent response format across all endpoints
 * - Easy client-side response handling
 * - Standardized error reporting
 * - Extensible for additional response metadata
 * 
 * @author API Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class ApiResponse {
    
    /**
     * Indicates whether the operation was successful
     * 
     * This field provides a quick way for clients to determine
     * if the requested operation completed successfully or failed.
     * 
     * Values:
     * - true: Operation completed successfully
     * - false: Operation failed or encountered an error
     */
    private Boolean success;
    
    /**
     * Human-readable message describing the operation result
     * 
     * This field provides context about what happened during
     * the operation, whether successful or failed. Messages
     * should be clear and actionable for end users.
     * 
     * Examples:
     * - "User created successfully"
     * - "Template downloaded successfully"
     * - "Invalid credentials provided"
     * - "File upload failed: invalid format"
     */
    private String message;
    
    /**
     * Optional data payload for successful operations
     * 
     * This field contains the actual response data for successful
     * operations. It can be any object type and is serialized
     * as JSON in the response. For error responses, this field
     * is typically null.
     * 
     * Common Data Types:
     * - User information objects
     * - List of entities (templates, reports, etc.)
     * - File download links
     * - Authentication tokens
     * - Status information
     */
    private Object data;
    
    /**
     * Timestamp when the response was generated
     * 
     * This field provides the exact time when the response was
     * created, which is useful for debugging, logging, and
     * client-side caching decisions.
     */
    private String timestamp;
    
    /**
     * Optional error code for failed operations
     * 
     * This field provides a machine-readable error code that
     * clients can use for programmatic error handling. Error
     * codes should be consistent and well-documented.
     * 
     * Common Error Codes:
     * - VALIDATION_ERROR: Input validation failed
     * - AUTHENTICATION_ERROR: Authentication failed
     * - AUTHORIZATION_ERROR: Insufficient permissions
     * - NOT_FOUND: Requested resource not found
     * - INTERNAL_ERROR: Server-side error
     */
    private String errorCode;

    /**
     * Default constructor for JSON serialization
     * 
     * This constructor is required for JSON serialization/deserialization
     * and should not be used directly. Use the static factory methods
     * or parameterized constructors instead.
     */
    public ApiResponse() {
        this.timestamp = java.time.Instant.now().toString();
    }

    /**
     * Constructor for simple success/error responses
     * 
     * This constructor creates a response with just success status
     * and message, suitable for simple confirmations or basic errors.
     * 
     * @param success Whether the operation was successful
     * @param message Human-readable message describing the result
     */
    public ApiResponse(Boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    /**
     * Constructor for responses with data payload
     * 
     * This constructor creates a response with success status, message,
     * and optional data payload for successful operations.
     * 
     * @param success Whether the operation was successful
     * @param message Human-readable message describing the result
     * @param data Optional data payload for the response
     */
    public ApiResponse(Boolean success, String message, Object data) {
        this(success, message);
        this.data = data;
    }

    /**
     * Constructor for error responses with error code
     * 
     * This constructor creates an error response with detailed error
     * information including an error code for programmatic handling.
     * 
     * @param success Whether the operation was successful (should be false)
     * @param message Human-readable error message
     * @param errorCode Machine-readable error code
     */
    public ApiResponse(Boolean success, String message, String errorCode) {
        this(success, message);
        this.errorCode = errorCode;
    }

    /**
     * Static factory method for success responses
     * 
     * This method provides a convenient way to create success responses
     * with a message and optional data payload.
     * 
     * @param message Success message
     * @param data Optional data payload
     * @return ApiResponse with success status
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    /**
     * Static factory method for simple success responses
     * 
     * This method provides a convenient way to create simple success
     * responses with just a confirmation message.
     * 
     * @param message Success message
     * @return ApiResponse with success status
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }

    /**
     * Static factory method for error responses
     * 
     * This method provides a convenient way to create error responses
     * with an error message and optional error code.
     * 
     * @param message Error message
     * @param errorCode Optional error code
     * @return ApiResponse with error status
     */
    public static ApiResponse error(String message, String errorCode) {
        return new ApiResponse(false, message, errorCode);
    }

    /**
     * Static factory method for simple error responses
     * 
     * This method provides a convenient way to create simple error
     * responses with just an error message.
     * 
     * @param message Error message
     * @return ApiResponse with error status
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message);
    }

    // Getters and setters with documentation

    /**
     * Gets the success status of the operation
     * 
     * @return true if operation was successful, false otherwise
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Sets the success status of the operation
     * 
     * @param success Whether the operation was successful
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Gets the response message
     * 
     * @return Human-readable message describing the result
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the response message
     * 
     * @param message Human-readable message describing the result
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the response data payload
     * 
     * @return Optional data payload for the response
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the response data payload
     * 
     * @param data Optional data payload for the response
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Gets the response timestamp
     * 
     * @return Timestamp when the response was generated
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the response timestamp
     * 
     * @param timestamp Timestamp when the response was generated
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the error code
     * 
     * @return Machine-readable error code for failed operations
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code
     * 
     * @param errorCode Machine-readable error code for failed operations
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
} 