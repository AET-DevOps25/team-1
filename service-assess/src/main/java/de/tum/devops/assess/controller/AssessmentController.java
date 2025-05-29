package de.tum.devops.assess.controller;

import de.tum.devops.assess.dto.*;
import de.tum.devops.persistence.entity.RecommendationEnum;
import de.tum.devops.assess.service.AssessmentService;
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
 * REST controller for assessment management endpoints
 */
@RestController
@RequestMapping("/api/v1/assessments")
@CrossOrigin(origins = "*")
public class AssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * POST /api/v1/assessments/{applicationId} - Create assessment for application
     * (HR only)
     */
    @PostMapping("/{applicationId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AssessmentDto>> createAssessment(
            @PathVariable UUID applicationId,
            Authentication authentication) {

        try {
            AssessmentDto createdAssessment = assessmentService.createAssessmentForApplication(applicationId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(createdAssessment));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid assessment creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating assessment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to create assessment"));
        }
    }

    /**
     * GET /api/v1/assessments - List assessments with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RecommendationEnum status,
            Authentication authentication) {

        try {
            String userRole = extractRoleFromJwt(authentication);
            UUID userId = extractUserIdFromJwt(authentication);
            Map<String, Object> result = assessmentService.getAssessments(page, size, status, userRole, userId);

            return ResponseEntity.ok(ApiResponse.success("Assessments retrieved successfully", result));
        } catch (Exception e) {
            logger.error("Error retrieving assessments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve assessments"));
        }
    }

    /**
     * GET /api/v1/assessments/{assessmentId} - Get assessment details
     */
    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<AssessmentDto>> getAssessmentById(
            @PathVariable UUID assessmentId,
            Authentication authentication) {

        try {
            String userRole = extractRoleFromJwt(authentication);
            UUID userId = extractUserIdFromJwt(authentication);
            AssessmentDto assessment = assessmentService.getAssessmentById(assessmentId, userRole, userId);

            return ResponseEntity.ok(ApiResponse.success("Assessment retrieved successfully", assessment));
        } catch (IllegalArgumentException e) {
            logger.warn("Assessment not found or not accessible: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving assessment {}: {}", assessmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve assessment"));
        }
    }

    /**
     * POST /api/v1/assessments/{assessmentId}/start - Start assessment (Candidates
     * only)
     */
    @PostMapping("/{assessmentId}/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<AssessmentDto>> startAssessment(
            @PathVariable UUID assessmentId,
            Authentication authentication) {

        try {
            UUID candidateId = extractUserIdFromJwt(authentication);
            AssessmentDto startedAssessment = assessmentService.startAssessment(assessmentId, candidateId);

            return ResponseEntity.ok(ApiResponse.success("Assessment started successfully", startedAssessment));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid assessment start request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error starting assessment {}: {}", assessmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to start assessment"));
        }
    }

    /**
     * POST /api/v1/assessments/{assessmentId}/submit - Submit assessment
     * (Candidates only)
     */
    @PostMapping("/{assessmentId}/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<AssessmentDto>> submitAssessment(
            @PathVariable UUID assessmentId,
            @RequestBody String assessmentData,
            Authentication authentication) {

        try {
            UUID candidateId = extractUserIdFromJwt(authentication);
            AssessmentDto submittedAssessment = assessmentService.submitAssessment(assessmentId, assessmentData,
                    candidateId);

            return ResponseEntity.ok(ApiResponse.success("Assessment submitted successfully", submittedAssessment));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid assessment submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error submitting assessment {}: {}", assessmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to submit assessment"));
        }
    }

    /**
     * POST /api/v1/assessments/{assessmentId}/score - Score assessment (HR only)
     */
    @PostMapping("/{assessmentId}/score")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AssessmentDto>> scoreAssessment(
            @PathVariable UUID assessmentId,
            @RequestParam Integer score,
            @RequestParam(required = false) String feedback,
            Authentication authentication) {

        try {
            UUID hrUserId = extractUserIdFromJwt(authentication);
            AssessmentDto scoredAssessment = assessmentService.scoreAssessment(assessmentId, score, feedback, hrUserId);

            return ResponseEntity.ok(ApiResponse.success("Assessment scored successfully", scoredAssessment));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid assessment scoring request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error scoring assessment {}: {}", assessmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to score assessment"));
        }
    }

    /**
     * PUT /api/v1/assessments/{assessmentId}/score - Update assessment scores
     * (HR only)
     */
    @PutMapping("/{assessmentId}/score")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AssessmentDto>> updateAssessmentScore(
            @PathVariable UUID assessmentId,
            @RequestParam(required = false) Float resumeScore,
            @RequestParam(required = false) Float interviewScore,
            @RequestParam(required = false) Float finalScore,
            @RequestParam(required = false) String resumeAnalysis,
            @RequestParam(required = false) String interviewSummary,
            @RequestParam(required = false) RecommendationEnum recommendation,
            Authentication authentication) {

        try {
            AssessmentDto updatedAssessment = assessmentService.updateAssessmentScore(
                    assessmentId, resumeScore, interviewScore, finalScore,
                    resumeAnalysis, interviewSummary, recommendation);

            return ResponseEntity.ok(ApiResponse.success("Assessment updated successfully", updatedAssessment));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid assessment update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating assessment {}: {}", assessmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to update assessment"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Assessment service is running", "OK"));
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
