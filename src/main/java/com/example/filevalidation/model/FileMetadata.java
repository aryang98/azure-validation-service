package com.example.filevalidation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * File Metadata Entity
 * 
 * This entity represents metadata information for uploaded files
 * stored in Azure SQL Database. It tracks file information,
 * validation status, and processing details.
 * 
 * @author File Validation Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "file_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    /**
     * Primary key for the file metadata
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Original filename of the uploaded file
     */
    @NotBlank(message = "Filename is required")
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /**
     * Azure Blob Storage container name
     */
    @NotBlank(message = "Container name is required")
    @Column(name = "container_name", nullable = false, length = 100)
    private String containerName;

    /**
     * Azure Blob Storage blob name (unique identifier)
     */
    @NotBlank(message = "Blob name is required")
    @Column(name = "blob_name", nullable = false, length = 500)
    private String blobName;

    /**
     * File size in bytes
     */
    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * File content type (MIME type)
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * Validation status of the file
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    private ValidationStatus validationStatus;

    /**
     * Number of rows processed during validation
     */
    @Column(name = "rows_processed")
    private Integer rowsProcessed;

    /**
     * Number of rows with validation errors
     */
    @Column(name = "rows_with_errors")
    private Integer rowsWithErrors;

    /**
     * Error message if validation failed
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * URL to download the processed file (if validation successful)
     */
    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    /**
     * URL to download the error file (if validation had errors)
     */
    @Column(name = "error_file_url", length = 500)
    private String errorFileUrl;

    /**
     * Timestamp when the file was uploaded
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the file metadata was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Validation Status Enumeration
     */
    public enum ValidationStatus {
        PENDING,
        PROCESSING,
        VALIDATED,
        VALIDATED_WITH_ERRORS,
        FAILED
    }
} 