# File Validation Service

A **pure backend** Azure Functions-based service for validating Excel files with Spring Boot, Azure Blob Storage, and Azure SQL Database integration. This service provides RESTful APIs for file validation without any frontend components.

## 🚀 Features

- **Pure Backend Service**: RESTful APIs only, no frontend components
- **Excel File Validation**: Validates sales data with comprehensive business rules
- **Azure Blob Storage Integration**: Secure file storage and retrieval
- **Azure SQL Database**: Metadata persistence and tracking
- **Row-level Error Tracking**: Detailed validation with error messages
- **Large File Support**: Optimized for files up to 70MB with 100,000+ rows
- **Production Ready**: Comprehensive error handling, logging, and monitoring
- **RESTful API**: Clean HTTP endpoints with JSON responses

## 📋 Validation Rules

The service validates the following fields in Excel files:

| Field | Validation Rules |
|-------|------------------|
| **ID** | Required, non-blank string |
| **Sale Date** | Required, valid date format, not in the past |
| **Sale Mode** | Must be either "dine_in" or "take_away" |
| **MIN_SALE** | Required, numeric value greater than 0 |

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   HTTP Client   │    │  Azure Function  │    │  Spring Boot App│
│   (API Calls)   │◄──►│  HTTP Trigger    │◄──►│  Validation Logic│
└─────────────────┘    └──────────────────┘    └──────────────────┘
                                                         │
                                                         ▼
                                                ┌─────────────────┐
                                                │  Azure Services │
                                                │  Blob Storage   │
                                                │  SQL Database   │
                                                └─────────────────┘
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 2.7.5
- **Azure Functions**: Java 11
- **Database**: Azure SQL Database
- **Storage**: Azure Blob Storage
- **Excel Processing**: Apache POI 5.2.3
- **Validation**: Hibernate Validator
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito

## 📦 Prerequisites

- Java 11 or higher
- Maven 3.6+
- Azure Subscription
- Azure Storage Account
- Azure SQL Database
- Azure Functions Core Tools

## 🔧 Configuration

### Environment Variables

Create a `local.settings.json` file for local development:

```json
{
  "IsEncrypted": false,
  "Values": {
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "AZURE_STORAGE_CONNECTION_STRING": "your_storage_connection_string",
    "AZURE_STORAGE_CONTAINER": "file-validation",
    "AZURE_SQL_CONNECTION_STRING": "your_sql_connection_string",
    "AZURE_SQL_USERNAME": "your_username",
    "AZURE_SQL_PASSWORD": "your_password"
  }
}
```

### Application Properties

Key configuration in `application.yml`:

```yaml
azure:
  storage:
    connection-string: ${AZURE_STORAGE_CONNECTION_STRING}
    container-name: ${AZURE_STORAGE_CONTAINER}

spring:
  datasource:
    url: ${AZURE_SQL_CONNECTION_STRING}
    username: ${AZURE_SQL_USERNAME}
    password: ${AZURE_SQL_PASSWORD}
```

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd file-validation-service
```

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Configure Azure Services

1. **Create Azure Storage Account**:
   - Create a storage account
   - Create a container named `file-validation`
   - Get the connection string

2. **Create Azure SQL Database**:
   - Create a SQL database
   - Get the connection string
   - Update firewall rules

### 4. Run Locally

```bash
# Start Azure Functions locally
mvn clean package
func start

# Or run as Spring Boot application
mvn spring-boot:run
```

### 5. Deploy to Azure

```bash
# Deploy to Azure Functions
mvn clean package
func azure functionapp publish <function-app-name>
```

## 📡 API Endpoints

### Validate File

**POST** `/api/validate`

Validates an Excel file by file metadata ID.

**Request Body:**
```json
{
  "fileMetadataId": 123
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "File validated successfully",
  "updatedFileUrl": null,
  "totalRows": 1000,
  "validRows": 950,
  "invalidRows": 50,
  "validationDate": "2024-01-15T10:30:00",
  "processingTime": "2.45 seconds"
}
```

### Health Check

**GET** `/api/health`

Returns service health status.

## 🔧 API Testing

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

## 📊 Database Schema

### File Metadata Table

```sql
CREATE TABLE file_metadata (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    upload_date DATETIME2,
    status VARCHAR(50),
    created_by VARCHAR(100),
    created_date DATETIME2,
    modified_by VARCHAR(100),
    modified_date DATETIME2
);
```

## 🔍 Validation Process

1. **File Retrieval**: Download file from Azure Blob Storage using URL from database
2. **Excel Processing**: Read Excel file using Apache POI
3. **Data Validation**: Validate each row against business rules
4. **Error Tracking**: Collect validation errors for each row
5. **File Update**: If errors exist, create updated file with error messages
6. **Storage**: Upload updated file to blob storage
7. **Response**: Return validation results and statistics

## 📈 Performance Considerations

- **Memory Management**: Stream processing for large files
- **Batch Processing**: Process rows in batches for better performance
- **Connection Pooling**: Optimized database connections
- **Caching**: Blob storage client caching
- **Timeout Handling**: 5-minute function timeout for large files

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Coverage

```bash
mvn jacoco:report
```

## 📝 Logging

The service uses structured logging with the following levels:

- **INFO**: General application flow
- **DEBUG**: Detailed processing information
- **WARN**: Non-critical issues
- **ERROR**: Critical errors and exceptions

## 🔒 Security

- **Authentication**: Azure Functions authentication levels
- **Authorization**: Function-level access control
- **Data Encryption**: Azure Storage encryption at rest
- **Network Security**: Azure SQL Database firewall rules
- **Input Validation**: Comprehensive request validation

## 🚨 Error Handling

The service provides comprehensive error handling:

- **File Not Found**: Proper error messages for missing files
- **Invalid Data**: Detailed validation error messages
- **Network Issues**: Retry logic for Azure service calls
- **Timeout Handling**: Graceful timeout for large file processing

## 📊 Monitoring

### Azure Application Insights

Enable Application Insights for:
- Performance monitoring
- Error tracking
- Usage analytics
- Custom metrics

### Health Checks

- Database connectivity
- Blob storage accessibility
- Service availability

## 🔄 Deployment

### Azure Functions Deployment

1. **Build the project**:
   ```bash
   mvn clean package
   ```

2. **Deploy to Azure**:
   ```bash
   func azure functionapp publish <function-app-name>
   ```

3. **Configure Application Settings**:
   - Set environment variables in Azure Portal
   - Configure connection strings
   - Set up monitoring

### CI/CD Pipeline

The project includes Azure DevOps pipeline configuration:

```yaml
# azure-pipelines.yml
trigger:
  - main

pool:
  vmImage: 'ubuntu-latest'

steps:
- task: Maven@3
  inputs:
    mavenPomFile: 'pom.xml'
    goals: 'clean package'
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the documentation

## 📚 Additional Resources

- [Azure Functions Documentation](https://docs.microsoft.com/en-us/azure/azure-functions/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Azure Blob Storage Documentation](https://docs.microsoft.com/en-us/azure/storage/blobs/)
- [Apache POI Documentation](https://poi.apache.org/)

---

**Version**: 1.0.0  
**Last Updated**: January 2024  
**Maintainer**: File Validation Team 