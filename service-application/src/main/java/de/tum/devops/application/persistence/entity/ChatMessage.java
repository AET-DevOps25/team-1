package de.tum.devops.application.persistence.entity;

import de.tum.devops.application.persistence.enums.MessageSender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatMessage entity mapping to chat_messages table
 * <p>
 * Database schema:
 * CREATE TABLE chat_messages (
 * message_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 * session_id UUID NOT NULL,
 * sender message_sender NOT NULL,
 * content TEXT NOT NULL,
 * sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session_id", columnList = "session_id")
})
public class ChatMessage {

    @Id
    @Column(name = "message_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, columnDefinition = "message_sender")
    private MessageSender sender;

    @NotNull
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    // Constructors
    public ChatMessage() {
    }

    public ChatMessage(ChatSession session, MessageSender sender, String content) {
        this();
        this.session = session;
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

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId=" + messageId +
                ", session=" + session +
                ", sender=" + sender +
                ", sentAt=" + sentAt +
                '}';
    }
}