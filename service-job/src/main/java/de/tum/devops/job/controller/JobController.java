package de.tum.devops.job.controller;

import de.tum.devops.job.dto.*;
import de.tum.devops.persistence.entity.JobStatus;
import de.tum.devops.job.service.JobService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for job management endpoints
 */
@RestController
@RequestMapping("/api/v1/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * GET /api/v1/jobs - List jobs with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status,
            Authentication authentication) {

        try {
            String userRole = extractRoleFromJwt(authentication);
            Map<String, Object> result = jobService.getJobs(page, size, status, userRole);

            return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", result));
        } catch (Exception e) {
            logger.error("Error retrieving jobs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve jobs"));
        }
    }

    /**
     * POST /api/v1/jobs - Create new job (HR only)
     */
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            Authentication authentication) {

        try {
            UUID hrCreatorId = extractUserIdFromJwt(authentication);
            JobDto createdJob = jobService.createJob(request, hrCreatorId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(createdJob));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid job creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to create job"));
        }
    }

    /**
     * GET /api/v1/jobs/{jobId} - Get job details
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(
            @PathVariable UUID jobId,
            Authentication authentication) {

        try {
            String userRole = extractRoleFromJwt(authentication);
            JobDto job = jobService.getJobById(jobId, userRole);

            return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", job));
        } catch (IllegalArgumentException e) {
            logger.warn("Job not found or not accessible: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve job"));
        }
    }

    /**
     * PUT /api/v1/jobs/{jobId} - Update job (HR only)
     */
    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> updateJob(
            @PathVariable UUID jobId,
            @Valid @RequestBody UpdateJobRequest request,
            Authentication authentication) {

        try {
            UUID hrUserId = extractUserIdFromJwt(authentication);
            JobDto updatedJob = jobService.updateJob(jobId, request, hrUserId);

            return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updatedJob));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid job update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to update job"));
        }
    }

    /**
     * POST /api/v1/jobs/{jobId}/close - Close job (HR only)
     */
    @PostMapping("/{jobId}/close")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> closeJob(
            @PathVariable UUID jobId,
            Authentication authentication) {

        try {
            UUID hrUserId = extractUserIdFromJwt(authentication);
            JobDto closedJob = jobService.closeJob(jobId, hrUserId);

            return ResponseEntity.ok(ApiResponse.success("Job closed successfully", closedJob));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid job close request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error closing job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to close job"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Job service is running", "OK"));
    }

    /**
     * Extract user ID from JWT token
     */
    private UUID extractUserIdFromJwt(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userIdStr = jwt.getClaimAsString("sub");
            return UUID.fromString(userIdStr);
        }
        throw new IllegalArgumentException("Invalid authentication token");
    }

    /**
     * Extract user role from JWT token
     */
    private String extractRoleFromJwt(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("role");
        }
        throw new IllegalArgumentException("Invalid authentication token");
    }
}