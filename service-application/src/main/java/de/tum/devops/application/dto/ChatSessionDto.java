package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.ChatStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatSession DTO according to api-documentation.yaml
 * <p>
 * Schema definition:
 * ChatSessionDto:
 * properties:
 * sessionID: string (uuid)
 * applicationID: string (uuid)
 * status: string (enum: [ACTIVE, COMPLETED, EXPIRED])
 * startTimestamp: string (date-time)
 * endTimestamp: string (date-time)
 * messageCount: integer
 */
public class ChatSessionDto {

    private UUID sessionId;
    private UUID applicationId;
    private ChatStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer messageCount;

    // Constructors
    public ChatSessionDto() {
    }

    public ChatSessionDto(UUID sessionId, UUID applicationId) {
        this.sessionId = sessionId;
        this.applicationId = applicationId;
    }

    public ChatSessionDto(UUID sessionId, UUID applicationId, ChatStatus status,
                          LocalDateTime startedAt, LocalDateTime completedAt,
                          Integer messageCount) {
        this.sessionId = sessionId;
        this.applicationId = applicationId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.messageCount = messageCount;
    }

    public ChatSessionDto(ChatSession session) {
        this.sessionId = session.getSessionId();
        this.applicationId = session.getApplication().getApplicationId();
        this.status = session.getStatus();
        this.startedAt = session.getStartedAt();
        this.completedAt = session.getCompletedAt();
        this.messageCount = session.getMessageCount();
    }

    // Getters and Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}