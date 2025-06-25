package de.tum.devops.application.persistence.entity;

import de.tum.devops.application.persistence.enums.ChatStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ChatSession entity mapping to chat_sessions table
 * <p>
 * Database schema:
 * CREATE TABLE chat_sessions (
 * session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 * application_id UUID NOT NULL UNIQUE,
 * status chat_status DEFAULT 'ACTIVE',
 * message_count INTEGER DEFAULT 0,
 * started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * completed_at TIMESTAMP
 * );
 */
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_application_id", columnList = "application_id")
})
public class ChatSession {

    @Id
    @Column(name = "session_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChatStatus status = ChatStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "message_count")
    private Integer messageCount = 0;

    // Constructors
    public ChatSession() {
    }

    public ChatSession(Application application) {
        this();
        this.application = application;
    }

    // Getters and Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
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

    @Override
    public String toString() {
        return "ChatSession{" +
                "sessionId=" + sessionId +
                ", application=" + application +
                ", status=" + status +
                ", messageCount=" + messageCount +
                '}';
    }
}