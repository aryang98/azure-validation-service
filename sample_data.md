# Sample Data for File Validation Service

This document provides sample data and examples for testing the file validation service.

## 📊 Sample Excel File Structure

The Excel file should contain the following columns:

| Column | Description | Validation Rules |
|--------|-------------|------------------|
| A | ID | Required, non-blank string |
| B | Sale Date | Required, valid date (YYYY-MM-DD), not in the past |
| C | Sale Mode | Required, must be "dine_in" or "take_away" |
| D | MIN_SALE | Required, numeric value greater than 0 |

## 📋 Sample Data (Valid)

| ID | Sale Date | Sale Mode | MIN_SALE |
|----|-----------|-----------|----------|
| SALE001 | 2024-01-15 | dine_in | 25.50 |
| SALE002 | 2024-01-16 | take_away | 15.75 |
| SALE003 | 2024-01-17 | dine_in | 30.00 |
| SALE004 | 2024-01-18 | take_away | 12.25 |
| SALE005 | 2024-01-19 | dine_in | 45.80 |

## ❌ Sample Data (Invalid)

| ID | Sale Date | Sale Mode | MIN_SALE | Expected Error |
|----|-----------|-----------|----------|----------------|
|  | 2024-01-15 | dine_in | 25.50 | ID is required |
| SALE006 | 2023-01-15 | dine_in | 25.50 | Sale date cannot be in the past |
| SALE007 | 2024-01-15 | delivery | 25.50 | Sale mode must be dine_in or take_away |
| SALE008 | 2024-01-15 | dine_in | 0 | MIN_SALE must be greater than 0 |
| SALE009 | 2024-01-15 | dine_in | -5 | MIN_SALE must be greater than 0 |

## 🔧 Creating Test Excel File

### Using Python

```python
import pandas as pd
from datetime import date, timedelta

# Valid data
valid_data = {
    'ID': ['SALE001', 'SALE002', 'SALE003', 'SALE004', 'SALE005'],
    'Sale Date': ['2024-01-15', '2024-01-16', '2024-01-17', '2024-01-18', '2024-01-19'],
    'Sale Mode': ['dine_in', 'take_away', 'dine_in', 'take_away', 'dine_in'],
    'MIN_SALE': [25.50, 15.75, 30.00, 12.25, 45.80]
}

# Invalid data
invalid_data = {
    'ID': ['', 'SALE006', 'SALE007', 'SALE008', 'SALE009'],
    'Sale Date': ['2024-01-15', '2023-01-15', '2024-01-15', '2024-01-15', '2024-01-15'],
    'Sale Mode': ['dine_in', 'dine_in', 'delivery', 'dine_in', 'dine_in'],
    'MIN_SALE': [25.50, 25.50, 25.50, 0, -5]
}

# Create DataFrames
df_valid = pd.DataFrame(valid_data)
df_invalid = pd.DataFrame(invalid_data)

# Save to Excel
df_valid.to_excel('valid_sales_data.xlsx', index=False)
df_invalid.to_excel('invalid_sales_data.xlsx', index=False)
```

### Using Excel

1. Create a new Excel file
2. Add headers in row 1: `ID`, `Sale Date`, `Sale Mode`, `MIN_SALE`
3. Add data starting from row 2
4. Save as `.xlsx` format

## 🧪 Testing Scenarios

### 1. Valid File Test

**Input**: File with all valid data
**Expected Output**:
```json
{
  "status": "SUCCESS",
  "message": "File validated successfully",
  "updatedFileUrl": null,
  "totalRows": 5,
  "validRows": 5,
  "invalidRows": 0,
  "validationDate": "2024-01-15T10:30:00",
  "processingTime": "1.25 seconds"
}
```

### 2. File with Errors Test

**Input**: File with some invalid data
**Expected Output**:
```json
{
  "status": "VALIDATED_WITH_ERRORS",
  "message": "File validated with errors. Check the updated file for details.",
  "updatedFileUrl": "https://storage.blob.core.windows.net/container/updated_file.xlsx",
  "totalRows": 5,
  "validRows": 2,
  "invalidRows": 3,
  "validationDate": "2024-01-15T10:30:00",
  "processingTime": "1.45 seconds"
}
```

### 3. Large File Test

**Input**: File with 100,000 rows (70MB)
**Expected Output**:
```json
{
  "status": "SUCCESS",
  "message": "File validated successfully",
  "updatedFileUrl": null,
  "totalRows": 100000,
  "validRows": 95000,
  "invalidRows": 5000,
  "validationDate": "2024-01-15T10:30:00",
  "processingTime": "45.30 seconds"
}
```

## 📝 API Testing

### Using cURL

```bash
# Test health endpoint
curl -X GET "https://your-function-app.azurewebsites.net/api/health"

# Test validation endpoint
curl -X POST "https://your-function-app.azurewebsites.net/api/validate" \
  -H "Content-Type: application/json" \
  -d '{"fileMetadataId": 1}'
```

### Using Postman

1. **Health Check**:
   - Method: GET
   - URL: `https://your-function-app.azurewebsites.net/api/health`

2. **File Validation**:
   - Method: POST
   - URL: `https://your-function-app.azurewebsites.net/api/validate`
   - Headers: `Content-Type: application/json`
   - Body: `{"fileMetadataId": 1}`

## 🔍 Error Messages

The service will add an "Error Message" column to files with validation errors:

| ID | Sale Date | Sale Mode | MIN_SALE | Error Message |
|----|-----------|-----------|----------|---------------|
|  | 2024-01-15 | dine_in | 25.50 | ID: ID is required |
| SALE006 | 2023-01-15 | dine_in | 25.50 | Sale date cannot be in the past |
| SALE007 | 2024-01-15 | delivery | 25.50 | Sale mode must be either 'dine_in' or 'take_away' |
| SALE008 | 2024-01-15 | dine_in | 0 | MIN_SALE: MIN_SALE must be greater than 0 |
| SALE009 | 2024-01-15 | dine_in | -5 | MIN_SALE: MIN_SALE must be greater than 0 |

## 📊 Performance Benchmarks

| File Size | Rows | Processing Time | Memory Usage |
|-----------|------|-----------------|--------------|
| 1MB | 1,000 | ~2 seconds | ~50MB |
| 10MB | 10,000 | ~15 seconds | ~200MB |
| 50MB | 50,000 | ~60 seconds | ~500MB |
| 70MB | 100,000 | ~90 seconds | ~1GB |

## 🚨 Common Issues

1. **Date Format**: Ensure dates are in YYYY-MM-DD format
2. **Sale Mode**: Only "dine_in" or "take_away" are valid
3. **Numeric Values**: MIN_SALE must be a positive number
4. **File Size**: Maximum 70MB supported
5. **Row Count**: Maximum 100,000 rows supported

## 📚 Additional Resources

- [Excel File Format Specifications](https://support.microsoft.com/en-us/office/file-formats-that-are-supported-in-excel-0943ff2c-6014-4e8d-aaea-b83d51d46247)
- [Date Format Standards](https://www.iso.org/iso-8601-date-and-time-format.html)
- [Azure Blob Storage Limits](https://docs.microsoft.com/en-us/azure/storage/blobs/scalability-targets) 