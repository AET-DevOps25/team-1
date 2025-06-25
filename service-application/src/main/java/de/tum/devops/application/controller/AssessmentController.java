package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.persistence.entity.Assessment;
import de.tum.devops.application.service.AIIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for assessment operations
 */
@RestController
@RequestMapping("/api/v1/assessments")
@CrossOrigin(origins = "*")
public class AssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    private final AIIntegrationService aiIntegrationService;

    public AssessmentController(AIIntegrationService aiIntegrationService) {
        this.aiIntegrationService = aiIntegrationService;
    }

    /**
     * Trigger resume scoring for an application
     */
    @PostMapping("/applications/{applicationId}/score-resume")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<Assessment>> scoreResume(@PathVariable UUID applicationId,
                                                               @AuthenticationPrincipal String userId) {
        logger.info("HR {} triggered resume scoring for application {}", userId, applicationId);
        Assessment assessment = aiIntegrationService.scoreResumeSync(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Resume scored successfully", assessment));
    }

    /**
     * Trigger interview scoring for an application
     */
    @PostMapping("/applications/{applicationId}/score-interview")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<Assessment>> scoreInterview(@PathVariable UUID applicationId,
                                                                  @AuthenticationPrincipal String userId) {
        logger.info("HR {} triggered interview scoring for application {}", userId, applicationId);
        Assessment assessment = aiIntegrationService.scoreInterviewSync(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Interview scored successfully", assessment));
    }
}