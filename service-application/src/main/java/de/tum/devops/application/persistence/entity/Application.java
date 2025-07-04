package de.tum.devops.application.persistence.entity;

import de.tum.devops.application.persistence.enums.ApplicationStatus;
import de.tum.devops.application.persistence.enums.DecisionEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application entity mapping to applications table
 * <p>
 * Database schema:
 * CREATE TABLE applications (
 * application_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 * job_id UUID NOT NULL,
 * candidate_id UUID NOT NULL,
 * status application_status DEFAULT 'SUBMITTED',
 * resume_text TEXT NOT NULL,
 * resume_file_path TEXT,
 * hr_decision decision_enum,
 * hr_comments TEXT,
 * submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * updated_at TIMESTAMP
 * );
 */
@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_applications_candidate_id", columnList = "candidate_id"),
        @Index(name = "idx_applications_job_id", columnList = "job_id")
})
public class Application {

    @Id
    @Column(name = "application_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID applicationId;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "application_status")
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @NotNull
    @Column(name = "resume_text", nullable = false, columnDefinition = "TEXT")
    private String resumeText;

    @Column(name = "resume_file_path", length = 255)
    private String resumeFilePath;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull
    @Column(name = "candidate_id", nullable = false, columnDefinition = "UUID")
    private UUID candidateId;

    @NotNull
    @Column(name = "job_id", nullable = false, columnDefinition = "UUID")
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hr_decision", columnDefinition = "decision_enum")
    private DecisionEnum hrDecision;

    @Column(name = "hr_comments", columnDefinition = "TEXT")
    private String hrComments;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Assessment assessment;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatSession chatSession;

    // Constructors
    public Application() {
    }

    public Application(UUID candidateId, UUID jobId, String resumeText, String resumeFilePath) {
        this();
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.resumeText = resumeText;
        this.resumeFilePath = resumeFilePath;
    }

    // Getters and Setters
    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getResumeFilePath() {
        return resumeFilePath;
    }

    public void setResumeFilePath(String resumeFilePath) {
        this.resumeFilePath = resumeFilePath;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
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

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public ChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    @Override
    public String toString() {
        return "Application{" +
                "applicationId=" + applicationId +
                ", candidateId=" + candidateId +
                ", jobId=" + jobId +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                '}';
    }
}