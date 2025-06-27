package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.dto.ChatMessageDto;
import de.tum.devops.application.dto.SendChatMessageRequest;
import de.tum.devops.application.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;
import java.util.UUID;

/**
 * Application - chat controller
 */
@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
@Validated
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/{sessionId}/messages", produces = "text/event-stream")
    @PreAuthorize("hasRole('CANDIDATE')")
    public SseEmitter sendMessage(@PathVariable UUID sessionId,
                                  @Valid @RequestBody SendChatMessageRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Candidate {} sending message to session {} (stream)", userId, sessionId);

        SseEmitter emitter = new SseEmitter(30_000L); // 30-second timeout

        emitter.onCompletion(() -> logger.info("SSE stream completed for session {}", sessionId));
        emitter.onTimeout(() -> logger.warn("SSE stream timed out for session {}", sessionId));
        emitter.onError(e -> logger.error("SSE stream error for session {}", sessionId, e));

        chatService.addCandidateMessageAndGetAiResponseStream(sessionId, request.getContent(), UUID.fromString(userId), emitter);

        return emitter;
    }

    @GetMapping("/{sessionId}/messages")
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('HR')")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> getMessages(@PathVariable UUID sessionId,
                                                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                         @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size,
                                                                         Authentication authentication,
                                                                         @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Optional<? extends GrantedAuthority> userRoleOptional = authentication.getAuthorities().stream().findFirst();
        String userRole = userRoleOptional.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")).orElse(null);
        logger.info("Candidate {} getting messages for session {}", userId, sessionId);
        if (userRole == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.forbidden("Access denied"));
        }
        Page<ChatMessageDto> messages = chatService.getMessagesBySession(sessionId, UUID.fromString(userId), PageRequest.of(page, size), userRole);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved", messages));
    }
}