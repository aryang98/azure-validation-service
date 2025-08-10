package com.validation.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface BlobStorageService {
    
    InputStream downloadFile(String filename);
    
    void uploadFile(String filename, InputStream inputStream, long contentLength);
    
    String generateSasUrl(String filename, int expiryHours);
    
    boolean fileExists(String filename);
    
    long getFileSize(String filename);
} 