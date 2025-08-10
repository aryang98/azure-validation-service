#!/bin/bash

# Test script for complex validation example with multiple errors and date formats
# This demonstrates the comprehensive error handling and highlighting features

BASE_URL="http://localhost:7071/api/validate"
# For deployed function, use: BASE_URL="https://your-function-app.azurewebsites.net/api/validate"

echo "🧪 Complex Validation Example Test"
echo "=================================="
echo ""

echo "📋 Testing with complex validation example (multiple errors + date formats)"
echo "Expected: COMPLETED_WITH_ERRORS response with download URL"
echo ""

echo "Request:"
echo 'curl -X POST "'$BASE_URL'" -H "Content-Type: application/json" -d '"'"'{"filename": "complex_validation_example.xlsx"}'"'"
echo ""

echo "Expected Response:"
echo '{
  "status": "COMPLETED_WITH_ERRORS",
  "message": "File validation completed with errors. Download the corrected file using the provided URL.",
  "totalRows": 10,
  "validRows": 1,
  "invalidRows": 9,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/file-validation/complex_validation_example_validated_20231201_143022.xlsx?sv=...",
  "executionTimeMs": 2200,
  "processedAt": "2023-12-01T14:30:22"
}'
echo ""

echo "📊 Validation Results Summary:"
echo "- Total Rows: 10"
echo "- Valid Rows: 1 (Row 1: 2023-12-01, retail, Product A, 100.00)"
echo "- Invalid Rows: 9"
echo ""

echo "🔍 Error Details by Row:"
echo "Row 2: date: Invalid date format (expected yyyy-MM-dd) - '12/01/2023'"
echo "Row 3: date: Invalid date format (expected yyyy-MM-dd) - '2023-13-45'"
echo "Row 4: name: Required field is empty"
echo "Row 5: id: Must be numeric - 'abc'"
echo "Row 6: price: Required field is empty"
echo "Row 7: date: Invalid date format (expected yyyy-MM-dd) - 'invalid-date'"
echo "Row 8: sellType: Required field is empty"
echo "Row 9: name: Required field is empty"
echo "Row 10: id: Must be numeric - 'xyz'"
echo ""

echo "🎨 Visual Highlighting in Excel Output:"
echo "- Row 2: Date cell '12/01/2023' → YELLOW highlight"
echo "- Row 3: Date cell '2023-13-45' → YELLOW highlight"
echo "- Row 4: Name cell (empty) → YELLOW highlight"
echo "- Row 5: ID cell 'abc' → YELLOW highlight"
echo "- Row 6: Price cell (empty) → YELLOW highlight"
echo "- Row 7: Date cell 'invalid-date' → YELLOW highlight"
echo "- Row 8: SellType cell (empty) → YELLOW highlight"
echo "- Row 9: Name cell (empty) → YELLOW highlight"
echo "- Row 10: ID cell 'xyz' → YELLOW highlight"
echo ""

echo "📋 ErrorDetails Column Format:"
echo "Each invalid row will have an ErrorDetails column with comma-separated errors:"
echo "Example: 'date: Invalid date format (expected yyyy-MM-dd), id: Must be numeric'"
echo ""

echo "✅ Key Features Demonstrated:"
echo "1. Multiple validation error types in single file"
echo "2. Various date format errors (MM/dd/yyyy, invalid dates, text)"
echo "3. Required field validation (empty fields)"
echo "4. Data type validation (non-numeric IDs)"
echo "5. Visual highlighting in Excel (yellow background)"
echo "6. Comma-separated error list in ErrorDetails column"
echo "7. All original data preserved"
echo ""

echo "🚀 Test Command:"
echo 'curl -X POST "'$BASE_URL'" \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"filename": "complex_validation_example.xlsx"}'"'"
echo ""

echo "📥 After validation, download the processed file using the provided SAS URL"
echo "The downloaded Excel file will show:"
echo "- All original data preserved"
echo "- Invalid cells highlighted in yellow"
echo "- ErrorDetails column with comma-separated error messages"
echo "- Ready for user review and correction" 