package de.tum.devops.job.controller;

import de.tum.devops.job.dto.ApiResponse;
import de.tum.devops.job.dto.CreateJobRequest;
import de.tum.devops.job.dto.JobDto;
import de.tum.devops.job.dto.PagedResponseDto;
import de.tum.devops.job.dto.UpdateJobRequest;
import de.tum.devops.job.persistence.enums.JobStatus;
import de.tum.devops.job.service.JobService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Job controller
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
     * GET /jobs - List jobs with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<JobDto>>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status,
            Authentication authentication) {

        try {
            String userRole = extractRole(authentication);
            Page<JobDto> resultPage = jobService.getJobs(page, size, status, userRole);
            PagedResponseDto<JobDto> pagedResponse = new PagedResponseDto<>(resultPage);

            return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", pagedResponse));
        } catch (Exception e) {
            logger.error("Error retrieving jobs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve jobs"));
        }
    }

    /**
     * POST /jobs - Create new job (HR only)
     */
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            UUID hrCreatorId = UUID.fromString(jwt.getSubject());
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
     * GET /jobs/{jobId} - Get job details
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(
            @PathVariable UUID jobId,
            Authentication authentication) {
        try {
            String userRole = extractRole(authentication);
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
     * PATCH /jobs/{jobId} - Update job (HR only)
     */
    @PatchMapping("/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> updateJob(
            @PathVariable UUID jobId,
            @Valid @RequestBody UpdateJobRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            UUID hrUserId = UUID.fromString(jwt.getSubject());
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
     * POST /jobs/{jobId}/close - Close job (HR only)
     */
    @PostMapping("/{jobId}/close")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> closeJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            UUID hrUserId = UUID.fromString(jwt.getSubject());
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
     * POST /jobs/{jobId}/open - Re-open job (HR only)
     */
    @PostMapping("/{jobId}/open")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<JobDto>> openJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            UUID hrUserId = UUID.fromString(jwt.getSubject());
            JobDto openedJob = jobService.reopenJob(jobId, hrUserId);

            return ResponseEntity.ok(ApiResponse.success("Job reopened successfully", openedJob));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid job reopen request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error reopening job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to reopen job"));
        }
    }

    /**
     * DELETE /jobs/{jobId} - Delete job (HR only)
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            UUID hrUserId = UUID.fromString(jwt.getSubject());
            jobService.deleteJob(jobId, hrUserId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid job delete request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error deleting job {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // helper to get role if needed
    private String extractRole(Authentication authentication) {
        if (authentication == null) return null;
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }
}