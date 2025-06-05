package de.tum.devops.persistence.entity;

import de.tum.devops.persistence.enums.MessageSender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatMessage entity mapping to chat_messages table
 * 
 * Database schema:
 * CREATE TABLE chat_messages (
 * message_id UUID PRIMARY KEY,
 * session_id UUID REFERENCES chat_sessions(session_id) NOT NULL,
 * sender message_sender NOT NULL,
 * content TEXT NOT NULL,
 * timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session_id", columnList = "session_id")
})
public class ChatMessage {

    @Id
    @Column(name = "message_id", columnDefinition = "UUID")
    private UUID messageId;

    @NotNull
    @Column(name = "session_id", nullable = false, columnDefinition = "UUID")
    private UUID sessionId;

    @NotNull
    @Column(name = "sender", nullable = false, columnDefinition = "message_sender")
    private MessageSender sender;

    @NotNull
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    // Constructors
    public ChatMessage() {
        this.messageId = UUID.randomUUID();
    }

    public ChatMessage(UUID sessionId, MessageSender sender, String content) {
        this();
        this.sessionId = sessionId;
        this.sender = sender;
        this.content = content;
    }

    // Getters and Setters
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public MessageSender getSender() {
        return sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId=" + messageId +
                ", sessionId=" + sessionId +
                ", sender=" + sender +
                ", timestamp=" + timestamp +
                '}';
    }
}