# File Validation Service

A Spring Boot application with Azure Functions HTTP trigger for Excel file validation with Azure Blob Storage and Azure SQL Database integration.

## 🚀 Features

- **Excel File Validation**: Comprehensive validation of Excel files with row-level error tracking
- **Data Type Validation**: Email, date, name, and ID validation with configurable rules
- **Azure Integration**: Seamless integration with Azure Blob Storage and Azure SQL Database
- **Error Reporting**: Detailed error reporting with row-level validation errors
- **Modular Architecture**: Well-structured, modular codebase with proper separation of concerns
- **Comprehensive Logging**: Detailed logging with Logback configuration
- **Unit Testing**: Extensive unit tests with high code coverage
- **Exception Handling**: Custom exception classes for different error scenarios

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Azure subscription with:
  - Azure Blob Storage account
  - Azure SQL Database
  - Azure Functions (for deployment)

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd file-validation-service
```

### 2. Configure Azure Services

#### Azure Blob Storage Setup
1. Create an Azure Storage account
2. Create a container named `file-uploads`
3. Get the connection string from Azure Portal

#### Azure SQL Database Setup
1. Create an Azure SQL Database
2. Note the server name, database name, username, and password
3. Configure firewall rules to allow your IP

### 3. Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Azure Blob Storage Configuration
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=yourstorageaccount;AccountKey=yourkey;EndpointSuffix=core.windows.net
AZURE_STORAGE_ACCOUNT_NAME=yourstorageaccount

# Azure SQL Database Configuration
AZURE_SQL_CONNECTION_STRING=jdbc:sqlserver://your-server.database.windows.net:1433;database=your-database;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
AZURE_SQL_USERNAME=your-username
AZURE_SQL_PASSWORD=your-password
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/filevalidation/
│   │   ├── exception/           # Custom exception classes
│   │   ├── function/            # Azure Functions HTTP triggers
│   │   ├── model/               # Entity and model classes
│   │   ├── repository/          # JPA repository interfaces
│   │   ├── service/             # Service interfaces
│   │   │   └── impl/           # Service implementations
│   │   └── FileValidationApplication.java
│   └── resources/
│       ├── application.properties
│       └── logback-spring.xml
└── test/
    └── java/com/example/filevalidation/
        └── service/             # Unit tests
```

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# File Validation Configuration
file.validation.max-file-size=10MB
file.validation.supported-formats=xlsx,xls
file.validation.max-rows=10000
file.validation.error-column-name=Validation_Errors

# Azure Configuration
azure.storage.container-name=file-uploads
azure.functions.enabled=true
```

### Logging Configuration

The application uses Logback for logging with separate log files for:
- Application logs: `logs/file-validation-service.log`
- Error logs: `logs/error.log`
- Validation logs: `logs/validation.log`

## 📊 API Endpoints

### File Validation

**POST** `/api/validate-file`

Upload and validate an Excel file.

**Request:**
- Content-Type: `multipart/form-data`
- Body: Excel file (.xlsx or .xls)

**Response:**
```json
{
  "success": true,
  "status": "SUCCESS_WITH_ERRORS",
  "filename": "data.xlsx",
  "rowsProcessed": 100,
  "rowsWithErrors": 5,
  "downloadUrl": "https://...",
  "errorFileUrl": "https://...",
  "errors": [
    {
      "rowNumber": 2,
      "columnName": "Email",
      "errorMessage": "Invalid email format",
      "invalidValue": "invalid-email"
    }
  ]
}
```

**Error File Features:**
- When validation errors are found, a separate error file is created
- Error file contains only rows with validation errors
- Includes instructions for users to fix errors
- Users can download, fix, and re-upload the corrected file

### Health Check

**GET** `/api/health`

Check service health status.

**Response:**
```json
{
  "status": "healthy",
  "timestamp": 1640995200000,
  "service": "file-validation-service",
  "version": "1.0.0"
}
```

### Statistics

**GET** `/api/statistics`

Get processing statistics.

**Response:**
```json
{
  "totalFilesProcessed": 150,
  "filesWithErrors": 10,
  "filesSuccessfullyProcessed": 140,
  "totalRowsProcessed": 15000,
  "totalRowsWithErrors": 500
}
```

## 🔍 Validation Rules

### Email Validation
- Must be a valid email format
- Supports international domains
- Allows common email patterns (user+tag@domain.com)

### Date Validation
- Default format: `yyyy-MM-dd`
- Validates leap years
- Checks for valid month/day combinations

### Name Validation
- Must contain only letters and spaces
- Supports international characters
- No numbers or special characters allowed

### ID Validation
- Must be alphanumeric
- No spaces or special characters
- Case-insensitive

### Optional Columns
The following columns are not validated:
- Notes
- Comments
- Description

## 📋 Error File Workflow

When validation errors are detected:

1. **Error File Creation**: A separate Excel file is created containing only rows with errors
2. **Instructions Included**: The error file includes clear instructions for users
3. **Download Link**: Users receive a download URL for the error file
4. **Correction Process**: Users can download, fix errors, and re-upload
5. **Re-validation**: The corrected file goes through the same validation process

**Error File Features:**
- Contains only problematic rows for easy identification
- Includes original data with error indicators
- Clear instructions for correction
- Maintains original file structure

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run with Coverage

```bash
mvn test jacoco:report
```

Coverage report will be generated in `target/site/jacoco/index.html`

### Test Coverage

The project includes comprehensive unit tests covering:
- Email validation (valid and invalid formats)
- Date validation (valid and invalid formats)
- Name validation (valid and invalid formats)
- ID validation (valid and invalid formats)
- File format validation
- Error handling scenarios

## 🚀 Deployment

### Local Development

```bash
mvn spring-boot:run
```

### Azure Functions Deployment

#### Option 1: Using IntelliJ IDEA (Recommended)

1. **Install Azure Toolkit for IntelliJ**
   - Go to File → Settings → Plugins
   - Search for "Azure Toolkit for IntelliJ"
   - Install and restart IntelliJ

2. **Sign in to Azure**
   - Go to Tools → Azure → Azure Sign In
   - Follow the browser authentication

3. **Deploy using IntelliJ**
   - Right-click on project → Azure → Deploy to Azure Functions
   - Choose your Function App or create a new one
   - Click "Deploy"

#### Option 2: Using Azure CLI

```bash
# Login to Azure
az login

# Create Function App (if not exists)
az functionapp create --name your-function-app --storage-account your-storage-account --consumption-plan-location eastus --resource-group your-resource-group --runtime java --functions-version 4

# Deploy the function
mvn clean package
func azure functionapp publish your-function-app
```

#### Option 3: Using Azure Functions Core Tools

```bash
# Install Azure Functions Core Tools
npm install -g azure-functions-core-tools@4 --unsafe-perm true

# Login to Azure
func azure login

# Deploy the function
func azure functionapp publish your-function-app
```

### Continuous Deployment

#### GitHub Actions
- Push to `main` branch to trigger automatic deployment
- See `.github/workflows/deploy.yml` for configuration

#### Azure DevOps
- Use `azure-pipelines.yml` for Azure DevOps deployment
- Configure build pipeline in Azure DevOps

### Detailed Deployment Guide

For comprehensive deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md)

## 📝 Logging

The application uses structured logging with different log levels:

- **DEBUG**: Detailed validation information
- **INFO**: General application flow
- **WARN**: Non-critical issues
- **ERROR**: Errors and exceptions

Log files are automatically rotated and archived.

## 🔧 Customization

### Adding New Validation Rules

1. Add new validation type to `ValidationService.ValidationType`
2. Implement validation logic in `ValidationServiceImpl`
3. Add unit tests for the new validation rule

### Modifying Validation Rules

Edit the validation patterns in `ValidationServiceImpl`:

```java
private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
);
```

### Adding New Required Columns

Update the `REQUIRED_COLUMNS` list in `ValidationServiceImpl`:

```java
private static final List<String> REQUIRED_COLUMNS = Arrays.asList(
    "Email", "Date", "Name", "ID", "NewColumn"
);
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add unit tests
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the unit tests for usage examples

## 📈 Performance Considerations

- Maximum file size: 10MB (configurable)
- Maximum rows: 10,000 (configurable)
- Supported formats: .xlsx, .xls
- Azure Blob Storage for file storage
- Azure SQL Database for metadata storage

## 🔒 Security

- Input validation on all endpoints
- Secure file handling
- Azure SAS tokens for secure file access
- Comprehensive error handling without information leakage 