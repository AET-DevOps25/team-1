package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.dto.ApplicationDto;
import de.tum.devops.application.dto.ChatSessionDto;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.service.ApplicationService;
import de.tum.devops.application.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal application info endpoint (cluster-internal, no auth).
 */
@RestController
@RequestMapping("/internal/api/v1/applications")
public class InternalApplicationController {

    private final ApplicationService applicationService;
    private final ChatService chatService;

    public InternalApplicationController(ApplicationService applicationService, ChatService chatService) {
        this.applicationService = applicationService;
        this.chatService = chatService;
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationWithAssessment(@PathVariable UUID applicationId) {
        // For internal calls, we use "INTERNAL" as role and null as userId to bypass security checks
        ApplicationDto applicationDto = applicationService.getApplicationById(applicationId, "INTERNAL", null);
        return ResponseEntity.ok(ApiResponse.success("Application retrieved", applicationDto));
    }

    @PostMapping("/{applicationId}/chat")
    public ResponseEntity<ApiResponse<ChatSessionDto>> createOrGetChatSession(@PathVariable UUID applicationId,
                                                                              @RequestParam UUID candidateId) {
        ChatSession session = chatService.createOrGetSession(applicationId, candidateId);
        ChatSessionDto sessionDto = new ChatSessionDto(
                session.getSessionId(),
                session.getApplication().getApplicationId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getMessageCount()
        );
        return ResponseEntity.ok(ApiResponse.success("Session retrieved", sessionDto));
    }
}