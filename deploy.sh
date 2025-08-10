#!/bin/bash

# Azure File Validation Service Deployment Script
# This script automates the deployment of the validation service to Azure

set -e

# Configuration
RESOURCE_GROUP="file-validation-rg"
LOCATION="eastus"
STORAGE_ACCOUNT="filevalidationstorage"
FUNCTION_APP="file-validation-function"
SQL_SERVER="file-validation-sql"
SQL_DATABASE="FileValidationDB"
CONTAINER_NAME="file-validation"

echo "🚀 Starting Azure File Validation Service Deployment..."

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo "❌ Azure CLI is not installed. Please install it first."
    exit 1
fi

# Check if user is logged in
if ! az account show &> /dev/null; then
    echo "🔐 Please log in to Azure..."
    az login
fi

# Create resource group
echo "📦 Creating resource group..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create storage account
echo "💾 Creating storage account..."
az storage account create \
    --name $STORAGE_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --sku Standard_LRS \
    --kind StorageV2

# Get storage account key
STORAGE_KEY=$(az storage account keys list \
    --resource-group $RESOURCE_GROUP \
    --account-name $STORAGE_ACCOUNT \
    --query '[0].value' \
    --output tsv)

# Create blob container
echo "📁 Creating blob container..."
az storage container create \
    --name $CONTAINER_NAME \
    --account-name $STORAGE_ACCOUNT \
    --account-key $STORAGE_KEY

# Create SQL Server
echo "🗄️ Creating SQL Server..."
az sql server create \
    --name $SQL_SERVER \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --admin-user sqladmin \
    --admin-password "YourStrongPassword123!"

# Create SQL Database
echo "📊 Creating SQL Database..."
az sql db create \
    --resource-group $RESOURCE_GROUP \
    --server $SQL_SERVER \
    --name $SQL_DATABASE \
    --edition Basic \
    --capacity 5

# Configure firewall rule
echo "🔥 Configuring SQL Server firewall..."
az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQL_SERVER \
    --name AllowAzureServices \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0

# Create App Service Plan
echo "📋 Creating App Service Plan..."
az appservice plan create \
    --name "${FUNCTION_APP}-plan" \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --sku B1 \
    --is-linux

# Create Function App
echo "⚡ Creating Function App..."
az functionapp create \
    --name $FUNCTION_APP \
    --resource-group $RESOURCE_GROUP \
    --plan "${FUNCTION_APP}-plan" \
    --runtime java \
    --runtime-version 11 \
    --functions-version 4 \
    --storage-account $STORAGE_ACCOUNT

# Build the project
echo "🔨 Building the project..."
mvn clean package

# Deploy the function
echo "🚀 Deploying the function..."
az functionapp deployment source config-zip \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP \
    --src target/azure-functions/file-validation-service-1.0.0.zip

# Configure application settings
echo "⚙️ Configuring application settings..."
az functionapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP \
    --settings \
    BLOB_CONNECTION_STRING="DefaultEndpointsProtocol=https;AccountName=${STORAGE_ACCOUNT};AccountKey=${STORAGE_KEY};EndpointSuffix=core.windows.net" \
    BLOB_CONTAINER_NAME="${CONTAINER_NAME}" \
    SQL_CONNECTION_STRING="jdbc:sqlserver://${SQL_SERVER}.database.windows.net:1433;database=${SQL_DATABASE};user=sqladmin;password=YourStrongPassword123!;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;" \
    SAS_EXPIRY_HOURS="24" \
    MAX_FILE_SIZE_MB="100" \
    REQUIRED_COLUMNS="id,date,sellType,name,price"

# Get function URL
FUNCTION_URL=$(az functionapp function show \
    --resource-group $RESOURCE_GROUP \
    --name $FUNCTION_APP \
    --function-name validateFile \
    --query "invokeUrlTemplate" \
    --output tsv)

echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Deployment Summary:"
echo "Resource Group: $RESOURCE_GROUP"
echo "Storage Account: $STORAGE_ACCOUNT"
echo "SQL Server: $SQL_SERVER"
echo "SQL Database: $SQL_DATABASE"
echo "Function App: $FUNCTION_APP"
echo "Function URL: $FUNCTION_URL"
echo ""
echo "🧪 Test the function:"
echo "curl -X POST \"$FUNCTION_URL\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"filename\": \"your-file.xlsx\"}'"
echo ""
echo "⚠️  Important:"
echo "1. Update the SQL Server password in production"
echo "2. Configure proper firewall rules for SQL Server"
echo "3. Set up monitoring and alerting"
echo "4. Review and adjust the function timeout settings" 