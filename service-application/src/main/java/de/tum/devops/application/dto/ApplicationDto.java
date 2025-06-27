package de.tum.devops.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tum.devops.application.persistence.enums.ApplicationStatus;
import de.tum.devops.application.persistence.enums.ChatStatus;
import de.tum.devops.application.persistence.enums.DecisionEnum;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application DTO according to api-documentation.yaml
 * <p>
 * Schema definition:
 * ApplicationDto:
 * properties:
 * applicationId: string (uuid)
 * jobId: string (uuid)
 * candidateId: string (uuid)
 * status: string (enum: [SUBMITTED, AI_SCREENING, AI_INTERVIEW, COMPLETED,
 * SHORTLISTED, REJECTED, HIRED])
 * resumeText: string
 * resumeFilePath: string
 * hrDecision: string (enum: [APPROVED, REJECTED])
 * hrComments: string
 * chatStatus: string (enum: [OPEN, CLOSED])
 * submittedAt: string (date-time)
 * updatedAt: string (date-time)
 * candidate: UserDto
 * job: JobDto
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationDto {

    private UUID applicationId;
    private UUID jobId;
    private UUID candidateId;
    private ApplicationStatus status;
    @Nullable
    private String resumeText;
    @Nullable
    private String resumeFilePath;
    private DecisionEnum hrDecision;
    private String hrComments;
    private ChatStatus chatStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    // Optional fields for rich response
    private UserDto candidate;
    private JobDto job;
    @Nullable
    private AssessmentDto assessment;

    public ApplicationDto() {
    }

    public ApplicationDto(UUID applicationId, UUID jobId, UUID candidateId, ApplicationStatus status, @Nullable String resumeText, @Nullable String resumeFilePath, DecisionEnum hrDecision, String hrComments, ChatStatus chatStatus, LocalDateTime submittedAt, LocalDateTime updatedAt, UserDto candidate, JobDto job, @Nullable AssessmentDto assessment) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.status = status;
        this.resumeText = resumeText;
        this.resumeFilePath = resumeFilePath;
        this.hrDecision = hrDecision;
        this.hrComments = hrComments;
        this.chatStatus = chatStatus;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
        this.candidate = candidate;
        this.job = job;
        this.assessment = assessment;
    }

    // Getters and Setters

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    @Nullable
    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(@Nullable String resumeText) {
        this.resumeText = resumeText;
    }

    @Nullable
    public String getResumeFilePath() {
        return resumeFilePath;
    }

    public void setResumeFilePath(@Nullable String resumeFilePath) {
        this.resumeFilePath = resumeFilePath;
    }

    public DecisionEnum getHrDecision() {
        return hrDecision;
    }

    public void setHrDecision(DecisionEnum hrDecision) {
        this.hrDecision = hrDecision;
    }

    public String getHrComments() {
        return hrComments;
    }

    public void setHrComments(String hrComments) {
        this.hrComments = hrComments;
    }

    public ChatStatus getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(ChatStatus chatStatus) {
        this.chatStatus = chatStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserDto getCandidate() {
        return candidate;
    }

    public void setCandidate(UserDto candidate) {
        this.candidate = candidate;
    }

    public JobDto getJob() {
        return job;
    }

    public void setJob(JobDto job) {
        this.job = job;
    }

    @Nullable
    public AssessmentDto getAssessment() {
        return assessment;
    }

    public void setAssessment(@Nullable AssessmentDto assessment) {
        this.assessment = assessment;
    }
}