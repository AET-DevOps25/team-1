package de.tum.devops.assess.controller;

import de.tum.devops.assess.dto.ApiResponse;
import de.tum.devops.assess.dto.AssessmentDto;
import de.tum.devops.assess.service.AssessmentService;
import de.tum.devops.persistence.enums.RecommendationEnum;
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
 * REST controller for assessment management endpoints (merged into
 * service-application)
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

    @PostMapping("/{applicationId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AssessmentDto>> createAssessment(
            @PathVariable UUID applicationId,
            Authentication authentication) {
        try {
            AssessmentDto created = assessmentService.createAssessmentForApplication(applicationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to create assessment"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RecommendationEnum status,
            Authentication authentication) {
        try {
            String role = extractRole(authentication);
            UUID userId = extractUserId(authentication);
            Map<String, Object> result = assessmentService.getAssessments(page, size, status, role, userId);
            return ResponseEntity.ok(ApiResponse.success("Assessments retrieved", result));
        } catch (Exception e) {
            logger.error("retrieve assessments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve assessments"));
        }
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<AssessmentDto>> getAssessmentById(
            @PathVariable UUID assessmentId,
            Authentication authentication) {
        try {
            String role = extractRole(authentication);
            UUID userId = extractUserId(authentication);
            AssessmentDto dto = assessmentService.getAssessmentById(assessmentId, role, userId);
            return ResponseEntity.ok(ApiResponse.success("Assessment retrieved", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(e.getMessage()));
        } catch (Exception e) {
            logger.error("get assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to retrieve assessment"));
        }
    }

    @PostMapping("/{assessmentId}/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<AssessmentDto>> startAssessment(
            @PathVariable UUID assessmentId,
            Authentication authentication) {
        try {
            UUID candidateId = extractUserId(authentication);
            AssessmentDto dto = assessmentService.startAssessment(assessmentId, candidateId);
            return ResponseEntity.ok(ApiResponse.success("Assessment started", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("start assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to start assessment"));
        }
    }

    @PostMapping("/{assessmentId}/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<AssessmentDto>> submitAssessment(
            @PathVariable UUID assessmentId,
            @RequestBody String assessmentData,
            Authentication authentication) {
        try {
            UUID candidateId = extractUserId(authentication);
            AssessmentDto dto = assessmentService.submitAssessment(assessmentId, assessmentData, candidateId);
            return ResponseEntity.ok(ApiResponse.success("Assessment submitted", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("submit assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to submit assessment"));
        }
    }

    @PostMapping("/{assessmentId}/score")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AssessmentDto>> scoreAssessment(
            @PathVariable UUID assessmentId,
            @RequestParam Integer score,
            @RequestParam(required = false) String feedback,
            Authentication authentication) {
        try {
            UUID hrUserId = extractUserId(authentication);
            AssessmentDto dto = assessmentService.scoreAssessment(assessmentId, score, feedback, hrUserId);
            return ResponseEntity.ok(ApiResponse.success("Assessment scored", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("score assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to score assessment"));
        }
    }

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
            AssessmentDto dto = assessmentService.updateAssessmentScore(assessmentId, resumeScore, interviewScore,
                    finalScore,
                    resumeAnalysis, interviewSummary, recommendation);
            return ResponseEntity.ok(ApiResponse.success("Assessment updated", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("update assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("Failed to update assessment"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Assessment service running", "OK"));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return UUID.fromString(jwt.getClaimAsString("sub"));
        }
        throw new IllegalArgumentException("Invalid authentication token");
    }

    private String extractRole(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("role");
        }
        throw new IllegalArgumentException("Invalid authentication token");
    }
}