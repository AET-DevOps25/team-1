package de.tum.devops.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Job Service Application - Main entry point
 * 
 * AI-HR Recruitment System - Job Management Service
 * Handles job postings, job searches, and job-related operations
 */
@SpringBootApplication
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}