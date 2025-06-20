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
 * 
 * Database schema:
 * CREATE TABLE applications (
 * application_id UUID PRIMARY KEY,
 * submission_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * status application_status NOT NULL DEFAULT 'SUBMITTED',
 * resume_content TEXT NOT NULL,
 * original_resume_filename VARCHAR(255),
 * last_modified_timestamp TIMESTAMP,
 * candidate_id UUID REFERENCES users(user_id) NOT NULL,
 * job_id UUID REFERENCES jobs(job_id) NOT NULL,
 * hr_decision decision_enum,
 * hr_comments TEXT
 * );
 */
@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_applications_candidate_id", columnList = "candidate_id"),
        @Index(name = "idx_applications_job_id", columnList = "job_id")
})
public class Application {

    @Id
    @Column(name = "application_id", columnDefinition = "UUID")
    private UUID applicationId;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @NotNull
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

    @Column(name = "hr_decision", columnDefinition = "decision_enum")
    private DecisionEnum hrDecision;

    @Column(name = "hr_comments", columnDefinition = "TEXT")
    private String hrComments;

    // Constructors
    public Application() {
        this.applicationId = UUID.randomUUID();
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