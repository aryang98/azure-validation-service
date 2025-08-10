#!/bin/bash

# Azure File Validation Service - API Test Script
# This script demonstrates various API calls and expected responses

BASE_URL="http://localhost:7071/api/validate"
# For deployed function, use: BASE_URL="https://your-function-app.azurewebsites.net/api/validate"

echo "🧪 Azure File Validation Service - API Test Suite"
echo "=================================================="
echo ""

# Test 1: Valid file (should return success)
echo "📋 Test 1: Valid file validation"
echo "Expected: SUCCESS response with no download URL"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "valid_sales_data.xlsx"}'"'"
echo ""
echo "Expected Response:"
echo '{
  "status": "SUCCESS",
  "message": "File validation completed successfully - no errors found",
  "totalRows": 5,
  "validRows": 5,
  "invalidRows": 0,
  "executionTimeMs": 1250,
  "processedAt": "2023-12-01T14:30:22"
}'
echo ""
echo "---"
echo ""

# Test 2: File with errors (should return download URL)
echo "📋 Test 2: File with validation errors"
echo "Expected: COMPLETED_WITH_ERRORS response with download URL"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "invalid_sales_data.xlsx"}'"'"
echo ""
echo "Expected Response:"
echo '{
  "status": "COMPLETED_WITH_ERRORS",
  "message": "File validation completed with errors. Download the corrected file using the provided URL.",
  "totalRows": 6,
  "validRows": 1,
  "invalidRows": 5,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/file-validation/invalid_sales_data_validated_20231201_143022.xlsx?sv=...",
  "executionTimeMs": 1850,
  "processedAt": "2023-12-01T14:30:22"
}'
echo ""
echo "---"
echo ""

# Test 3: CSV file with errors
echo "📋 Test 3: CSV file with validation errors"
echo "Expected: COMPLETED_WITH_ERRORS response with download URL"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "invalid_sales_data.csv"}'"'"
echo ""
echo "Expected Response:"
echo '{
  "status": "COMPLETED_WITH_ERRORS",
  "message": "File validation completed with errors. Download the corrected file using the provided URL.",
  "totalRows": 7,
  "validRows": 2,
  "invalidRows": 5,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/file-validation/invalid_sales_data_validated_20231201_143022.csv?sv=...",
  "executionTimeMs": 1650,
  "processedAt": "2023-12-01T14:30:22"
}'
echo ""
echo "---"
echo ""

# Test 4: File not found
echo "📋 Test 4: File not found error"
echo "Expected: FILE_NOT_FOUND error"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "nonexistent_file.xlsx"}'"'"
echo ""
echo "Expected Response:"
echo '{
  "errorCode": "FILE_NOT_FOUND",
  "message": "File not found: nonexistent_file.xlsx",
  "timestamp": "2023-12-01T14:30:22"
}'
echo ""
echo "---"
echo ""

# Test 5: Unsupported file type
echo "📋 Test 5: Unsupported file type error"
echo "Expected: UNSUPPORTED_FILE_TYPE error"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "document.pdf"}'"'"
echo ""
echo "Expected Response:"
echo '{
  "errorCode": "UNSUPPORTED_FILE_TYPE",
  "message": "Unsupported file type: pdf. Only .xlsx and .csv files are supported.",
  "timestamp": "2023-12-01T14:30:22"
}'
echo ""
echo "---"
echo ""

# Test 6: Invalid request format
echo "📋 Test 6: Invalid request format"
echo "Expected: VALIDATION_ERROR"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": ""}'"'"
echo ""
echo "Expected Response:"
echo '{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed: Filename is required",
  "timestamp": "2023-12-01T14:30:22"
}'
echo ""
echo "---"
echo ""

# Test 7: Missing required column
echo "📋 Test 7: Missing required column"
echo "Expected: VALIDATION_ERROR"
echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "missing_column.xlsx"}'"'"
echo ""
echo "Expected Response:"
echo '{
  "errorCode": "VALIDATION_ERROR",
  "message": "Required column not found: sellType",
  "timestamp": "2023-12-01T14:30:22"
}'
echo ""
echo "=================================================="
echo "🎯 Test Summary:"
echo "✅ Test 1: Valid file - Simple success response"
echo "✅ Test 2: File with errors - Download URL provided"
echo "✅ Test 3: CSV with errors - Download URL provided"
echo "❌ Test 4: File not found - Error response"
echo "❌ Test 5: Unsupported type - Error response"
echo "❌ Test 6: Invalid request - Error response"
echo "❌ Test 7: Missing column - Error response"
echo ""
echo "💡 Key Behaviors:"
echo "• No errors = Simple response, no file upload"
echo "• With errors = Detailed response, file uploaded with corrections"
echo "• Execution time tracked for all operations"
echo "• Error highlighting in Excel files"
echo "• ErrorDetails column added for invalid rows" 