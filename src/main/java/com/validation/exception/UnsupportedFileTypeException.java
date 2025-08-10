package com.validation.exception;

public class UnsupportedFileTypeException extends ValidationException {
    
    public UnsupportedFileTypeException(String fileType) {
        super("Unsupported file type: " + fileType + ". Only .xlsx and .csv files are supported.");
    }
} 