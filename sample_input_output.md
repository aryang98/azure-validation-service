# Sample Input and Output Examples

This document provides comprehensive examples of how the Azure File Validation Service works with different input scenarios and their corresponding outputs.

## API Endpoint

```
POST /api/validate
Content-Type: application/json
```

## Sample Input

### Request Body
```json
{
  "filename": "sales_data.xlsx"
}
```

## Sample Output Scenarios

### 1. Successful Validation (No Errors)

**Input File**: `sales_data.xlsx` (valid data)
```
| id | date       | sellType  | name      | price |
|----|------------|-----------|-----------|-------|
| 1  | 2023-12-01 | retail    | Product A | 100.00|
| 2  | 2023-12-02 | wholesale | Product B | 200.00|
| 3  | 2023-12-03 | retail    | Product C | 150.00|
```

**API Response**:
```json
{
  "status": "SUCCESS",
  "message": "File validation completed successfully - no errors found",
  "totalRows": 3,
  "validRows": 3,
  "invalidRows": 0,
  "executionTimeMs": 1250,
  "processedAt": "2023-12-01T14:30:22"
}
```

**Notes**:
- No file modification or upload
- No download URL provided
- Fast response time
- Simple success message

---

### 2. Validation with Errors (Excel File)

**Input File**: `sales_data_with_errors.xlsx`
```
| id | date         | sellType  | name      | price |
|----|--------------|-----------|-----------|-------|
| 1  | 2023-12-01   | retail    | Product A | 100.00|
| 2  | invalid-date | wholesale | Product B | 200.00|
| 3  | 2023-12-03   |           | Product C | 150.00|
| 4  | 2023-12-04   | wholesale |           | 300.00|
| abc| 2023-12-05   | retail    | Product E | 75.50 |
```

**API Response**:
```json
{
  "status": "COMPLETED_WITH_ERRORS",
  "message": "File validation completed with errors. Download the corrected file using the provided URL.",
  "totalRows": 5,
  "validRows": 1,
  "invalidRows": 4,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/file-validation/sales_data_with_errors_validated_20231201_143022.xlsx?sv=2020-08-04&ss=bfqt&srt=sco&sp=r&se=2023-12-02T14:30:22Z&st=2023-12-01T06:30:22Z&spr=https&sig=abc123...",
  "executionTimeMs": 1850,
  "processedAt": "2023-12-01T14:30:22"
}
```

**Output File**: `sales_data_with_errors_validated_20231201_143022.xlsx`
```
| id | date         | sellType  | name      | price | ErrorDetails                    |
|----|--------------|-----------|-----------|-------|--------------------------------|
| 1  | 2023-12-01   | retail    | Product A | 100.00|                                 |
| 2  | invalid-date | wholesale | Product B | 200.00| date: Invalid date format      |
| 3  | 2023-12-03   |           | Product C | 150.00| sellType: Required field is empty|
| 4  | 2023-12-04   | wholesale |           | 300.00| name: Required field is empty   |
| abc| 2023-12-05   | retail    | Product E | 75.50 | id: Must be numeric            |
```

**Notes**:
- **All original data preserved** - no data is removed or modified
- Invalid cells highlighted in **yellow background**
- ErrorDetails column added with specific error messages (comma-separated for multiple errors)
- File uploaded to blob storage
- Download URL provided

---

### 3. Validation with Errors (CSV File)

**Input File**: `sales_data.csv`
```csv
id,date,sellType,name,price
1,2023-12-01,retail,Product A,100.00
2,invalid-date,wholesale,Product B,200.00
3,2023-12-03,,Product C,150.00
4,2023-12-04,wholesale,,300.00
5,2023-12-05,retail,Product E,75.50
abc,2023-12-06,retail,Product F,50.00
```

**API Response**:
```json
{
  "status": "COMPLETED_WITH_ERRORS",
  "message": "File validation completed with errors. Download the corrected file using the provided URL.",
  "totalRows": 6,
  "validRows": 2,
  "invalidRows": 4,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/file-validation/sales_data_validated_20231201_143022.csv?sv=2020-08-04&ss=bfqt&srt=sco&sp=r&se=2023-12-02T14:30:22Z&st=2023-12-01T06:30:22Z&spr=https&sig=def456...",
  "executionTimeMs": 1650,
  "processedAt": "2023-12-01T14:30:22"
}
```

**Output File**: `sales_data_validated_20231201_143022.csv`
```csv
id,date,sellType,name,price,ErrorDetails
1,2023-12-01,retail,Product A,100.00,
5,2023-12-05,retail,Product E,75.50,
2,invalid-date,wholesale,Product B,200.00,date: Invalid date format (expected yyyy-MM-dd)
3,2023-12-03,,Product C,150.00,sellType: Required field is empty
4,2023-12-04,wholesale,,300.00,name: Required field is empty
abc,2023-12-06,retail,Product F,50.00,id: Must be numeric
```

**Notes**:
- **All original data preserved** - no data is removed or modified
- ErrorDetails column added with specific error messages (comma-separated for multiple errors)
- Valid rows show empty ErrorDetails column
- File uploaded to blob storage
- Download URL provided

---

### 4. Error Scenarios

#### 4.1 File Not Found
**Request**:
```json
{
  "filename": "nonexistent_file.xlsx"
}
```

**Response**:
```json
{
  "errorCode": "FILE_NOT_FOUND",
  "message": "File not found: nonexistent_file.xlsx",
  "timestamp": "2023-12-01T14:30:22"
}
```

#### 4.2 Unsupported File Type
**Request**:
```json
{
  "filename": "document.pdf"
}
```

**Response**:
```json
{
  "errorCode": "UNSUPPORTED_FILE_TYPE",
  "message": "Unsupported file type: pdf. Only .xlsx and .csv files are supported.",
  "timestamp": "2023-12-01T14:30:22"
}
```

#### 4.3 Missing Required Column
**Input File**: `missing_column.xlsx`
```
| id | date       | name      | price |
|----|------------|-----------|-------|
| 1  | 2023-12-01 | Product A | 100.00|
```

**Response**:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Required column not found: sellType",
  "timestamp": "2023-12-01T14:30:22"
}
```

#### 4.4 Invalid Request Format
**Request**:
```json
{
  "filename": ""
}
```

**Response**:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed: Filename is required",
  "timestamp": "2023-12-01T14:30:22"
}
```

---

## Validation Rules Summary

| Field | Validation Rule | Error Message |
|-------|----------------|---------------|
| id | Must be numeric | "id: Must be numeric" |
| date | Must be yyyy-MM-dd format | "date: Invalid date format (expected yyyy-MM-dd)" |
| sellType | Must not be empty | "sellType: Required field is empty" |
| name | Must not be empty | "name: Required field is empty" |
| price | Must not be empty | "price: Required field is empty" |

---

## Key Features

### Data Preservation
- **All original data is preserved** - no rows or columns are removed
- **Original values remain unchanged** - only highlighting and error details are added
- **Complete audit trail** - users can see exactly what was wrong with their data

### Visual Error Indicators
- **Yellow highlighting** in Excel files for invalid cells
- **ErrorDetails column** with specific error messages
- **Empty ErrorDetails** for valid rows to maintain consistency

### File Processing Behavior
- **No errors**: Simple success response, no file upload
- **With errors**: Modified file uploaded with ErrorDetails column and highlighting

---

## Performance Examples

### Small File (100 rows, no errors)
- **Execution Time**: ~500ms
- **Response**: Simple success response
- **Storage**: No upload

### Large File (10,000 rows, no errors)
- **Execution Time**: ~2,500ms
- **Response**: Simple success response
- **Storage**: No upload

### Small File (100 rows, 20 errors)
- **Execution Time**: ~800ms
- **Response**: Detailed response with download URL
- **Storage**: Modified file uploaded (preserves all data)

### Large File (10,000 rows, 500 errors)
- **Execution Time**: ~3,500ms
- **Response**: Detailed response with download URL
- **Storage**: Modified file uploaded (preserves all data)

---

## Testing with cURL

### Test with Valid File
```bash
curl -X POST http://localhost:7071/api/validate \
  -H "Content-Type: application/json" \
  -d '{"filename": "sales_data.xlsx"}'
```

### Test with File Containing Errors
```bash
curl -X POST http://localhost:7071/api/validate \
  -H "Content-Type: application/json" \
  -d '{"filename": "sales_data_with_errors.xlsx"}'
```

### Test Error Handling
```bash
curl -X POST http://localhost:7071/api/validate \
  -H "Content-Type: application/json" \
  -d '{"filename": "nonexistent_file.xlsx"}'
```

---

## Database Metadata Examples

### Successful Validation Record
```sql
SELECT * FROM validation_metadata WHERE status = 'SUCCESS';
```

| id | file_name | total_rows | valid_rows | invalid_rows | status | uploaded_at | processed_at | output_file_name | error_message |
|----|-----------|------------|------------|--------------|--------|-------------|--------------|------------------|---------------|
| 1 | sales_data.xlsx | 3 | 3 | 0 | SUCCESS | 2023-12-01 14:30:22 | 2023-12-01 14:30:22 | NULL | NULL |

### Validation with Errors Record
```sql
SELECT * FROM validation_metadata WHERE status = 'COMPLETED_WITH_ERRORS';
```

| id | file_name | total_rows | valid_rows | invalid_rows | status | uploaded_at | processed_at | output_file_name | error_message |
|----|-----------|------------|------------|--------------|--------|-------------|--------------|------------------|---------------|
| 2 | sales_data_with_errors.xlsx | 5 | 1 | 4 | COMPLETED_WITH_ERRORS | 2023-12-01 14:30:22 | 2023-12-01 14:30:22 | sales_data_with_errors_validated_20231201_143022.xlsx | NULL |

### Error Record
```sql
SELECT * FROM validation_metadata WHERE status = 'ERROR';
```

| id | file_name | total_rows | valid_rows | invalid_rows | status | uploaded_at | processed_at | output_file_name | error_message |
|----|-----------|------------|------------|--------------|--------|-------------|--------------|------------------|---------------|
| 3 | invalid_file.xlsx | 0 | 0 | 0 | ERROR | 2023-12-01 14:30:22 | 2023-12-01 14:30:22 | NULL | Required column not found: sellType | 

### Example: Multiple Errors in One Row

**Input Row with Multiple Issues:**
```
| abc | invalid-date |   |   | invalid-price |
```

**ErrorDetails Column Output:**
```
id: Must be numeric, date: Invalid date format (expected yyyy-MM-dd), sellType: Required field is empty, name: Required field is empty, price: Required field is empty
```

**Notes:**
- Multiple validation errors are separated by commas
- Each error follows the format: "fieldName: error description"
- All errors for the row are listed in a single cell
- Easy to read and parse for further processing 

---

### Comprehensive Example: Various Date Format Errors and Highlighting

**Input File**: `complex_validation_example.xlsx`
```
| id | date       | sellType  | name      | price |
|----|------------|-----------|-----------|-------|
| 1  | 2023-12-01 | retail    | Product A | 100.00|  ← Valid row
| 2  | 12/01/2023 | wholesale | Product B | 200.00|  ← Wrong date format (MM/dd/yyyy)
| 3  | 2023-13-45 | retail    | Product C | 150.00|  ← Invalid date (month 13, day 45)
| 4  | 2023-12-04 | wholesale |           | 300.00|  ← Empty name field
| abc| 2023-12-05 | retail    | Product E | 75.50 |  ← Non-numeric ID
| 6  | 2023-12-06 | retail    | Product F |        |  ← Empty price field
| 7  | invalid-date| wholesale | Product G | 250.00|  ← Invalid date format (text)
| 8  | 2023-12-08 |           | Product H | 180.00|  ← Empty sellType field
| 9  | 2023-12-09 | retail    |           | 90.00 |  ← Empty name field
| xyz| 2023-12-10 | retail    | Product J | 120.00|  ← Non-numeric ID
```

**API Response:**
```json
{
  "status": "COMPLETED_WITH_ERRORS",
  "message": "File validation completed with errors. Download the corrected file using the provided URL.",
  "totalRows": 10,
  "validRows": 1,
  "invalidRows": 9,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/file-validation/complex_validation_example_validated_20231201_143022.xlsx?sv=...",
  "executionTimeMs": 2200,
  "processedAt": "2023-12-01T14:30:22"
}
```

**Output File with ErrorDetails Column:**
```
| id | date       | sellType  | name      | price | ErrorDetails                    |
|----|------------|-----------|-----------|-------|--------------------------------|
| 1  | 2023-12-01 | retail    | Product A | 100.00|                                 |
| 2  | 12/01/2023 | wholesale | Product B | 200.00| date: Invalid date format (expected yyyy-MM-dd) |
| 3  | 2023-13-45 | retail    | Product C | 150.00| date: Invalid date format (expected yyyy-MM-dd) |
| 4  | 2023-12-04 | wholesale |           | 300.00| name: Required field is empty   |
| abc| 2023-12-05 | retail    | Product E | 75.50 | id: Must be numeric            |
| 6  | 2023-12-06 | retail    | Product F |       | price: Required field is empty |
| 7  | invalid-date| wholesale | Product G | 250.00| date: Invalid date format (expected yyyy-MM-dd) |
| 8  | 2023-12-08 |           | Product H | 180.00| sellType: Required field is empty|
| 9  | 2023-12-09 | retail    |           | 90.00 | name: Required field is empty   |
| xyz| 2023-12-10 | retail    | Product J | 120.00| id: Must be numeric            |
```

**Visual Highlighting in Excel Output:**
- **Row 2**: Date cell `12/01/2023` highlighted in **YELLOW** (wrong format)
- **Row 3**: Date cell `2023-13-45` highlighted in **YELLOW** (invalid date)
- **Row 4**: Name cell (empty) highlighted in **YELLOW** (required field empty)
- **Row 5**: ID cell `abc` highlighted in **YELLOW** (non-numeric)
- **Row 6**: Price cell (empty) highlighted in **YELLOW** (required field empty)
- **Row 7**: Date cell `invalid-date` highlighted in **YELLOW** (invalid format)
- **Row 8**: SellType cell (empty) highlighted in **YELLOW** (required field empty)
- **Row 9**: Name cell (empty) highlighted in **YELLOW** (required field empty)
- **Row 10**: ID cell `xyz` highlighted in **YELLOW** (non-numeric)

**Key Features Demonstrated:**
1. **Date Format Validation**: Multiple wrong date formats detected
2. **Visual Highlighting**: Invalid cells highlighted in yellow
3. **ErrorDetails Column**: Comma-separated list of all errors per row
4. **Data Preservation**: All original data remains intact
5. **Multiple Error Types**: ID, date, required field validations 