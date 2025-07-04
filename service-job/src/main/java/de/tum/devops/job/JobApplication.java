package de.tum.devops.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI-HR Job Management Service
 * 
 * This service handles:
 * - Job posting creation and management
 * - Job listing and search
 * - Job status management
 * - HR job operations
 * 
 * @author AI-HR Team
 * @version 1.0.0
 */
@SpringBootApplication
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}