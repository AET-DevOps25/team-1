package de.tum.devops.application.service;

import de.tum.devops.application.dto.ChatInitializationDto;
import de.tum.devops.application.dto.ChatMessageDto;
import de.tum.devops.application.dto.ChatSessionDto;
import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.ApplicationStatus;
import de.tum.devops.application.persistence.enums.ChatStatus;
import de.tum.devops.application.persistence.enums.MessageSender;
import de.tum.devops.application.persistence.repository.ApplicationRepository;
import de.tum.devops.application.persistence.repository.ChatMessageRepository;
import de.tum.devops.application.persistence.repository.ChatSessionRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ApplicationRepository applicationRepository;
    private final AIIntegrationService aiIntegrationService;

    private final int aiMessageLimit = 2;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       ApplicationRepository applicationRepository,
                       AIIntegrationService aiIntegrationService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.applicationRepository = applicationRepository;
        this.aiIntegrationService = aiIntegrationService;
    }

    @Transactional
    public ChatInitializationDto initiateChatSession(UUID applicationId, UUID candidateId) {
        ChatSession session = createOrGetSession(applicationId, candidateId);
        ChatMessage initialMessage;

        List<ChatMessage> aiMessages = chatMessageRepository.findBySessionIdAndSenderOrderBySentAtAsc(session.getSessionId(), MessageSender.AI);

        if (aiMessages.isEmpty()) {
            logger.info("No AI messages found for session {}, generating initial question.", session.getSessionId());
            initialMessage = addInitialAiMessage(session);
        } else {
            initialMessage = aiMessages.getLast();
            logger.info("Found existing initial AI message {} for session {}.", initialMessage.getMessageId(), session.getSessionId());
        }

        return new ChatInitializationDto(new ChatSessionDto(session), new ChatMessageDto(initialMessage));
    }

    private ChatMessage addInitialAiMessage(ChatSession session) {
        // Get the AI's first response
        return aiIntegrationService.processAndGetAIResponse(session.getSessionId(), session);
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
                    newSession.setSessionId(UUID.randomUUID());
                    newSession.setApplication(application);
                    return chatSessionRepository.save(newSession);
                });
    }

    public void addCandidateMessageAndGetAiResponseStream(UUID sessionId, String content, UUID candidateId, SseEmitter emitter) {
        // Step 1: Prepare session and save user message (transactional part)
        PreparedSession preparedSession = prepareForStreaming(sessionId, content, candidateId);
        ChatSession session = preparedSession.session;

        final String endingMessage = "Interview session is already complete. Do you have any questions about our company? I can answer some of them if I know it. For security reasons, no further context will be retrieved at this session.";

        StringBuilder fullAiResponse = new StringBuilder();

        // step 2: Check if the session is already complete. If so, then answer as NormalQA
        if (preparedSession.isAlreadyCompleteMoreThanOnce) {
            // Use NormalQA stream which needs only user question
            StreamObserver<de.tum.devops.grpc.ai.ChatReplyResponse> qaObserver = new StreamObserver<>() {
                @Override
                public void onNext(de.tum.devops.grpc.ai.ChatReplyResponse response) {
                    try {
                        String chunk = response.getAiMessage();
                        if (!chunk.isEmpty()) {
                            fullAiResponse.append(chunk);
                            ChatMessageDto chunkDto = new ChatMessageDto(null, session.getSessionId(), MessageSender.AI, chunk, LocalDateTime.now());
                            emitter.send(SseEmitter.event().name("message-chunk").data(chunkDto));
                        }
                    } catch (IOException e) {
                        logger.error("Error sending SSE QA chunk for session {}", sessionId, e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("NormalQA stream error for session {}", sessionId, t);
                    try {
                        emitter.send(SseEmitter.event().name("error").data("Error during QA."));
                    } catch (IOException ignored) {
                        logger.error("Error sending SSE error for session {}", sessionId, t);
                    }
                    emitter.completeWithError(t);
                }

                @Override
                public void onCompleted() {
                    logger.info("NormalQA stream completed for session {}.", sessionId);
                    fullAiResponse.append("\n").append(endingMessage);
                    try {
                        ChatMessage finalMessage = saveFinalAiMessage(session.getSessionId(), fullAiResponse.toString(), false);
                        emitter.send(SseEmitter.event().name("message-chunk").data("\n").data(endingMessage));
                        emitter.send(SseEmitter.event().name("stream-end").data(new ChatMessageDto(finalMessage)));
                    } catch (IOException ignored) {
                        logger.error("Error sending SSE stream end for session {}", sessionId);
                    }
                    emitter.complete();
                }
            };

            aiIntegrationService.processNormalQAStream(content, true, qaObserver);
            return;
        }

        if (session.getStatus() == ChatStatus.COMPLETE) {
            logger.warn("Chat session {} is already complete.", sessionId);
            try {
                emitter.send(SseEmitter.event().name("message-chunk").data(endingMessage));
                emitter.complete();
            } catch (Exception e) {
                logger.error("Error during stream completion for session {}", sessionId, e);
                emitter.completeWithError(e);
            }
            return;
        }

        // Step 3: Set up and start the stream (non-transactional part)
        StreamObserver<de.tum.devops.grpc.ai.ChatReplyResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(de.tum.devops.grpc.ai.ChatReplyResponse response) {
                try {
                    String chunk = response.getAiMessage();
                    if (!chunk.isEmpty()) {
                        fullAiResponse.append(chunk);
                        // Send a partial DTO for the chunk
                        ChatMessageDto chunkDto = new ChatMessageDto(null, session.getSessionId(), MessageSender.AI, chunk, LocalDateTime.now());
                        emitter.send(SseEmitter.event().name("message-chunk").data(chunkDto));
                    }
                } catch (IOException e) {
                    logger.error("Error sending SSE event for session {}", sessionId, e);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error from AI service stream for session {}", sessionId, t);
                try {
                    emitter.send(SseEmitter.event().name("error").data("An error occurred while communicating with the AI service."));
                } catch (IOException e) {
                    // Ignore, client likely disconnected.
                } finally {
                    emitter.completeWithError(t);
                }
            }

            @Override
            public void onCompleted() {
                logger.info("AI stream completed for session {}.", sessionId);
                try {
                    // Save the final message and get the entity back
                    ChatMessage finalMessage = saveFinalAiMessage(session.getSessionId(), fullAiResponse.toString(), true);
                    // Send the final, complete DTO
                    emitter.send(SseEmitter.event().name("stream-end").data(new ChatMessageDto(finalMessage)));
                    emitter.complete();
                } catch (Exception e) {
                    logger.error("Error during stream completion for session {}", sessionId, e);
                    emitter.completeWithError(e);
                }
            }
        };

        aiIntegrationService.processAndGetAIResponseStream(sessionId, session, responseObserver);
    }

    public record PreparedSession(ChatSession session, boolean isAlreadyCompleteMoreThanOnce) {
    }

    @Transactional
    public PreparedSession prepareForStreaming(UUID sessionId, String content, UUID candidateId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        if (!session.getApplication().getCandidateId().equals(candidateId)) {
            throw new SecurityException("Access denied to this chat session");
        }

        if (session.getStatus() == ChatStatus.COMPLETE) {
            logger.warn("Chat session {} is already complete before adding user message.", sessionId);
            return new PreparedSession(session, true);
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setMessageId(UUID.randomUUID());
        userMessage.setSession(session);
        userMessage.setSender(MessageSender.CANDIDATE);
        userMessage.setContent(content);
        chatMessageRepository.save(userMessage);

        if (session.getMessageCount() >= aiMessageLimit) {
            logger.info("Chat session {} has reached the maximum AI message count. Setting status to complete.", sessionId);
            session = completeInterview(session);
            return new PreparedSession(session, false);
        }

        Application application = session.getApplication();
        if (application.getStatus() == ApplicationStatus.AI_SCREENING) {
            application.setStatus(ApplicationStatus.AI_INTERVIEW);
            applicationRepository.save(application);
        }
        return new PreparedSession(session, false);
    }

    @Transactional
    public ChatMessage saveFinalAiMessage(UUID sessionId, String content, boolean isSaveToDB) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found during final save."));

        logger.info("Saving full AI response for session {}", sessionId);
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setMessageId(UUID.randomUUID());
        aiMessage.setSession(session);
        aiMessage.setSender(MessageSender.AI);
        aiMessage.setContent(content);
        if (isSaveToDB) {
            chatMessageRepository.save(aiMessage);

            Integer aiMessageCount = session.getMessageCount();
            session.setMessageCount(aiMessageCount + 1);

            chatSessionRepository.save(session);
        } else {
            aiMessage.setSentAt(LocalDateTime.now());
        }
        return aiMessage;
    }

    /**
     * Complete the interview process
     */
    @Transactional
    public ChatSession completeInterview(ChatSession session) {
        // Mark chat session as complete
        session.setStatus(ChatStatus.COMPLETE);
        session.setCompletedAt(LocalDateTime.now());
        session = chatSessionRepository.saveAndFlush(session);

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
        return session;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDto> getMessagesBySession(UUID sessionId, UUID candidateId, int page, int size, String role) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));

        if (role.equals("CANDIDATE")) {
            if (!session.getApplication().getCandidateId().equals(candidateId)) {
                throw new SecurityException("Access denied to this chat's history");
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySentAtAsc(sessionId, pageable);
        // transform messages to ChatMessageDto
        return messages.map(ChatMessageDto::new);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDto> getMessagesByApplication(UUID applicationId, int page, int size) {
        // Verify application exists
        if (!applicationRepository.existsById(applicationId)) {
            throw new IllegalArgumentException("Application not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        // Use the direct query method for better performance
        Page<ChatMessage> messages = chatMessageRepository.findByApplicationIdOrderBySentAtAsc(applicationId, pageable);
        // transform messages to ChatMessageDto
        return messages.map(ChatMessageDto::new);
    }
}