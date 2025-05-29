package de.tum.devops.assess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI-HR Assessment Service
 * 
 * This service handles:
 * - Assessment creation and management
 * - Scoring and evaluation
 * - AI-powered assessments
 * - Resume analysis
 * - Interview scoring
 * 
 * @author AI-HR Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "de.tum.devops.assess",
        "de.tum.devops.persistence",
})
public class AssessApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssessApplication.class, args);
    }
}