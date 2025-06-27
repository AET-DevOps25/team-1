package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.dto.ChatMessageDto;
import de.tum.devops.application.dto.SendChatMessageRequest;
import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

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

    @PostMapping("/{sessionId}/messages")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMessage(@PathVariable UUID sessionId,
                                                                   @Valid @RequestBody SendChatMessageRequest request,
                                                                   @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Candidate {} sending message to session {}", userId, sessionId);
        ChatMessage aiMessage = chatService.addCandidateMessageAndGetAiResponse(sessionId, request.getContent(), UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Message sent", new ChatMessageDto(aiMessage)));
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
        Page<ChatMessageDto> messages = chatService.getMessagesBySession(sessionId, UUID.fromString(userId), PageRequest.of(page, size), userRole);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved", messages));
    }
}