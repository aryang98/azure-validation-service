package com.example.filevalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class representing a sales record from the Excel file
 * Contains validation annotations for business rules
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesRecord {
    
    @NotBlank(message = "ID is required")
    private String id;
    
    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;
    
    @NotBlank(message = "Sale mode is required")
    private String saleMode; // "dine_in" or "take_away"
    
    @NotNull(message = "MIN_SALE amount is required")
    @DecimalMin(value = "0.01", message = "MIN_SALE must be greater than 0")
    private BigDecimal minSale;
    
    private String errorMessage;
    
    /**
     * Validates if the sale mode is valid
     * @return true if sale mode is valid, false otherwise
     */
    public boolean isValidSaleMode() {
        return "dine_in".equalsIgnoreCase(saleMode) || "take_away".equalsIgnoreCase(saleMode);
    }
    
    /**
     * Gets the error message for sale mode validation
     * @return error message for invalid sale mode
     */
    public String getSaleModeErrorMessage() {
        if (!isValidSaleMode()) {
            return "Sale mode must be either 'dine_in' or 'take_away'";
        }
        return null;
    }
} 