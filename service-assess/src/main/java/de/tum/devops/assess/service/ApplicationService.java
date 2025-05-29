package de.tum.devops.assess.service;

import de.tum.devops.assess.dto.ApplicationDto;
import de.tum.devops.assess.dto.JobDto;
import de.tum.devops.assess.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for retrieving application information from application service
 */
@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final WebClient webClient;

    public ApplicationService(WebClient.Builder webClientBuilder) {
        // TODO: Configure actual service-application URL from configuration
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8082")
                .build();
    }

    /**
     * Get application information by ID
     * For now, return a mock application to avoid inter-service dependency
     */
    public ApplicationDto getApplicationById(UUID applicationId) {
        logger.info("Getting application information for ID: {}", applicationId);

        // TODO: Implement actual inter-service call to service-application
        // For now, return a mock application to make the service functional
        UserDto mockCandidate = new UserDto(
                UUID.randomUUID(),
                "John Candidate",
                "candidate@example.com",
                "CANDIDATE",
                LocalDateTime.now().minusDays(10));

        UserDto mockHrCreator = new UserDto(
                UUID.randomUUID(),
                "HR Manager",
                "hr@example.com",
                "HR",
                LocalDateTime.now().minusDays(30));

        JobDto mockJob = new JobDto(
                UUID.randomUUID(),
                "Software Engineer",
                "We are looking for a talented software engineer...",
                "Bachelor's degree in Computer Science, 3+ years experience...",
                "OPEN",
                LocalDateTime.now().minusDays(5),
                null,
                LocalDateTime.now().minusDays(5),
                mockHrCreator);

        return new ApplicationDto(
                applicationId,
                mockCandidate,
                mockJob,
                "PENDING",
                "resume_path.pdf",
                "I am excited to apply for this position...",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2),
                null);

        /*
         * Future implementation:
         * try {
         * ApiResponse<ApplicationDto> response = webClient
         * .get()
         * .uri("/api/v1/applications/{applicationId}", applicationId)
         * .retrieve()
         * .bodyToMono(new ParameterizedTypeReference<ApiResponse<ApplicationDto>>() {})
         * .block();
         * 
         * if (response != null && response.isSuccess()) {
         * return response.getData();
         * } else {
         * throw new IllegalArgumentException("Application not found");
         * }
         * } catch (Exception e) {
         * logger.error("Failed to get application information: {}", e.getMessage());
         * throw new
         * IllegalArgumentException("Failed to retrieve application information");
         * }
         */
    }

    /**
     * Check if application exists
     * For now, return true to make the service functional
     */
    public boolean applicationExists(UUID applicationId) {
        logger.info("Checking if application exists: {}", applicationId);

        // TODO: Implement actual inter-service call to service-application
        // For now, return true to make the service functional
        return true;

        /*
         * Future implementation:
         * try {
         * getApplicationById(applicationId);
         * return true;
         * } catch (Exception e) {
         * logger.warn("Application not found: {}", applicationId);
         * return false;
         * }
         */
    }
}