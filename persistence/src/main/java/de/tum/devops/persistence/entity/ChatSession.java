package de.tum.devops.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatSession entity mapping to chat_sessions table
 * 
 * Database schema:
 * CREATE TABLE chat_sessions (
 * session_id UUID PRIMARY KEY,
 * application_id UUID REFERENCES applications(application_id) NOT NULL,
 * status chat_status DEFAULT 'ACTIVE',
 * start_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * end_timestamp TIMESTAMP,
 * message_count INTEGER DEFAULT 0
 * );
 */
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_application_id", columnList = "application_id")
})
public class ChatSession {

    @Id
    @Column(name = "session_id", columnDefinition = "UUID")
    private UUID sessionId;

    @NotNull
    @Column(name = "application_id", nullable = false, columnDefinition = "UUID")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "chat_status")
    private ChatStatus status = ChatStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "start_timestamp", updatable = false)
    private LocalDateTime startTimestamp;

    @Column(name = "end_timestamp")
    private LocalDateTime endTimestamp;

    @Column(name = "message_count")
    private Integer messageCount = 0;

    // Constructors
    public ChatSession() {
        this.sessionId = UUID.randomUUID();
    }

    public ChatSession(UUID applicationId) {
        this();
        this.applicationId = applicationId;
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

    @Override
    public String toString() {
        return "ChatSession{" +
                "sessionId=" + sessionId +
                ", applicationId=" + applicationId +
                ", status=" + status +
                ", messageCount=" + messageCount +
                '}';
    }
}