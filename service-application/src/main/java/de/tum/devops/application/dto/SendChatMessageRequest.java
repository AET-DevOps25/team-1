package de.tum.devops.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for sending chat messages according to
 * api-openapi-original-design.yaml
 * <p>
 * Schema definition:
 * SendChatMessageRequest:
 * properties:
 * content: string (required)
 */
public class SendChatMessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    // Constructors
    public SendChatMessageRequest() {
    }

    public SendChatMessageRequest(String content) {
        this.content = content;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}