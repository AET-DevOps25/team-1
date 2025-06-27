package de.tum.devops.application.controller;

import de.tum.devops.application.dto.*;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.ApplicationStatus;
import de.tum.devops.application.service.AIIntegrationService;
import de.tum.devops.application.service.ApplicationService;
import de.tum.devops.application.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for application management endpoints
 */
@RestController
@RequestMapping("/api/v1/applications")
@CrossOrigin(origins = "*")
@Validated
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;
    private final ChatService chatService;
    private final AIIntegrationService aiIntegrationService;

    public ApplicationController(ApplicationService applicationService, ChatService chatService, AIIntegrationService aiIntegrationService) {
        this.applicationService = applicationService;
        this.chatService = chatService;
        this.aiIntegrationService = aiIntegrationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ApplicationDto>> submitApplication(@Valid @RequestPart("request") SubmitApplicationRequest request,
                                                                         @RequestPart("resumeFile") MultipartFile resumeFile,
                                                                         @AuthenticationPrincipal Jwt jwt) {
        if (resumeFile.isEmpty() || !"application/pdf".equals(resumeFile.getContentType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest("Only PDF file is allowed for resume."));
        }
        logger.info("Candidate {} submitting application for job {}", jwt.getSubject(), request.getJobId());
        ApplicationDto applicationDto = applicationService.submitApplication(request, UUID.fromString(jwt.getSubject()), resumeFile);

        // Score resume
        try {
            aiIntegrationService.scoreResumeAsync(applicationDto.getApplicationId());
        } catch (Exception e) {
            logger.error("Failed to score resume for application {}", applicationDto.getApplicationId(), e);
            // Don't fail the application submission if scoring fails
        }
        logger.info("Application submitted successfully with ID: {}", applicationDto.getApplicationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(applicationDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR') or hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplications(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                                            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                                                            @RequestParam(required = false) UUID jobId,
                                                                            @RequestParam(required = false) ApplicationStatus status,
                                                                            Authentication authentication,
                                                                            @AuthenticationPrincipal Jwt jwt) {
        Optional<? extends GrantedAuthority> userRoleOptional = authentication.getAuthorities().stream().findFirst();
        String userRole = userRoleOptional.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")).orElse(null);
        Map<String, Object> result = applicationService.getApplications(page, size, jobId, status, userRole, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", result));
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('HR') or hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationById(@PathVariable UUID applicationId,
                                                                          Authentication authentication,
                                                                          @AuthenticationPrincipal Jwt jwt) {
        Optional<? extends GrantedAuthority> userRoleOptional = authentication.getAuthorities().stream().findFirst();
        String userRole = userRoleOptional.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")).orElse(null);
        ApplicationDto applicationDto = applicationService.getApplicationById(applicationId, userRole, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("Application retrieved", applicationDto));
    }

    @PatchMapping("/{applicationId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<ApplicationDto>> updateApplication(@PathVariable UUID applicationId,
                                                                         @Valid @RequestBody UpdateApplicationRequest request,
                                                                         @AuthenticationPrincipal Jwt jwt) {
        logger.info("HR {} updating application {} with decision: {}", jwt.getSubject(), applicationId, request.getHrDecision());
        ApplicationDto applicationDto = applicationService.updateApplication(applicationId, request.getHrDecision(), request.getHrComments(), UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("Application updated", applicationDto));
    }

    @GetMapping("/{applicationId}/messages")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<Object>> getMessagesForApplication(@PathVariable UUID applicationId,
                                                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                         @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
        Object messages = chatService.getMessagesByApplication(applicationId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved", messages));
    }

    @PostMapping("/{applicationId}/chat")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ChatInitializationDto>> createOrGetChatSession(@PathVariable UUID applicationId,
                                                                               @AuthenticationPrincipal Jwt jwt) {
        ChatInitializationDto chatInitializationDto = chatService.initiateChatSession(applicationId, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("Session retrieved", chatInitializationDto));
    }

    @PostMapping("/{applicationId}/chat/complete")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Object>> completeChatSession(@PathVariable UUID applicationId,
                                                                   @AuthenticationPrincipal Jwt jwt) {
        logger.info("Candidate {} attempting to complete chat for application {}", jwt.getSubject(), applicationId);
        // Get session (also performs security check)
        ChatSession session = chatService.createOrGetSession(applicationId, UUID.fromString(jwt.getSubject()));
        // Mark interview as complete
        chatService.completeInterview(session);
        logger.info("Chat session for application {} completed successfully", applicationId);
        return ResponseEntity.ok(ApiResponse.success("Chat session completed successfully.", null));
    }
}