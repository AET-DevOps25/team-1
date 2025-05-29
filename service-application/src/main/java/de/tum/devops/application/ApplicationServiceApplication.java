package de.tum.devops.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application Service Application - Main entry point
 * 
 * AI-HR Recruitment System - Application Management Service
 * Handles job applications, candidate tracking, and application status
 * management
 */
@SpringBootApplication
public class ApplicationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationServiceApplication.class, args);
    }
}