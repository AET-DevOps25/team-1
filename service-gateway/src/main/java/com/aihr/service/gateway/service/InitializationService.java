package com.aihr.service.gateway.service;

import com.aihr.service.gateway.entity.User;
import com.aihr.service.gateway.entity.UserRole;
import com.aihr.service.gateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service to initialize the system with default data
 */
@Service
public class InitializationService implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.hr.email:admin@aihr.com}")
    private String initialHREmail;

    @Value("${app.init.hr.password:Admin@123!}")
    private String initialHRPassword;

    @Value("${app.init.hr.fullname:System Administrator}")
    private String initialHRFullName;

    @Autowired
    public InitializationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeHRUser();
    }

    /**
     * Initialize the first HR user if no HR users exist
     */
    private void initializeHRUser() {
        try {
            // Check if any HR users exist
            if (!userRepository.existsAnyHRUser()) {
                System.out.println("No HR users found. Creating initial HR user...");

                // Create initial HR user
                User initialHR = new User(
                        initialHRFullName,
                        initialHREmail,
                        passwordEncoder.encode(initialHRPassword),
                        UserRole.HR);

                userRepository.save(initialHR);

                System.out.println("Initial HR user created successfully:");
                System.out.println("Email: " + initialHREmail);
                System.out.println("Password: " + initialHRPassword);
                System.out.println("Full Name: " + initialHRFullName);
                System.out.println("Please change the password after first login!");
            } else {
                System.out.println("HR users already exist. Skipping initialization.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize HR user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get system statistics
     */
    public SystemStats getSystemStats() {
        long hrCount = userRepository.countByRole(UserRole.HR);
        long candidateCount = userRepository.countByRole(UserRole.CANDIDATE);
        long totalUsers = hrCount + candidateCount;

        return new SystemStats(totalUsers, hrCount, candidateCount);
    }

    /**
     * System statistics data class
     */
    public static class SystemStats {
        private final long totalUsers;
        private final long hrUsers;
        private final long candidates;

        public SystemStats(long totalUsers, long hrUsers, long candidates) {
            this.totalUsers = totalUsers;
            this.hrUsers = hrUsers;
            this.candidates = candidates;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public long getHrUsers() {
            return hrUsers;
        }

        public long getCandidates() {
            return candidates;
        }

        @Override
        public String toString() {
            return "SystemStats{" +
                    "totalUsers=" + totalUsers +
                    ", hrUsers=" + hrUsers +
                    ", candidates=" + candidates +
                    '}';
        }
    }
}