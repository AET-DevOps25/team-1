package de.tum.devops.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI-HR Authentication Service
 * 
 * This service handles:
 * - User authentication (login/logout)
 * - User registration (candidates)
 * - HR user creation
 * - JWT token generation and refresh
 * - User profile management
 * 
 * @author AI-HR Team
 * @version 1.0.0
 */
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}