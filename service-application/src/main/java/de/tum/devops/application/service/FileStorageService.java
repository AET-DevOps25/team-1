package de.tum.devops.application.service;

import de.tum.devops.application.config.FileStorageProperties;
import de.tum.devops.application.exception.FileNotFoundException;
import de.tum.devops.application.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for handling file storage operations
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final FileStorageProperties fileStorageProperties;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    /**
     * Store uploaded file
     */
    public String store(MultipartFile file, String filePrefix) {
        if (file == null || file.isEmpty()) {
            return null; // Allow null files for optional file uploads
        }

        try {
            // Validate file
            validateFile(file);

            // Create upload directory if not exists
            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                originalFilename = ".pdf";
            }
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = filePrefix + "_" + System.currentTimeMillis() + extension;

            Path filePath = uploadPath.resolve(uniqueFilename);

            // Store file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully: {}", uniqueFilename);
            return uniqueFilename;

        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage());
            throw new FileStorageException("Failed to store file", e);
        }
    }

    /**
     * Store uploaded file (legacy method for backward compatibility)
     */
    public String storeFile(MultipartFile file, UUID candidateId) {
        return store(file, candidateId.toString());
    }

    /**
     * Store uploaded file with custom prefix
     */
    public String storeFile(MultipartFile file, String filePrefix) {
        return store(file, filePrefix);
    }

    /**
     * Delete file
     */
    public void deleteFile(String filename) {
        try {
            Path filePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(filename);
            Files.deleteIfExists(filePath);
            logger.info("File deleted successfully: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", e.getMessage());
        }
    }

    /**
     * Get file path for download
     */
    public Path getFilePath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        Path filePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(filename);

        // Security check: ensure the file is within the upload directory
        if (!filePath.normalize().startsWith(Paths.get(fileStorageProperties.getUploadDir()).normalize())) {
            throw new SecurityException("Access to file outside upload directory is not allowed");
        }

        return filePath;
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        try {
            Path filePath = getFilePath(filename);
            return Files.exists(filePath) && Files.isRegularFile(filePath);
        } catch (Exception e) {
            logger.warn("Error checking file existence for {}: {}", filename, e.getMessage());
            return false;
        }
    }

    /**
     * Get file size in bytes
     */
    public long getFileSize(String filename) {
        try {
            Path filePath = getFilePath(filename);
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException("File not found: " + filename);
            }
            return Files.size(filePath);
        } catch (IOException e) {
            logger.error("Failed to get file size for {}: {}", filename, e.getMessage());
            throw new FileStorageException("Failed to get file size", e);
        }
    }

    /**
     * Get file content type based on extension
     */
    public String getContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }

        String extension = getFileExtension(filename).toLowerCase();
        return switch (extension) {
            case ".pdf" -> "application/pdf";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    /**
     * Initialize upload directory on startup
     */
    public void initializeUploadDirectory() {
        try {
            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            } else {
                logger.info("Upload directory already exists: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to initialize upload directory: {}", e.getMessage());
            throw new FileStorageException("Failed to initialize upload directory", e);
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " +
                    (fileStorageProperties.getMaxFileSize() / (1024 * 1024)) + "MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is missing");
        }

        // Security check: prevent path traversal attacks
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // Check file extension
        String extension = getFileExtension(filename).toLowerCase();
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("File must have an extension");
        }

        boolean isAllowed = false;
        for (String allowedExt : fileStorageProperties.getAllowedExtensions()) {
            if (extension.equals(allowedExt)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("File type not supported. Only " +
                    String.join(", ", fileStorageProperties.getAllowedExtensions()) + " files are allowed");
        }

        // Additional MIME type validation
        String contentType = file.getContentType();
        if (contentType != null && !isValidContentType(contentType)) {
            throw new IllegalArgumentException("Invalid file content type");
        }
    }

    /**
     * Validate content type
     */
    private boolean isValidContentType(String contentType) {
        for (String allowedType : fileStorageProperties.getAllowedContentTypes()) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex != -1) ? filename.substring(lastDotIndex) : "";
    }
}