package de.tum.devops.application.dto;

public class ChatInitializationDto {

    private ChatSessionDto session;
    private ChatMessageDto initialMessage;

    public ChatInitializationDto(ChatSessionDto session, ChatMessageDto initialMessage) {
        this.session = session;
        this.initialMessage = initialMessage;
    }

    public ChatInitializationDto() {
    }

    public ChatSessionDto getSession() {
        return session;
    }

    public void setSession(ChatSessionDto session) {
        this.session = session;
    }

    public ChatMessageDto getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(ChatMessageDto initialMessage) {
        this.initialMessage = initialMessage;
    }
}
