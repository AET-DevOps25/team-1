package de.tum.devops.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for file storage
 */
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    @Value("${app.file.upload-dir}")
    private String uploadDir;
    private long maxFileSize = 10 * 1024 * 1024; // 10MB
    private String[] allowedExtensions = {".pdf", ".doc", ".docx"};
    private String[] allowedContentTypes = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    // Getters and Setters
    public String getUploadDir() {
        return uploadDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public String[] getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(String[] allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}