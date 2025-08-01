package com.example.filevalidation.exception;

/**
 * Validation Rule Exception
 * 
 * This exception is thrown when validation rules are violated
 * during the file validation process.
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class ValidationRuleException extends FileValidationException {

    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public ValidationRuleException(String message) {
        super(message, "VALIDATION_RULE_ERROR");
    }

    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Root cause of the exception
     */
    public ValidationRuleException(String message, Throwable cause) {
        super(message, cause, "VALIDATION_RULE_ERROR");
    }

    /**
     * Constructor for specific validation rule violation
     * 
     * @param columnName Name of the column being validated
     * @param rowNumber Row number where the violation occurred
     * @param invalidValue The invalid value that caused the violation
     * @param ruleDescription Description of the validation rule
     */
    public ValidationRuleException(String columnName, int rowNumber, String invalidValue, String ruleDescription) {
        super(String.format("Validation rule violation in column '%s' at row %d. Value '%s' violates rule: %s", 
                          columnName, rowNumber, invalidValue, ruleDescription), "VALIDATION_RULE_ERROR");
    }
} 