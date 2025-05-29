package de.tum.devops.application.service;

import de.tum.devops.application.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for retrieving user information from auth service
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final WebClient webClient;

    public UserService(WebClient.Builder webClientBuilder) {
        // TODO: Configure actual service-auth URL from configuration
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080")
                .build();
    }

    /**
     * Get user information by ID
     * For now, return a mock user to avoid inter-service dependency
     */
    public UserDto getUserById(UUID userId) {
        logger.info("Getting user information for ID: {}", userId);

        // TODO: Implement actual inter-service call to service-auth
        // For now, return a mock user to make the service functional
        return new UserDto(
                userId,
                "John Candidate",
                "candidate@example.com",
                "CANDIDATE",
                LocalDateTime.now().minusDays(10));

        /*
         * Future implementation:
         * try {
         * ApiResponse<UserDto> response = webClient
         * .get()
         * .uri("/api/v1/users/{userId}", userId)
         * .retrieve()
         * .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDto>>() {})
         * .block();
         * 
         * if (response != null && response.isSuccess()) {
         * return response.getData();
         * } else {
         * throw new IllegalArgumentException("User not found");
         * }
         * } catch (Exception e) {
         * logger.error("Failed to get user information: {}", e.getMessage());
         * throw new IllegalArgumentException("Failed to retrieve user information");
         * }
         */
    }
}