# Azure Functions Deployment Guide

This guide provides step-by-step instructions for deploying the File Validation Service to Azure Functions using IntelliJ IDEA.

## 📋 Prerequisites

### Required Software
- **IntelliJ IDEA** (Ultimate or Community Edition)
- **Java 11** or higher
- **Maven** 3.6 or higher
- **Azure CLI** installed and configured
- **Azure Functions Core Tools** (optional, for local testing)

### Required Azure Resources
- **Azure Subscription** with billing enabled
- **Azure Storage Account** for blob storage
- **Azure SQL Database** for metadata storage
- **Azure Function App** (will be created during deployment)

## 🚀 Deployment Steps

### Step 1: Install Azure Toolkit for IntelliJ

1. **Open IntelliJ IDEA**
2. **Go to File → Settings → Plugins**
3. **Search for "Azure Toolkit for IntelliJ"**
4. **Click Install** and restart IntelliJ

### Step 2: Sign in to Azure

1. **Go to Tools → Azure → Azure Sign In**
2. **Click "Sign in"** and follow the browser authentication
3. **Verify your subscription** appears in the Azure Explorer

### Step 3: Configure Azure Resources

#### 3.1 Create Azure Storage Account

1. **Go to Azure Portal** (portal.azure.com)
2. **Create a new Storage Account:**
   ```
   Resource Group: your-resource-group
   Storage Account Name: yourstorageaccount
   Location: East US
   Performance: Standard
   Redundancy: LRS
   ```
3. **After creation, get the connection string:**
   - Go to **Access Keys**
   - Copy the **Connection String**

#### 3.2 Create Azure SQL Database

1. **Create a new SQL Database:**
   ```
   Resource Group: your-resource-group
   Database Name: file-validation-db
   Server: Create new server
   Location: East US
   ```
2. **Configure firewall rules** to allow your IP
3. **Note the connection details:**
   - Server name
   - Database name
   - Username
   - Password

#### 3.3 Create Azure Function App

1. **In IntelliJ, go to Tools → Azure → Deploy to Azure Functions**
2. **Click "Create Function App"**
3. **Fill in the details:**
   ```
   App Name: file-validation-function-app
   Resource Group: your-resource-group
   Location: East US
   Runtime Stack: Java 11
   Hosting Plan: Consumption
   Storage Account: yourfunctionstorageaccount
   ```

### Step 4: Configure Environment Variables

#### 4.1 Update local.settings.json

Replace the placeholder values in `local.settings.json`:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "AZURE_STORAGE_CONNECTION_STRING": "YOUR_ACTUAL_STORAGE_CONNECTION_STRING",
    "AZURE_STORAGE_ACCOUNT_NAME": "yourstorageaccount",
    "AZURE_SQL_CONNECTION_STRING": "YOUR_ACTUAL_SQL_CONNECTION_STRING",
    "AZURE_SQL_USERNAME": "your-sql-username",
    "AZURE_SQL_PASSWORD": "your-sql-password",
    "AZURE_FUNCTION_APP_NAME": "file-validation-function-app",
    "AZURE_RESOURCE_GROUP": "your-resource-group",
    "AZURE_REGION": "eastus",
    "AZURE_FUNCTION_PLAN": "consumption",
    "AZURE_FUNCTION_STORAGE_ACCOUNT": "yourfunctionstorageaccount"
  }
}
```

#### 4.2 Update application.properties

Replace placeholder values in `src/main/resources/application.properties`:

```properties
# Azure Blob Storage Configuration
azure.storage.connection-string=${AZURE_STORAGE_CONNECTION_STRING}
azure.storage.account-name=${AZURE_STORAGE_ACCOUNT_NAME}

# Azure SQL Database Configuration
spring.datasource.url=${AZURE_SQL_CONNECTION_STRING}
spring.datasource.username=${AZURE_SQL_USERNAME}
spring.datasource.password=${AZURE_SQL_PASSWORD}
```

### Step 5: Build the Project

1. **Open Terminal in IntelliJ** (Alt+F12)
2. **Run Maven build:**
   ```bash
   mvn clean package
   ```
3. **Verify the build succeeds** and creates the JAR file

### Step 6: Deploy to Azure Functions

#### Method 1: Using IntelliJ Azure Toolkit

1. **Right-click on the project** in Project Explorer
2. **Select "Azure → Deploy to Azure Functions"**
3. **Choose your Function App** from the list
4. **Click "Deploy"**
5. **Wait for deployment to complete**

#### Method 2: Using Azure CLI

1. **Open Terminal in IntelliJ**
2. **Login to Azure:**
   ```bash
   az login
   ```
3. **Set the subscription:**
   ```bash
   az account set --subscription "your-subscription-id"
   ```
4. **Deploy the function:**
   ```bash
   az functionapp deployment source config-zip \
     --resource-group your-resource-group \
     --name file-validation-function-app \
     --src target/file-validation-service-1.0.0.jar
   ```

#### Method 3: Using Azure Functions Core Tools

1. **Install Azure Functions Core Tools:**
   ```bash
   npm install -g azure-functions-core-tools@4 --unsafe-perm true
   ```
2. **Login to Azure:**
   ```bash
   func azure login
   ```
3. **Deploy the function:**
   ```bash
   func azure functionapp publish file-validation-function-app
   ```

### Step 7: Configure Application Settings

1. **Go to Azure Portal**
2. **Navigate to your Function App**
3. **Go to Configuration → Application Settings**
4. **Add the following settings:**

```
AZURE_STORAGE_CONNECTION_STRING = YOUR_STORAGE_CONNECTION_STRING
AZURE_STORAGE_ACCOUNT_NAME = yourstorageaccount
AZURE_SQL_CONNECTION_STRING = YOUR_SQL_CONNECTION_STRING
AZURE_SQL_USERNAME = your-sql-username
AZURE_SQL_PASSWORD = your-sql-password
```

5. **Click "Save"**

### Step 8: Test the Deployment

1. **Get the Function App URL** from Azure Portal
2. **Test the health endpoint:**
   ```bash
   curl https://file-validation-function-app.azurewebsites.net/api/health
   ```
3. **Test file validation:**
   ```bash
   curl -X POST \
     -F "file=@test-file.xlsx" \
     https://file-validation-function-app.azurewebsites.net/api/validate-file
   ```

## 🔧 Troubleshooting

### Common Issues

#### 1. Build Failures
- **Issue:** Maven build fails
- **Solution:** Check Java version and Maven configuration
- **Command:** `mvn clean package -X`

#### 2. Deployment Failures
- **Issue:** Function App deployment fails
- **Solution:** Check Azure credentials and resource group permissions
- **Command:** `az account show`

#### 3. Runtime Errors
- **Issue:** Function fails to start
- **Solution:** Check application settings and connection strings
- **Logs:** Check Function App logs in Azure Portal

#### 4. Database Connection Issues
- **Issue:** Cannot connect to SQL Database
- **Solution:** 
  - Check firewall rules
  - Verify connection string format
  - Ensure database exists

#### 5. Storage Connection Issues
- **Issue:** Cannot access Blob Storage
- **Solution:**
  - Verify storage account exists
  - Check connection string format
  - Ensure container exists

### Debugging Steps

1. **Check Function App Logs:**
   - Go to Azure Portal → Function App → Monitor
   - View recent executions and logs

2. **Test Locally:**
   ```bash
   mvn spring-boot:run
   ```

3. **Check Application Insights:**
   - Enable Application Insights in Function App
   - Monitor performance and errors

## 📊 Monitoring and Maintenance

### Application Monitoring

1. **Enable Application Insights:**
   - Go to Function App → Application Insights
   - Click "Enable Application Insights"

2. **Set up Alerts:**
   - Go to Application Insights → Alerts
   - Create alerts for errors and performance

3. **Monitor Logs:**
   - Use Azure Monitor for centralized logging
   - Set up log analytics workspace

### Performance Optimization

1. **Function App Scaling:**
   - Monitor CPU and memory usage
   - Adjust app service plan if needed

2. **Database Optimization:**
   - Monitor SQL Database performance
   - Consider scaling up for high load

3. **Storage Optimization:**
   - Monitor blob storage usage
   - Implement lifecycle policies

## 🔄 Continuous Deployment

### GitHub Actions (Recommended)

1. **Create .github/workflows/deploy.yml:**
   ```yaml
   name: Deploy to Azure Functions
   on:
     push:
       branches: [ main ]
   
   jobs:
     deploy:
       runs-on: ubuntu-latest
       steps:
       - uses: actions/checkout@v2
       
       - name: Set up Java
         uses: actions/setup-java@v2
         with:
           java-version: '11'
           
       - name: Build with Maven
         run: mvn clean package
         
       - name: Deploy to Azure Functions
         uses: Azure/functions-action@v1
         with:
           app-name: 'file-validation-function-app'
           package: './target/file-validation-service-1.0.0.jar'
           publish-profile: ${{ secrets.AZURE_FUNCTIONAPP_PUBLISH_PROFILE }}
   ```

2. **Add secrets to GitHub repository**
3. **Push to main branch to trigger deployment**

### Azure DevOps Pipeline

1. **Create azure-pipelines.yml:**
   ```yaml
   trigger:
   - main
   
   pool:
     vmImage: 'ubuntu-latest'
   
   steps:
   - task: JavaToolInstaller@0
     inputs:
       versionSpec: '11'
       
   - task: Maven@3
     inputs:
       mavenPomFile: 'pom.xml'
       goals: 'clean package'
       
   - task: ArchiveFiles@2
     inputs:
       rootFolderOrFile: 'target'
       includeRootFolder: false
       archiveType: 'zip'
       archiveFile: '$(Build.ArtifactStagingDirectory)/$(Build.BuildId).zip'
       
   - task: AzureFunctionApp@1
     inputs:
       azureSubscription: 'Your-Azure-Subscription'
       appName: 'file-validation-function-app'
       package: '$(Build.ArtifactStagingDirectory)/$(Build.BuildId).zip'
   ```

## 📝 Post-Deployment Checklist

- [ ] Function App is running and accessible
- [ ] Health endpoint returns 200 OK
- [ ] File validation endpoint accepts uploads
- [ ] Azure Blob Storage is accessible
- [ ] Azure SQL Database is connected
- [ ] Application Insights is enabled
- [ ] Error monitoring is configured
- [ ] Performance monitoring is set up
- [ ] Backup strategy is implemented
- [ ] Security policies are configured

## 🆘 Support

For deployment issues:
1. Check Azure Portal logs
2. Review Application Insights
3. Test locally first
4. Verify all connection strings
5. Check Azure resource permissions

## 📚 Additional Resources

- [Azure Functions Documentation](https://docs.microsoft.com/en-us/azure/azure-functions/)
- [Azure Toolkit for IntelliJ](https://docs.microsoft.com/en-us/azure/developer/java/toolkit-for-intellij/)
- [Spring Boot on Azure Functions](https://docs.microsoft.com/en-us/azure/azure-functions/functions-reference-java)
- [Azure CLI Documentation](https://docs.microsoft.com/en-us/cli/azure/) 