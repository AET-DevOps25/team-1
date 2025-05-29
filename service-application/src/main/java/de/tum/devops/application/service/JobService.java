package de.tum.devops.application.service;

import de.tum.devops.application.dto.JobDto;
import de.tum.devops.application.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for retrieving job information from job service
 */
@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final WebClient webClient;

    public JobService(WebClient.Builder webClientBuilder) {
        // TODO: Configure actual service-job URL from configuration
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8081")
                .build();
    }

    /**
     * Get job information by ID
     * For now, return a mock job to avoid inter-service dependency
     */
    public JobDto getJobById(UUID jobId) {
        logger.info("Getting job information for ID: {}", jobId);

        // TODO: Implement actual inter-service call to service-job
        // For now, return a mock job to make the service functional
        UserDto mockHrCreator = new UserDto(
                UUID.randomUUID(),
                "HR Manager",
                "hr@example.com",
                "HR",
                LocalDateTime.now().minusDays(30));

        return new JobDto(
                jobId,
                "Software Engineer",
                "We are looking for a talented software engineer...",
                "Bachelor's degree in Computer Science, 3+ years experience...",
                "OPEN",
                LocalDateTime.now().minusDays(5),
                LocalDate.now().plusDays(30),
                LocalDateTime.now().minusDays(5),
                mockHrCreator);

        /*
         * Future implementation:
         * try {
         * ApiResponse<JobDto> response = webClient
         * .get()
         * .uri("/api/v1/jobs/{jobId}", jobId)
         * .retrieve()
         * .bodyToMono(new ParameterizedTypeReference<ApiResponse<JobDto>>() {})
         * .block();
         * 
         * if (response != null && response.isSuccess()) {
         * return response.getData();
         * } else {
         * throw new IllegalArgumentException("Job not found");
         * }
         * } catch (Exception e) {
         * logger.error("Failed to get job information: {}", e.getMessage());
         * throw new IllegalArgumentException("Failed to retrieve job information");
         * }
         */
    }

    /**
     * Check if job is open for applications
     * For now, return true to make the service functional
     */
    public boolean isJobOpenForApplications(UUID jobId) {
        logger.info("Checking if job {} is open for applications", jobId);

        // TODO: Implement actual inter-service call to service-job
        // For now, return true to make the service functional
        return true;

        /*
         * Future implementation:
         * try {
         * // Call service-job to check if job exists and is OPEN
         * JobDto job = getJobById(jobId);
         * return "OPEN".equals(job.getStatus());
         * } catch (Exception e) {
         * logger.error("Failed to check job status: {}", e.getMessage());
         * return false;
         * }
         */
    }
}