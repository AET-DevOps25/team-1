package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.enums.MessageSender;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatMessage DTO according to api-openapi-original-design.yaml
 * <p>
 * Schema definition:
 * ChatMessageDto:
 * properties:
 * messageId: string (uuid)
 * sessionId: string (uuid)
 * sender: string (enum: [AI, CANDIDATE])
 * content: string
 * sentAt: string (date-time)
 */
public class ChatMessageDto {

    private UUID messageId;
    private UUID sessionId;
    private MessageSender sender;
    private String content;
    private LocalDateTime sentAt;

    // Constructors
    public ChatMessageDto() {
    }

    public ChatMessageDto(ChatMessage chatMessage) {
        this.messageId = chatMessage.getMessageId();
        this.sessionId = chatMessage.getSession().getSessionId();
        this.sender = chatMessage.getSender();
        this.content = chatMessage.getContent();
        this.sentAt = chatMessage.getSentAt();
    }

    public ChatMessageDto(UUID messageId, UUID sessionId, MessageSender sender,
                          String content, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    public ChatMessageDto(UUID sessionId, MessageSender sender, String content) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.content = content;
        this.sentAt = LocalDateTime.now();
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}