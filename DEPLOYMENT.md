# Deployment Guide

This guide provides step-by-step instructions for deploying the File Validation Service to Azure.

## 📋 Prerequisites

Before deploying, ensure you have:

- Azure subscription with admin access
- Azure CLI installed and configured
- Azure Functions Core Tools v4.x
- Maven 3.6+
- Java 11+

## 🏗️ Azure Resources Setup

### 1. Create Resource Group

```bash
az group create --name file-validation-rg --location eastus
```

### 2. Create Storage Account

```bash
# Create storage account
az storage account create \
  --name filevalidationstorage \
  --resource-group file-validation-rg \
  --location eastus \
  --sku Standard_LRS \
  --kind StorageV2

# Get connection string
az storage account show-connection-string \
  --name filevalidationstorage \
  --resource-group file-validation-rg
```

### 3. Create Blob Container

```bash
# Get storage account key
STORAGE_KEY=$(az storage account keys list \
  --account-name filevalidationstorage \
  --resource-group file-validation-rg \
  --query '[0].value' -o tsv)

# Create container
az storage container create \
  --name file-validation \
  --account-name filevalidationstorage \
  --account-key $STORAGE_KEY
```

### 4. Create Azure SQL Database

```bash
# Create SQL Server
az sql server create \
  --name file-validation-sql \
  --resource-group file-validation-rg \
  --location eastus \
  --admin-user sqladmin \
  --admin-password "YourStrongPassword123!"

# Create database
az sql db create \
  --resource-group file-validation-rg \
  --server file-validation-sql \
  --name FileValidationDB \
  --service-objective S0

# Get connection string
az sql db show-connection-string \
  --server file-validation-sql \
  --name FileValidationDB \
  --client ado.net
```

### 5. Configure SQL Server Firewall

```bash
# Allow Azure services
az sql server firewall-rule create \
  --resource-group file-validation-rg \
  --server file-validation-sql \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# Allow your IP (replace with your IP)
az sql server firewall-rule create \
  --resource-group file-validation-rg \
  --server file-validation-sql \
  --name AllowMyIP \
  --start-ip-address YOUR_IP_ADDRESS \
  --end-ip-address YOUR_IP_ADDRESS
```

## 🚀 Azure Functions Deployment

### 1. Create Function App

```bash
# Create function app
az functionapp create \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --consumption-plan-location eastus \
  --runtime java \
  --runtime-version 11 \
  --functions-version 4 \
  --storage-account filevalidationstorage \
  --os-type Linux
```

### 2. Configure Application Settings

```bash
# Get storage connection string
STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
  --name filevalidationstorage \
  --resource-group file-validation-rg \
  --query connectionString -o tsv)

# Get SQL connection string
SQL_CONNECTION_STRING=$(az sql db show-connection-string \
  --server file-validation-sql \
  --name FileValidationDB \
  --client ado.net | sed 's/<username>/sqladmin/' | sed 's/<password>/YourStrongPassword123!/')

# Set application settings
az functionapp config appsettings set \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --settings \
    AZURE_STORAGE_CONNECTION_STRING="$STORAGE_CONNECTION_STRING" \
    AZURE_STORAGE_CONTAINER="file-validation" \
    AZURE_SQL_CONNECTION_STRING="$SQL_CONNECTION_STRING" \
    AZURE_SQL_USERNAME="sqladmin" \
    AZURE_SQL_PASSWORD="YourStrongPassword123!" \
    FUNCTIONS_WORKER_RUNTIME="java" \
    WEBSITE_RUN_FROM_PACKAGE="1"
```

### 3. Deploy Function Code

```bash
# Build the project
mvn clean package

# Deploy to Azure Functions
func azure functionapp publish file-validation-function
```

## 🔧 Configuration

### Environment Variables

Set these environment variables in your Azure Function App:

| Variable | Description | Example |
|----------|-------------|---------|
| `AZURE_STORAGE_CONNECTION_STRING` | Azure Storage connection string | `DefaultEndpointsProtocol=https;AccountName=...` |
| `AZURE_STORAGE_CONTAINER` | Blob container name | `file-validation` |
| `AZURE_SQL_CONNECTION_STRING` | SQL Database connection string | `jdbc:sqlserver://...` |
| `AZURE_SQL_USERNAME` | SQL Database username | `sqladmin` |
| `AZURE_SQL_PASSWORD` | SQL Database password | `YourStrongPassword123!` |

### Application Settings

Configure these settings in Azure Portal:

1. Go to Azure Portal → Function App → Configuration
2. Add/Edit application settings
3. Save and restart the function app

## 📊 Monitoring Setup

### 1. Enable Application Insights

```bash
# Create Application Insights
az monitor app-insights component create \
  --app file-validation-insights \
  --location eastus \
  --resource-group file-validation-rg \
  --application-type web

# Get instrumentation key
INSTRUMENTATION_KEY=$(az monitor app-insights component show \
  --app file-validation-insights \
  --resource-group file-validation-rg \
  --query instrumentationKey -o tsv)

# Add to function app settings
az functionapp config appsettings set \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --settings APPINSIGHTS_INSTRUMENTATIONKEY="$INSTRUMENTATION_KEY"
```

### 2. Configure Logging

```bash
# Enable detailed logging
az functionapp config appsettings set \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --settings \
    WEBSITE_ENABLE_APP_SERVICE_LOG="true" \
    WEBSITE_APP_SERVICE_LOG_LEVEL="Information"
```

## 🔒 Security Configuration

### 1. Enable Authentication

```bash
# Enable Azure AD authentication
az functionapp config set \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --generic-configurations '{"authSettings":{"enabled":true,"unauthenticatedClientAction":"RedirectToLoginPage"}}'
```

### 2. Configure CORS (Optional)

```bash
# Allow specific origins if needed
az functionapp cors add \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --allowed-origins "https://your-api-client.azurewebsites.net"
```

## 🧪 Testing Deployment

### 1. Test Health Endpoint

```bash
# Get function app URL
FUNCTION_URL=$(az functionapp show \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --query defaultHostName -o tsv)

# Test health endpoint
curl "https://$FUNCTION_URL/api/health"
```

### 2. Test Validation Endpoint

```bash
# Test validation endpoint
curl -X POST "https://$FUNCTION_URL/api/validate" \
  -H "Content-Type: application/json" \
  -d '{"fileMetadataId": 1}'
```

## 🔄 CI/CD Pipeline

### Azure DevOps Pipeline

Create `azure-pipelines.yml`:

```yaml
trigger:
  - main

pool:
  vmImage: 'ubuntu-latest'

variables:
  functionAppName: 'file-validation-function'
  resourceGroupName: 'file-validation-rg'

steps:
- task: JavaToolInstaller@0
  inputs:
    versionSpec: '11'
    jdkArchitectureOption: 'x64'

- task: Maven@3
  inputs:
    mavenPomFile: 'pom.xml'
    goals: 'clean package'
    publishJUnitResults: true
    testResultsFiles: '**/surefire-reports/TEST-*.xml'
    javaHomeOption: 'JDKVersion'
    jdkVersionOption: '1.11'

- task: ArchiveFiles@2
  inputs:
    rootFolderOrFile: '$(System.DefaultWorkingDirectory)/target'
    includeRootFolder: false
    archiveType: 'zip'
    archiveFile: '$(Build.ArtifactStagingDirectory)/$(Build.BuildId).zip'
    replaceExistingArchive: true

- task: AzureFunctionApp@1
  inputs:
    azureSubscription: 'Your-Azure-Subscription'
    appName: '$(functionAppName)'
    package: '$(Build.ArtifactStagingDirectory)/$(Build.BuildId).zip'
    resourceGroupName: '$(resourceGroupName)'
```

### GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Azure Functions

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Build with Maven
      run: mvn clean package
    
    - name: Deploy to Azure Functions
      uses: Azure/functions-action@v1
      with:
        app-name: 'file-validation-function'
        package: 'target/file-validation-service-1.0.0.jar'
        publish-profile: ${{ secrets.AZURE_FUNCTIONAPP_PUBLISH_PROFILE }}
```

## 🚨 Troubleshooting

### Common Issues

1. **Function App Not Starting**
   ```bash
   # Check logs
   az functionapp logs tail --name file-validation-function --resource-group file-validation-rg
   ```

2. **Database Connection Issues**
   ```bash
   # Test database connectivity
   az sql db show --name FileValidationDB --server file-validation-sql --resource-group file-validation-rg
   ```

3. **Storage Access Issues**
   ```bash
   # Test storage access
   az storage blob list --container-name file-validation --account-name filevalidationstorage
   ```

### Log Analysis

```bash
# Get function app logs
az functionapp logs download --name file-validation-function --resource-group file-validation-rg

# Stream logs
az functionapp logs tail --name file-validation-function --resource-group file-validation-rg
```

## 📈 Performance Optimization

### 1. Scale Configuration

```bash
# Configure scaling
az functionapp plan update \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --max-burst 200 \
  --min-instances 1 \
  --max-instances 10
```

### 2. Memory Configuration

```bash
# Set memory limits
az functionapp config appsettings set \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --settings \
    WEBSITE_MEMORY_LIMIT_MB="2048" \
    JAVA_OPTS="-Xmx1536m"
```

## 🔄 Update Deployment

### 1. Update Function Code

```bash
# Build and deploy updates
mvn clean package
func azure functionapp publish file-validation-function
```

### 2. Update Configuration

```bash
# Update application settings
az functionapp config appsettings set \
  --name file-validation-function \
  --resource-group file-validation-rg \
  --settings NEW_SETTING="new_value"
```

## 🧹 Cleanup

To remove all resources:

```bash
# Delete resource group (removes all resources)
az group delete --name file-validation-rg --yes
```

## 📚 Additional Resources

- [Azure Functions Documentation](https://docs.microsoft.com/en-us/azure/azure-functions/)
- [Azure CLI Documentation](https://docs.microsoft.com/en-us/cli/azure/)
- [Azure DevOps Pipelines](https://docs.microsoft.com/en-us/azure/devops/pipelines/)
- [GitHub Actions](https://docs.github.com/en/actions)

---

**Note**: Replace placeholder values (like `YourStrongPassword123!`, `YOUR_IP_ADDRESS`) with your actual values before running the commands. 