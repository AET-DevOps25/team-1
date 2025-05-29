package de.tum.devops.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI-HR Application Management Service
 *
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
@ComponentScan(basePackages = {
        "de.tum.devops.application",
        "de.tum.devops.persistence",
})
public class ApplicationService {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationService.class, args);
    }
}