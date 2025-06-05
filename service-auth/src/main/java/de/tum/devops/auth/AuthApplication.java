package de.tum.devops.auth;

import de.tum.devops.persistence.entity.User;
import de.tum.devops.persistence.enums.UserRole;
import de.tum.devops.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    // create initial HR user if not exists
    @Bean
    public CommandLineRunner ensureAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        Logger log = LoggerFactory.getLogger(AuthApplication.class);
        return args -> {
            // check if there is already an HR user
            if (userRepository.countByRole(UserRole.HR) == 0) {
                User admin = new User();
                admin.setFullName("admin");
                admin.setEmail("admin@aihr.com");
                admin.setPasswordHash(passwordEncoder.encode("admin"));
                admin.setRole(UserRole.HR);
                userRepository.save(admin);
                log.info("Created initial HR user: {}", admin.getEmail());
            }
            else {
                log.info("HR user already exists, skipping creation.");
            }
        };
    }
}
