package com.validation.exception;

public class FileNotFoundException extends ValidationException {
    
    public FileNotFoundException(String filename) {
        super("File not found: " + filename);
    }
    
    public FileNotFoundException(String filename, Throwable cause) {
        super("File not found: " + filename, cause);
    }
} 