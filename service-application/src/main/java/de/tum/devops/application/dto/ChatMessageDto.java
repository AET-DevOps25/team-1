package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.enums.MessageSender;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatMessage DTO according to api-documentation.yaml
 * 
 * Schema definition:
 * ChatMessageDto:
 * properties:
 * messageID: string (uuid)
 * sessionID: string (uuid)
 * sender: string (enum: [AI, CANDIDATE])
 * content: string
 * timestamp: string (date-time)
 */
public class ChatMessageDto {

    private UUID messageID;
    private UUID sessionID;
    private MessageSender sender;
    private String content;
    private LocalDateTime timestamp;

    // Constructors
    public ChatMessageDto() {
    }

    public ChatMessageDto(UUID messageID, UUID sessionID, MessageSender sender,
            String content, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.sessionID = sessionID;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public UUID getMessageID() {
        return messageID;
    }

    public void setMessageID(UUID messageID) {
        this.messageID = messageID;
    }

    public UUID getSessionID() {
        return sessionID;
    }

    public void setSessionID(UUID sessionID) {
        this.sessionID = sessionID;
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
}