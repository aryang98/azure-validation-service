package com.validation.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.validation.dto.ValidationRequest;
import com.validation.dto.ValidationResponse;
import com.validation.handler.ValidationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FileValidationFunctionTest {
    
    @Mock
    private HttpRequestMessage<Optional<String>> mockRequest;
    
    @Mock
    private ValidationHandler mockHandler;
    
    private FileValidationFunction function;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        function = new FileValidationFunction(mockHandler, objectMapper);
    }
    
    @Test
    void testSuccessfulValidation() throws Exception {
        // Arrange
        ValidationRequest request = new ValidationRequest("test.xlsx");
        ValidationResponse expectedResponse = new ValidationResponse(
                "SUCCESS",
                "File validation completed successfully",
                100,
                95,
                5,
                "https://example.com/download"
        );
        
        when(mockRequest.getBody()).thenReturn(Optional.of("{\"filename\":\"test.xlsx\"}"));
        when(mockHandler.parseRequest(any())).thenReturn(request);
        when(mockHandler.handleValidationRequest(any())).thenReturn(expectedResponse);
        
        // Act
        HttpResponseMessage response = function.run(mockRequest, null);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
    }
    
    @Test
    void testMissingRequestBody() {
        // Arrange
        when(mockRequest.getBody()).thenReturn(Optional.empty());
        
        // Act
        HttpResponseMessage response = function.run(mockRequest, null);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }
    
    @Test
    void testInvalidJsonRequest() {
        // Arrange
        when(mockRequest.getBody()).thenReturn(Optional.of("invalid json"));
        
        // Act
        HttpResponseMessage response = function.run(mockRequest, null);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }
} 