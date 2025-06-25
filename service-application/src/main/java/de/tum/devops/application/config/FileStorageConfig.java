package de.tum.devops.application.config;

import de.tum.devops.application.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for file storage initialization
 */
@Configuration
public class FileStorageConfig {

    /**
     * Initialize file storage on application startup
     */
    @Bean
    public CommandLineRunner initFileStorage(FileStorageService fileStorageService) {
        return args -> {
            fileStorageService.initializeUploadDirectory();
        };
    }
}