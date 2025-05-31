package de.tum.devops.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * AI-HR Authentication Service
 * 
 * This service handles:
 * - User authentication (login/logout)
 * - User registration (candidates)
 * - HR user creation
 * - JWT token generation
 * - User profile management
 * 
 * @author AI-HR Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "de.tum.devops.auth",
        "de.tum.devops.persistence",
})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
