# Azure File Validation Service

A production-ready Azure Function service that validates Excel (.xlsx) and CSV files stored in Azure Blob Storage. The service performs row-by-row validation, highlights invalid cells, and provides detailed error reporting.

## Features

- **Streaming File Processing**: Processes large files without loading them entirely into memory
- **Multi-format Support**: Handles both Excel (.xlsx) and CSV files
- **Comprehensive Validation**: Validates required fields, data types, and formats
- **Visual Error Highlighting**: Highlights invalid cells in yellow in Excel output
- **Error Details**: Adds an ErrorDetails column with specific validation failures
- **Azure Integration**: Seamless integration with Azure Blob Storage and SQL Server
- **SAS URL Generation**: Provides secure, time-limited download URLs
- **Metadata Persistence**: Stores validation results in SQL Server for audit trails

## Architecture

The project follows a clean, layered architecture:

```
├── function/          # Azure Function entry point
├── handler/           # Request/response adapter layer
├── service/           # Business logic interfaces
├── service/impl/      # Business logic implementations
├── dto/              # Data transfer objects
├── entity/           # JPA entities
├── dao/              # Data access objects
├── exception/        # Custom exceptions
└── config/           # Spring configuration
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Azure Storage Account
- Azure SQL Database
- Azure Functions Core Tools (for local development)

## Configuration

### 1. Azure Blob Storage Setup

Create a storage account and container:
```bash
# Create storage account
az storage account create --name yourstorageaccount --resource-group your-rg --location eastus --sku Standard_LRS

# Create container
az storage container create --name file-validation --account-name yourstorageaccount
```

### 2. Azure SQL Database Setup

Create a SQL database and configure the connection string:
```sql
-- Create database
CREATE DATABASE FileValidationDB;

-- Create table (auto-created by JPA)
-- The table will be created automatically when the application starts
```

### 3. Environment Configuration

Update `local.settings.json` with your Azure credentials:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "BLOB_CONNECTION_STRING": "DefaultEndpointsProtocol=https;AccountName=yourstorageaccount;AccountKey=yourstoragekey;EndpointSuffix=core.windows.net",
    "BLOB_CONTAINER_NAME": "file-validation",
    "SQL_CONNECTION_STRING": "jdbc:sqlserver://yourserver.database.windows.net:1433;database=yourdatabase;user=yourusername;password=yourpassword;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
    "SAS_EXPIRY_HOURS": "24",
    "MAX_FILE_SIZE_MB": "100",
    "REQUIRED_COLUMNS": "id,date,sellType,name,price"
  }
}
```

## Validation Rules

The service validates the following rules:

- **Required Fields**: All specified columns must be present and non-empty
- **ID Field**: Must be numeric
- **Date Field**: Must be in yyyy-MM-dd format
- **SellType Field**: Must be non-empty
- **Other Required Fields**: Must be non-empty

## API Usage

### Endpoint
```
POST /api/validate
```

### Request Format
```json
{
  "filename": "sales_data.xlsx"
}
```

### Response Format
```json
{
  "status": "SUCCESS",
  "message": "File validation completed successfully",
  "totalRows": 1000,
  "validRows": 950,
  "invalidRows": 50,
  "downloadUrl": "https://yourstorage.blob.core.windows.net/container/filename_validated_20231201_143022.xlsx?sv=2020-08-04&ss=bfqt&srt=sco&sp=r&se=2023-12-02T14:30:22Z&st=2023-12-01T06:30:22Z&spr=https&sig=...",
  "processedAt": "2023-12-01T14:30:22"
}
```

### Error Response Format
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Required column not found: id",
  "timestamp": "2023-12-01T14:30:22"
}
```

## Local Development

### 1. Install Dependencies
```bash
mvn clean install
```

### 2. Start Local Development
```bash
# Install Azure Functions Core Tools
npm install -g azure-functions-core-tools@4 --unsafe-perm true

# Start the function locally
mvn clean package
func start
```

### 3. Test the Function
```bash
curl -X POST http://localhost:7071/api/validate \
  -H "Content-Type: application/json" \
  -d '{"filename": "test_data.xlsx"}'
```

## Deployment

### 1. Deploy to Azure
```bash
# Login to Azure
az login

# Deploy the function
mvn clean package
func azure functionapp publish your-function-app-name
```

### 2. Configure Application Settings
```bash
az functionapp config appsettings set --name your-function-app-name --resource-group your-rg --settings \
  BLOB_CONNECTION_STRING="your-connection-string" \
  BLOB_CONTAINER_NAME="file-validation" \
  SQL_CONNECTION_STRING="your-sql-connection-string" \
  SAS_EXPIRY_HOURS="24" \
  MAX_FILE_SIZE_MB="100" \
  REQUIRED_COLUMNS="id,date,sellType,name,price"
```

## Sample Data

### Excel File Format
| id | date | sellType | name | price |
|----|------|----------|------|-------|
| 1 | 2023-12-01 | retail | Product A | 100.00 |
| 2 | 2023-12-02 | wholesale | Product B | 200.00 |

### CSV File Format
```csv
id,date,sellType,name,price
1,2023-12-01,retail,Product A,100.00
2,2023-12-02,wholesale,Product B,200.00
```

## Error Handling

The service handles various error scenarios:

- **File Not Found**: Returns 404 with FILE_NOT_FOUND error
- **Unsupported File Type**: Returns 400 with UNSUPPORTED_FILE_TYPE error
- **Validation Errors**: Returns 400 with VALIDATION_ERROR
- **Internal Errors**: Returns 500 with INTERNAL_ERROR

## Performance Considerations

- **Streaming Processing**: Files are processed row-by-row to minimize memory usage
- **Batch Processing**: Large files are processed in configurable batches
- **Connection Pooling**: Database connections are pooled for optimal performance
- **Async Processing**: Non-blocking I/O operations where possible

## Monitoring and Logging

The service includes comprehensive logging:

- Request/response logging
- Validation statistics
- Error tracking
- Performance metrics

Logs are automatically integrated with Azure Application Insights when deployed.

## Security

- **SAS URLs**: Time-limited, secure download URLs
- **Input Validation**: Comprehensive request validation
- **SQL Injection Protection**: Parameterized queries via JPA
- **File Size Limits**: Configurable maximum file size limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License. 