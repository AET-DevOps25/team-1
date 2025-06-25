package de.tum.devops.application.service;

import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.ApplicationStatus;
import de.tum.devops.application.persistence.enums.ChatStatus;
import de.tum.devops.application.persistence.enums.MessageSender;
import de.tum.devops.application.persistence.repository.ApplicationRepository;
import de.tum.devops.application.persistence.repository.ChatMessageRepository;
import de.tum.devops.application.persistence.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ApplicationRepository applicationRepository;
    private final AIIntegrationService aiIntegrationService;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       ApplicationRepository applicationRepository,
                       AIIntegrationService aiIntegrationService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.applicationRepository = applicationRepository;
        this.aiIntegrationService = aiIntegrationService;
    }

    public ChatSession createOrGetSession(UUID applicationId, UUID candidateId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Check if the candidate owns this application
        if (!application.getCandidateId().equals(candidateId)) {
            throw new SecurityException("Access denied to this application's chat");
        }

        return chatSessionRepository.findByApplicationApplicationId(applicationId)
                .orElseGet(() -> {
                    logger.info("Creating new chat session for application {}", applicationId);
                    ChatSession newSession = new ChatSession();
                    newSession.setApplication(application);
                    return chatSessionRepository.save(newSession);
                });
    }

    public ChatMessage addCandidateMessageAndGetAiResponse(UUID sessionId, String content, UUID candidateId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        if (!session.getApplication().getCandidateId().equals(candidateId)) {
            throw new SecurityException("Access denied to this chat session");
        }

        // Save candidate's message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSession(session);
        userMessage.setSender(MessageSender.CANDIDATE);
        userMessage.setContent(content);
        chatMessageRepository.save(userMessage);

        // Update application status to AI_INTERVIEW if it's still in AI_SCREENING
        Application application = session.getApplication();
        if (application.getStatus() == ApplicationStatus.AI_SCREENING) {
            application.setStatus(ApplicationStatus.AI_INTERVIEW);
            applicationRepository.save(application);
        }

        // Get AI response using AI integration service
        ChatMessage aiMessage = aiIntegrationService.processAndGetAIResponse(sessionId, session);

        logger.info("Saved user message and generated AI response for session {}", sessionId);

        // Update session message count (only for AI messages)
        Integer aiMessageCount = session.getMessageCount();
        session.setMessageCount(aiMessageCount + 1);

        // Check if this is the last message (interview completion)
        if (aiMessageCount + 1 >= 20) { // Assuming 10 messages (5 exchanges) completes an interview
            completeInterview(session);
        }

        return aiMessage;
    }

    /**
     * Complete the interview process
     */
    @Transactional
    public void completeInterview(ChatSession session) {
        // Mark chat session as complete
        session.setStatus(ChatStatus.COMPLETE);
        session.setCompletedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        // Update application status
        Application application = session.getApplication();
        application.setStatus(ApplicationStatus.COMPLETED);
        applicationRepository.save(application);

        // Score the interview
        try {
            aiIntegrationService.scoreInterviewAsync(application.getApplicationId());
        } catch (Exception e) {
            logger.error("Failed to score interview for application {}", application.getApplicationId(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessagesBySession(UUID sessionId, UUID candidateId, Pageable pageable) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        if (!session.getApplication().getCandidateId().equals(candidateId)) {
            throw new SecurityException("Access denied to this chat's history");
        }

        return chatMessageRepository.findBySessionIdOrderBySentAtAsc(sessionId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessagesByApplication(UUID applicationId, Pageable pageable) {
        // Verify application exists
        if (!applicationRepository.existsById(applicationId)) {
            throw new IllegalArgumentException("Application not found");
        }

        // Use the direct query method for better performance
        return chatMessageRepository.findByApplicationIdOrderBySentAtAsc(applicationId, pageable);
    }
}