package de.tum.devops.application.service;

import de.tum.devops.application.client.AIServiceGrpcClient;
import de.tum.devops.application.client.JobWebClient;
import de.tum.devops.application.dto.JobDto;
import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.entity.Assessment;
import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.MessageSender;
import de.tum.devops.application.persistence.enums.RecommendationEnum;
import de.tum.devops.application.persistence.repository.ApplicationRepository;
import de.tum.devops.application.persistence.repository.AssessmentRepository;
import de.tum.devops.application.persistence.repository.ChatMessageRepository;
import de.tum.devops.grpc.ai.ChatReplyResponse;
import de.tum.devops.grpc.ai.ScoreInterviewResponse;
import de.tum.devops.grpc.ai.ScoreResumeResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for AI integration
 */
@Service
public class AIIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(AIIntegrationService.class);

    private final AIServiceGrpcClient aiServiceClient;
    private final JobWebClient jobWebClient;
    private final ApplicationRepository applicationRepository;
    private final AssessmentRepository assessmentRepository;
    private final ChatMessageRepository chatMessageRepository;

    public AIIntegrationService(AIServiceGrpcClient aiServiceClient,
                                JobWebClient jobWebClient,
                                ApplicationRepository applicationRepository,
                                AssessmentRepository assessmentRepository,
                                ChatMessageRepository chatMessageRepository) {
        this.aiServiceClient = aiServiceClient;
        this.jobWebClient = jobWebClient;
        this.applicationRepository = applicationRepository;
        this.assessmentRepository = assessmentRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Process a chat message from a candidate and get AI response
     *
     * @param sessionId Chat session ID
     * @return AI response message
     */
    @Transactional
    public ChatMessage processAndGetAIResponse(UUID sessionId, ChatSession session) {
        // Get application and job details
        Application application = session.getApplication();
        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();

        if (job == null) {
            logger.error("Failed to fetch job details for application {}", application.getApplicationId());
            return createErrorMessage(session, "Sorry, I couldn't process your message due to missing job information.");
        }

        // Get chat history
        List<ChatMessage> chatHistory = chatMessageRepository.findBySessionIdOrderBySentAtAsc(sessionId);

        // Call AI service for response
        String aiResponse = aiServiceClient.getChatReply(
                application.getResumeText(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                chatHistory
        );

        // Create and save AI response message
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setMessageId(UUID.randomUUID());
        aiMessage.setSession(session);
        aiMessage.setSender(MessageSender.AI);
        aiMessage.setContent(aiResponse);

        // Update session message count
        session.setMessageCount(session.getMessageCount() + 1);

        return chatMessageRepository.save(aiMessage);
    }

    @Transactional
    public void processAndGetAIResponseStream(UUID sessionId, ChatSession session, StreamObserver<ChatReplyResponse> responseObserver) {
        // Get application and job details
        Application application = session.getApplication();
        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();

        if (job == null) {
            logger.error("Failed to fetch job details for application {}", application.getApplicationId());
            // Handle error through the stream
            responseObserver.onError(new IllegalStateException("Could not fetch job details."));
            return;
        }

        // Get chat history
        List<ChatMessage> chatHistory = chatMessageRepository.findBySessionIdOrderBySentAtAsc(sessionId);

        // Build gRPC request
        List<de.tum.devops.grpc.ai.ChatMessage> grpcMessages = chatHistory.stream()
                .map(aiServiceClient::convertToGrpcChatMessage)
                .collect(Collectors.toList());

        de.tum.devops.grpc.ai.ChatReplyRequest request = de.tum.devops.grpc.ai.ChatReplyRequest.newBuilder()
                .setResumeText(application.getResumeText())
                .setJobTitle(job.getTitle())
                .setJobDescription(job.getDescription())
                .setJobRequirements(job.getRequirements())
                .addAllChatHistory(grpcMessages)
                .build();

        // Start the stream
        aiServiceClient.chatReplyStream(request, responseObserver);
    }

    /**
     * Score a resume against job requirements
     *
     * @param applicationId Application ID
     * @return Assessment with resume score
     */
    @Transactional
    public Assessment scoreResumeSync(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();
        if (job == null) {
            logger.error("Failed to fetch job details for application {}", applicationId);
            throw new IllegalStateException("Failed to fetch job details");
        }

        // Call AI service to score resume
        ScoreResumeResponse scoreResponse = aiServiceClient.scoreResume(
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                application.getResumeText()
        );

        // Create or update assessment
        Assessment assessment = assessmentRepository.findByApplication(application)
                .orElse(new Assessment(application));

        assessment.setResumeScore((float) scoreResponse.getResumeScore());
        assessment.setResumeComment(scoreResponse.getComment());
        assessment.setRecommendation(aiServiceClient.convertToRecommendationEnum(scoreResponse.getRecommendation()));

        return assessmentRepository.save(assessment);
    }

    @Async
    @Transactional
    public void scoreResumeAsync(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();
        if (job == null) {
            logger.error("Failed to fetch job details for application {}", applicationId);
            throw new IllegalStateException("Failed to fetch job details");
        }

        // Call AI service to score resume
        ScoreResumeResponse scoreResponse = aiServiceClient.scoreResume(
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                application.getResumeText()
        );

        // Create or update assessment
        Assessment assessment = assessmentRepository.findByApplication(application)
                .orElse(new Assessment(application));
        if (assessment.getAssessmentId() == null) {
            assessment.setAssessmentId(UUID.randomUUID());
        }
        assessment.setResumeScore((float) scoreResponse.getResumeScore());
        assessment.setResumeComment(scoreResponse.getComment());
        assessment.setRecommendation(aiServiceClient.convertToRecommendationEnum(scoreResponse.getRecommendation()));

        assessmentRepository.save(assessment);
    }

    /**
     * Score an interview based on chat history
     *
     * @param applicationId Application ID
     */
    @Async
    @Transactional
    public void scoreInterviewAsync(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();
        if (job == null) {
            logger.error("Failed to fetch job details for application {}", applicationId);
            throw new IllegalStateException("Failed to fetch job details");
        }

        // Get chat history
        List<ChatMessage> chatHistory = chatMessageRepository.findByApplicationIdOrderBySentAtAsc(applicationId);
        if (chatHistory.isEmpty()) {
            logger.warn("No chat history found for application {}", applicationId);
            throw new IllegalStateException("No chat history found for scoring");
        }

        // Call AI service to score interview
        ScoreInterviewResponse scoreResponse = aiServiceClient.scoreInterview(
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                chatHistory
        );

        // Create or update assessment
        Assessment assessment = assessmentRepository.findByApplication(application)
                .orElse(new Assessment(application));

        assessment.setInterviewScore((float) scoreResponse.getInterviewScore());
        assessment.setInterviewComment(scoreResponse.getComment());

        // Only update recommendation if it's worse than the existing one
        RecommendationEnum newRecommendation = aiServiceClient.convertToRecommendationEnum(scoreResponse.getRecommendation());
        if (assessment.getRecommendation() == null ||
                assessment.getRecommendation().ordinal() < newRecommendation.ordinal()) {
            assessment.setRecommendation(newRecommendation);
        }

        assessmentRepository.save(assessment);
    }

    @Transactional
    public Assessment scoreInterviewSync(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();
        if (job == null) {
            logger.error("Failed to fetch job details for application {}", applicationId);
            throw new IllegalStateException("Failed to fetch job details");
        }

        // Get chat history
        List<ChatMessage> chatHistory = chatMessageRepository.findByApplicationIdOrderBySentAtAsc(applicationId);
        if (chatHistory.isEmpty()) {
            logger.warn("No chat history found for application {}", applicationId);
            throw new IllegalStateException("No chat history found for scoring");
        }

        // Call AI service to score interview
        ScoreInterviewResponse scoreResponse = aiServiceClient.scoreInterview(
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                chatHistory
        );

        // Create or update assessment
        Assessment assessment = assessmentRepository.findByApplication(application)
                .orElse(new Assessment(application));

        assessment.setInterviewScore((float) scoreResponse.getInterviewScore());
        assessment.setInterviewComment(scoreResponse.getComment());

        // Only update recommendation if it's worse than the existing one
        RecommendationEnum newRecommendation = aiServiceClient.convertToRecommendationEnum(scoreResponse.getRecommendation());
        if (assessment.getRecommendation() == null ||
                assessment.getRecommendation().ordinal() < newRecommendation.ordinal()) {
            assessment.setRecommendation(newRecommendation);
        }

        return assessmentRepository.save(assessment);
    }

    /**
     * Create error message when AI service fails
     */
    private ChatMessage createErrorMessage(ChatSession session, String errorMessage) {
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());
        message.setSession(session);
        message.setSender(MessageSender.AI);
        message.setContent(errorMessage);
        message.setSentAt(LocalDateTime.now());
        // Not save error message
        return message;
    }
}