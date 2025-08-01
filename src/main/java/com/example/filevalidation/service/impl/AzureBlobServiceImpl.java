package com.example.filevalidation.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.example.filevalidation.exception.FileValidationException;
import com.example.filevalidation.service.AzureBlobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Azure Blob Storage Service Implementation
 * 
 * This class implements the AzureBlobService interface and provides
 * comprehensive Azure Blob Storage operations using the Azure SDK.
 * 
 * Features:
 * - File upload with automatic blob naming
 * - File download with error handling
 * - File deletion with existence checks
 * - Container management
 * - URL generation for file access
 * - Comprehensive logging and error handling
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class AzureBlobServiceImpl implements AzureBlobService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String defaultContainerName;

    @Value("${azure.storage.account-name}")
    private String accountName;

    private BlobServiceClient blobServiceClient;

    /**
     * Initializes the Azure Blob Service Client
     */
    public AzureBlobServiceImpl() {
        initializeBlobServiceClient();
    }

    @Override
    public String uploadFile(MultipartFile file, String containerName) {
        String blobName = generateBlobName(file.getOriginalFilename());
        return uploadFile(file, blobName, containerName);
    }

    @Override
    public String uploadFile(MultipartFile file, String blobName, String containerName) {
        log.info("Starting file upload to Azure Blob Storage. File: {}, Blob: {}, Container: {}", 
                file.getOriginalFilename(), blobName, containerName);

        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Upload the file
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            log.info("File uploaded successfully. Blob name: {}", blobName);
            return blobName;

        } catch (Exception e) {
            log.error("Error uploading file to Azure Blob Storage: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to upload file to Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String blobName, String containerName) {
        log.info("Starting file download from Azure Blob Storage. Blob: {}, Container: {}", 
                blobName, containerName);

        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new FileValidationException("File not found in Azure Blob Storage: " + blobName);
            }

            InputStream inputStream = blobClient.openInputStream();
            log.info("File download started successfully. Blob name: {}", blobName);
            return inputStream;

        } catch (Exception e) {
            log.error("Error downloading file from Azure Blob Storage: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to download file from Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String blobName) {
        return downloadFile(blobName, null);
    }

    @Override
    public boolean deleteFile(String blobName, String containerName) {
        log.info("Starting file deletion from Azure Blob Storage. Blob: {}, Container: {}", 
                blobName, containerName);

        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                log.warn("File not found for deletion. Blob name: {}", blobName);
                return false;
            }

            boolean deleted = blobClient.deleteIfExists();
            log.info("File deletion completed. Blob name: {}, Deleted: {}", blobName, deleted);
            return deleted;

        } catch (Exception e) {
            log.error("Error deleting file from Azure Blob Storage: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteFile(String blobName) {
        return deleteFile(blobName, null);
    }

    @Override
    public String getDownloadUrl(String blobName, String containerName) {
        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new FileValidationException("File not found in Azure Blob Storage: " + blobName);
            }

            // Generate a SAS token for secure access (expires in 1 hour)
            String sasToken = blobClient.generateSas(com.azure.storage.common.sas.BlobSasPermission.parse("r"), 
                                                   java.time.OffsetDateTime.now().plusHours(1));

            String downloadUrl = blobClient.getBlobUrl() + "?" + sasToken;
            log.debug("Generated download URL for blob: {}", blobName);
            return downloadUrl;

        } catch (Exception e) {
            log.error("Error generating download URL: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to generate download URL: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDownloadUrl(String blobName) {
        return getDownloadUrl(blobName, null);
    }

    @Override
    public boolean fileExists(String blobName, String containerName) {
        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            boolean exists = blobClient.exists();
            log.debug("File existence check. Blob: {}, Container: {}, Exists: {}", 
                     blobName, targetContainer, exists);
            return exists;

        } catch (Exception e) {
            log.error("Error checking file existence: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String blobName) {
        return fileExists(blobName, null);
    }

    @Override
    public List<String> listFiles(String containerName) {
        log.info("Listing files in container: {}", containerName);

        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);

            List<String> blobNames = new ArrayList<>();
            for (BlobItem blobItem : containerClient.listBlobs()) {
                blobNames.add(blobItem.getName());
            }

            log.info("Found {} files in container: {}", blobNames.size(), targetContainer);
            return blobNames;

        } catch (Exception e) {
            log.error("Error listing files in container: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to list files in container: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listFiles() {
        return listFiles(null);
    }

    @Override
    public long getFileSize(String blobName, String containerName) {
        try {
            String targetContainer = containerName != null ? containerName : defaultContainerName;
            BlobContainerClient containerClient = getBlobContainerClient(targetContainer);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new FileValidationException("File not found in Azure Blob Storage: " + blobName);
            }

            long fileSize = blobClient.getProperties().getBlobSize();
            log.debug("File size retrieved. Blob: {}, Size: {} bytes", blobName, fileSize);
            return fileSize;

        } catch (Exception e) {
            log.error("Error getting file size: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to get file size: " + e.getMessage(), e);
        }
    }

    @Override
    public long getFileSize(String blobName) {
        return getFileSize(blobName, null);
    }

    /**
     * Initializes the Azure Blob Service Client
     */
    private void initializeBlobServiceClient() {
        try {
            blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

            log.info("Azure Blob Service Client initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing Azure Blob Service Client: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to initialize Azure Blob Service Client: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the Blob Container Client for the specified container
     * 
     * @param containerName Name of the container
     * @return BlobContainerClient for the container
     */
    private BlobContainerClient getBlobContainerClient(String containerName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                containerClient.create();
                log.info("Created new container: {}", containerName);
            }
            
            return containerClient;
        } catch (Exception e) {
            log.error("Error getting blob container client: {}", e.getMessage(), e);
            throw new FileValidationException("Failed to get blob container client: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a unique blob name for the uploaded file
     * 
     * @param originalFilename Original filename
     * @return Unique blob name
     */
    private String generateBlobName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return String.format("file_%s_%s%s", timestamp, uuid, extension);
    }
} 