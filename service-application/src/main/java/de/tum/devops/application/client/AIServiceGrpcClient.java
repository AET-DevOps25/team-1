package de.tum.devops.application.client;

import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.enums.MessageSender;
import de.tum.devops.application.persistence.enums.RecommendationEnum;
import de.tum.devops.grpc.ai.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Client for AI service gRPC API using Spring gRPC
 */
@Component
public class AIServiceGrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceGrpcClient.class);

    private final AIServiceGrpc.AIServiceBlockingStub blockingStub;
    private final AIServiceGrpc.AIServiceStub asyncStub;


    public AIServiceGrpcClient(AIServiceGrpc.AIServiceBlockingStub blockingStub, AIServiceGrpc.AIServiceStub asyncStub) {
        this.blockingStub = blockingStub;
        this.asyncStub = asyncStub;
    }

    /**
     * Get AI reply for a chat message
     *
     * @param resumeText      Candidate's resume text
     * @param jobTitle        Job title
     * @param jobDescription  Job description
     * @param jobRequirements Job requirements
     * @param chatHistory     Chat history
     * @return AI response message
     */
    public String getChatReply(String resumeText, String jobTitle, String jobDescription,
                               String jobRequirements, List<ChatMessage> chatHistory) {
        try {
            // Convert domain chat messages to gRPC chat messages
            List<de.tum.devops.grpc.ai.ChatMessage> grpcMessages = chatHistory.stream()
                    .map(this::convertToGrpcChatMessage)
                    .collect(Collectors.toList());

            // Build request
            ChatReplyRequest request = ChatReplyRequest.newBuilder()
                    .setResumeText(resumeText)
                    .setJobTitle(jobTitle)
                    .setJobDescription(jobDescription)
                    .setJobRequirements(jobRequirements)
                    .addAllChatHistory(grpcMessages)
                    .build();

            // For streaming response, we need to collect all tokens
            StringBuilder fullResponse = new StringBuilder();
            CountDownLatch finishLatch = new CountDownLatch(1);

            // Make async call with streaming response
            asyncStub.chatReply(request, new StreamObserver<ChatReplyResponse>() {
                @Override
                public void onNext(ChatReplyResponse response) {
                    fullResponse.append(response.getAiMessage());
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("Error in chat reply streaming", t);
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                }
            });

            // Wait for streaming to complete (with timeout)
            if (!finishLatch.await(30, TimeUnit.SECONDS)) {
                logger.warn("Chat reply streaming timed out");
                return "I'm sorry, but I couldn't generate a response in time. Please try again.";
            }

            return fullResponse.toString();
        } catch (Exception e) {
            logger.error("Error getting chat reply", e);
            return "I apologize, but I encountered an error. Please try again later.";
        }
    }

    public void chatReplyStream(ChatReplyRequest request, StreamObserver<ChatReplyResponse> responseObserver) {
        try {
            asyncStub.chatReply(request, responseObserver);
        } catch (Exception e) {
            logger.error("Error starting chat reply stream", e);
            responseObserver.onError(e);
        }
    }

    /**
     * Score a resume against job requirements
     *
     * @param resumeText      Candidate's resume text
     * @param jobRequirements Job requirements
     * @return Resume score response with score, comment and recommendation
     */
    public ScoreResumeResponse scoreResume(String resumeText, String jobRequirements) {
        try {
            ScoreResumeRequest request = ScoreResumeRequest.newBuilder()
                    .setResumeText(resumeText)
                    .setJobRequirements(jobRequirements)
                    .build();

            return blockingStub.scoreResume(request);
        } catch (Exception e) {
            logger.error("Error scoring resume", e);
            // Return default response with error message
            return ScoreResumeResponse.newBuilder()
                    .setResumeScore(0)
                    .setComment("Error scoring resume: " + e.getMessage())
                    .setRecommendation(de.tum.devops.grpc.ai.RecommendationEnum.NOT_RECOMMEND)
                    .build();
        }
    }

    /**
     * Score an interview based on chat history
     *
     * @param resumeText      Candidate's resume text
     * @param jobRequirements Job requirements
     * @param chatHistory     Chat history
     * @return Interview score response with score, comment and recommendation
     */
    public ScoreInterviewResponse scoreInterview(String resumeText, String jobRequirements, List<ChatMessage> chatHistory) {
        try {
            // Convert domain chat messages to gRPC chat messages
            List<de.tum.devops.grpc.ai.ChatMessage> grpcMessages = chatHistory.stream()
                    .map(this::convertToGrpcChatMessage)
                    .collect(Collectors.toList());

            ScoreInterviewRequest request = ScoreInterviewRequest.newBuilder()
                    .setResumeText(resumeText)
                    .setJobRequirements(jobRequirements)
                    .addAllChatHistory(grpcMessages)
                    .build();

            return blockingStub.scoreInterview(request);
        } catch (Exception e) {
            logger.error("Error scoring interview", e);
            // Return default response with error message
            return ScoreInterviewResponse.newBuilder()
                    .setInterviewScore(0)
                    .setComment("Error scoring interview: " + e.getMessage())
                    .setRecommendation(de.tum.devops.grpc.ai.RecommendationEnum.NOT_RECOMMEND)
                    .build();
        }
    }

    /**
     * Convert domain chat message to gRPC chat message
     */
    public de.tum.devops.grpc.ai.ChatMessage convertToGrpcChatMessage(ChatMessage message) {
        de.tum.devops.grpc.ai.ChatMessage.Sender sender;

        if (message.getSender() == MessageSender.AI) {
            sender = de.tum.devops.grpc.ai.ChatMessage.Sender.SENDER_AI;
        } else if (message.getSender() == MessageSender.CANDIDATE) {
            sender = de.tum.devops.grpc.ai.ChatMessage.Sender.SENDER_CANDIDATE;
        } else {
            sender = de.tum.devops.grpc.ai.ChatMessage.Sender.SENDER_UNSPECIFIED;
        }

        return de.tum.devops.grpc.ai.ChatMessage.newBuilder()
                .setSender(sender)
                .setContent(message.getContent())
                .build();
    }

    /**
     * Convert gRPC recommendation enum to domain recommendation enum
     */
    public RecommendationEnum convertToRecommendationEnum(de.tum.devops.grpc.ai.RecommendationEnum grpcEnum) {
        return switch (grpcEnum) {
            case RECOMMEND -> RecommendationEnum.RECOMMEND;
            case CONSIDER -> RecommendationEnum.CONSIDER;
            default -> RecommendationEnum.NOT_RECOMMEND;
        };
    }
}