package de.tum.devops.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI-HR Application Management Service
 * <p>
 * This service handles:
 * - Job application submission and management
 * - Application status tracking
 * - AI chat interviews
 * - Document upload and processing
 *
 * @author AI-HR Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class ApplicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationApplication.class, args);
    }
}