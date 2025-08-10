package com.validation.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.validation.exception.FileNotFoundException;
import com.validation.service.BlobStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class BlobStorageServiceImpl implements BlobStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlobStorageServiceImpl.class);
    
    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient containerClient;
    
    @Value("${azure.blob.container-name}")
    private String containerName;
    
    @Value("${azure.blob.sas-expiry-hours}")
    private int sasExpiryHours;
    
    public BlobStorageServiceImpl(@Value("${azure.blob.connection-string}") String connectionString) {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }
    
    @Override
    public InputStream downloadFile(String filename) {
        BlobClient blobClient = containerClient.getBlobClient(filename);
        
        if (!blobClient.exists()) {
            throw new FileNotFoundException(filename);
        }
        
        logger.info("Downloading file: {}", filename);
        return blobClient.openInputStream();
    }
    
    @Override
    public void uploadFile(String filename, InputStream inputStream, long contentLength) {
        BlobClient blobClient = containerClient.getBlobClient(filename);
        
        logger.info("Uploading file: {} with size: {} bytes", filename, contentLength);
        blobClient.upload(inputStream, contentLength, true);
    }
    
    @Override
    public String generateSasUrl(String filename, int expiryHours) {
        BlobClient blobClient = containerClient.getBlobClient(filename);
        
        if (!blobClient.exists()) {
            throw new FileNotFoundException(filename);
        }
        
        BlobSasPermission permission = new BlobSasPermission()
                .setReadPermission(true);
        
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plus(Duration.ofHours(expiryHours)),
                permission
        );
        
        String sasToken = blobClient.generateSas(sasSignatureValues);
        String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;
        
        logger.info("Generated SAS URL for file: {} with expiry: {} hours", filename, expiryHours);
        return sasUrl;
    }
    
    @Override
    public boolean fileExists(String filename) {
        BlobClient blobClient = containerClient.getBlobClient(filename);
        return blobClient.exists();
    }
    
    @Override
    public long getFileSize(String filename) {
        BlobClient blobClient = containerClient.getBlobClient(filename);
        
        if (!blobClient.exists()) {
            throw new FileNotFoundException(filename);
        }
        
        return blobClient.getProperties().getBlobSize();
    }
} 