package com.example.filevalidation.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * Azure Blob Storage Service Interface
 * 
 * This interface defines the contract for Azure Blob Storage operations
 * including file upload, download, and management functionality.
 * 
 * Business Operations:
 * - File upload to Azure Blob Storage
 * - File download from Azure Blob Storage
 * - File deletion from Azure Blob Storage
 * - File metadata management
 * - Container management
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface AzureBlobService {

    /**
     * Uploads a file to Azure Blob Storage
     * 
     * @param file The file to upload
     * @param containerName The container name (optional, uses default if null)
     * @return The blob name (unique identifier) of the uploaded file
     * @throws com.example.filevalidation.exception.FileValidationException if upload fails
     */
    String uploadFile(MultipartFile file, String containerName);

    /**
     * Uploads a file to Azure Blob Storage with custom blob name
     * 
     * @param file The file to upload
     * @param blobName Custom blob name for the file
     * @param containerName The container name (optional, uses default if null)
     * @return The blob name of the uploaded file
     * @throws com.example.filevalidation.exception.FileValidationException if upload fails
     */
    String uploadFile(MultipartFile file, String blobName, String containerName);

    /**
     * Downloads a file from Azure Blob Storage
     * 
     * @param blobName The blob name of the file to download
     * @param containerName The container name (optional, uses default if null)
     * @return InputStream containing the file data
     * @throws com.example.filevalidation.exception.FileValidationException if download fails
     */
    InputStream downloadFile(String blobName, String containerName);

    /**
     * Downloads a file from Azure Blob Storage using default container
     * 
     * @param blobName The blob name of the file to download
     * @return InputStream containing the file data
     * @throws com.example.filevalidation.exception.FileValidationException if download fails
     */
    InputStream downloadFile(String blobName);

    /**
     * Deletes a file from Azure Blob Storage
     * 
     * @param blobName The blob name of the file to delete
     * @param containerName The container name (optional, uses default if null)
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteFile(String blobName, String containerName);

    /**
     * Deletes a file from Azure Blob Storage using default container
     * 
     * @param blobName The blob name of the file to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteFile(String blobName);

    /**
     * Gets the download URL for a file
     * 
     * @param blobName The blob name of the file
     * @param containerName The container name (optional, uses default if null)
     * @return The download URL for the file
     */
    String getDownloadUrl(String blobName, String containerName);

    /**
     * Gets the download URL for a file using default container
     * 
     * @param blobName The blob name of the file
     * @return The download URL for the file
     */
    String getDownloadUrl(String blobName);

    /**
     * Checks if a file exists in Azure Blob Storage
     * 
     * @param blobName The blob name of the file
     * @param containerName The container name (optional, uses default if null)
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String blobName, String containerName);

    /**
     * Checks if a file exists in Azure Blob Storage using default container
     * 
     * @param blobName The blob name of the file
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String blobName);

    /**
     * Lists all files in a container
     * 
     * @param containerName The container name (optional, uses default if null)
     * @return List of blob names in the container
     */
    List<String> listFiles(String containerName);

    /**
     * Lists all files in the default container
     * 
     * @return List of blob names in the default container
     */
    List<String> listFiles();

    /**
     * Gets the file size of a blob
     * 
     * @param blobName The blob name of the file
     * @param containerName The container name (optional, uses default if null)
     * @return The file size in bytes
     */
    long getFileSize(String blobName, String containerName);

    /**
     * Gets the file size of a blob using default container
     * 
     * @param blobName The blob name of the file
     * @return The file size in bytes
     */
    long getFileSize(String blobName);
} 