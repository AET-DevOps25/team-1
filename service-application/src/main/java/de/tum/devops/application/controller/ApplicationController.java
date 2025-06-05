package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.dto.ApplicationDto;
import de.tum.devops.application.dto.SubmitApplicationRequest;
import de.tum.devops.application.service.ApplicationService;
import de.tum.devops.persistence.enums.ApplicationStatus;
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
 * REST controller for application management endpoints
 */
@RestController
@RequestMapping("/api/v1/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * POST /api/v1/applications - Submit new application (Candidates only)
     */
    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ApplicationDto>> submitApplication(
            @Valid @RequestBody SubmitApplicationRequest request,
            Authentication authentication) {

        try {
            UUID candidateId = extractUserIdFromJwt(authentication);
            ApplicationDto submittedApplication = applicationService.submitApplication(request, candidateId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(submittedApplication));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid application submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error submitting application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to submit application"));
        }
    }

    /**
     * GET /api/v1/applications - Get applications (role-based)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ApplicationStatus status,
            Authentication authentication) {

        try {
            String userRole = extractRoleFromJwt(authentication);
            UUID userId = extractUserIdFromJwt(authentication);

            Map<String, Object> result;
            if ("CANDIDATE".equals(userRole)) {
                // Candidates see only their own applications
                result = applicationService.getCandidateApplications(userId, page, size);
            } else {
                // HR sees all applications for review
                result = applicationService.getApplicationsForReview(status, page, size);
            }

            return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", result));
        } catch (Exception e) {
            logger.error("Error retrieving applications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve applications"));
        }
    }

    /**
     * GET /api/v1/applications/{applicationId} - Get application details
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationById(
            @PathVariable UUID applicationId,
            Authentication authentication) {

        try {
            UUID userId = extractUserIdFromJwt(authentication);
            String userRole = extractRoleFromJwt(authentication);

            ApplicationDto application = applicationService.getApplicationById(applicationId, userId, userRole);

            return ResponseEntity.ok(ApiResponse.success("Application retrieved successfully", application));
        } catch (IllegalArgumentException e) {
            logger.warn("Application not found or not accessible: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve application"));
        }
    }

    /**
     * PUT /api/v1/applications/{applicationId}/status - Update application status
     * (HR only)
     */
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<ApplicationDto>> updateApplicationStatus(
            @PathVariable UUID applicationId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String feedback,
            Authentication authentication) {

        try {
            UUID hrUserId = extractUserIdFromJwt(authentication);
            ApplicationDto updatedApplication = applicationService.updateApplicationStatus(
                    applicationId, status, feedback, hrUserId);

            return ResponseEntity
                    .ok(ApiResponse.success("Application status updated successfully", updatedApplication));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid application status update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating application status {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to update application status"));
        }
    }

    /**
     * DELETE /api/v1/applications/{applicationId} - Withdraw application
     * (Candidates only)
     */
    @DeleteMapping("/{applicationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> withdrawApplication(
            @PathVariable UUID applicationId,
            Authentication authentication) {

        try {
            UUID candidateId = extractUserIdFromJwt(authentication);
            applicationService.withdrawApplication(applicationId, candidateId);

            return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", "OK"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid application withdrawal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error withdrawing application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to withdraw application"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Application service is running", "OK"));
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