package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.enums.ChatStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatSession DTO according to api-documentation.yaml
 * 
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

    private UUID sessionID;
    private UUID applicationID;
    private ChatStatus status;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Integer messageCount;

    // Constructors
    public ChatSessionDto() {
    }

    public ChatSessionDto(UUID sessionID, UUID applicationID) {
        this.sessionID = sessionID;
        this.applicationID = applicationID;
    }

    public ChatSessionDto(UUID sessionID, UUID applicationID, ChatStatus status,
            LocalDateTime startTimestamp, LocalDateTime endTimestamp,
            Integer messageCount) {
        this.sessionID = sessionID;
        this.applicationID = applicationID;
        this.status = status;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.messageCount = messageCount;
    }

    // Getters and Setters
    public UUID getSessionID() {
        return sessionID;
    }

    public void setSessionID(UUID sessionID) {
        this.sessionID = sessionID;
    }

    public UUID getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(UUID applicationID) {
        this.applicationID = applicationID;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}