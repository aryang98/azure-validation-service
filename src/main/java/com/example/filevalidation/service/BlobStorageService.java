package com.example.filevalidation.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.example.filevalidation.exception.FileValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

/**
 * Service for Azure Blob Storage operations
 * Handles file download, upload, and URL generation
 */
@Service
@Slf4j
public class BlobStorageService {
    
    @Value("${azure.storage.connection-string}")
    private String connectionString;
    
    @Value("${azure.storage.container-name}")
    private String containerName;
    
    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;
    
    /**
     * Initialize blob service client
     */
    private void initializeClient() {
        if (blobServiceClient == null) {
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            containerClient = blobServiceClient.getBlobContainerClient(containerName);
        }
    }
    
    /**
     * Download file from blob storage using URL
     * @param fileUrl The URL of the file to download
     * @return InputStream containing the file data
     */
    public InputStream downloadFile(String fileUrl) {
        try {
            initializeClient();
            log.info("Downloading file from URL: {}", fileUrl);
            
            // Extract blob name from URL
            String blobName = extractBlobNameFromUrl(fileUrl);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                throw new FileValidationException("File not found in blob storage: " + blobName);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            
            log.info("Successfully downloaded file: {}", blobName);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (Exception e) {
            log.error("Error downloading file from URL: {}", fileUrl, e);
            throw new FileValidationException("Failed to download file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Upload file to blob storage
     * @param fileName The name of the file to upload
     * @param fileData The file data as byte array
     * @return The URL of the uploaded file
     */
    public String uploadFile(String fileName, byte[] fileData) {
        try {
            initializeClient();
            log.info("Uploading file: {}", fileName);
            
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(new ByteArrayInputStream(fileData), fileData.length, true);
            
            // Generate URL with SAS token for read access
            String fileUrl = generateFileUrl(fileName);
            
            log.info("Successfully uploaded file: {} with URL: {}", fileName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Error uploading file: {}", fileName, e);
            throw new FileValidationException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update existing file in blob storage
     * @param fileName The name of the file to update
     * @param fileData The new file data
     * @return The URL of the updated file
     */
    public String updateFile(String fileName, byte[] fileData) {
        try {
            initializeClient();
            log.info("Updating file: {}", fileName);
            
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(new ByteArrayInputStream(fileData), fileData.length, true);
            
            String fileUrl = generateFileUrl(fileName);
            
            log.info("Successfully updated file: {} with URL: {}", fileName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Error updating file: {}", fileName, e);
            throw new FileValidationException("Failed to update file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate file URL with SAS token
     * @param fileName The name of the file
     * @return The URL with SAS token
     */
    private String generateFileUrl(String fileName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            Duration expiryDuration = Duration.ofHours(24); // 24 hours expiry
            
            URL url = blobClient.generateSasUrl(expiryDuration);
            return url.toString();
            
        } catch (Exception e) {
            log.error("Error generating file URL for: {}", fileName, e);
            throw new FileValidationException("Failed to generate file URL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract blob name from file URL
     * @param fileUrl The file URL
     * @return The blob name
     */
    private String extractBlobNameFromUrl(String fileUrl) {
        try {
            // Remove the base URL and container name to get the blob name
            String baseUrl = blobServiceClient.getAccountUrl();
            String containerPrefix = "/" + containerName + "/";
            
            if (fileUrl.contains(containerPrefix)) {
                return fileUrl.substring(fileUrl.indexOf(containerPrefix) + containerPrefix.length());
            } else {
                // If it's a direct blob URL, extract the blob name
                String[] parts = fileUrl.split("/");
                return parts[parts.length - 1];
            }
        } catch (Exception e) {
            log.error("Error extracting blob name from URL: {}", fileUrl, e);
            throw new FileValidationException("Invalid file URL format: " + fileUrl);
        }
    }
} 