package de.tum.devops.assess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Assessment Service Application - Main entry point
 * 
 * AI-HR Recruitment System - Assessment Service
 * Handles skill assessments, AI evaluations, and candidate scoring
 */
@SpringBootApplication
public class AssessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssessServiceApplication.class, args);
    }
}