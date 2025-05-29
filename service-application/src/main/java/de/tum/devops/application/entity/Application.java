package de.tum.devops.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application entity mapping to applications table
 */
@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_applications_candidate_id", columnList = "candidate_id"),
        @Index(name = "idx_applications_job_id", columnList = "job_id"),
        @Index(name = "idx_applications_status", columnList = "status")
})
public class Application {

    @Id
    @Column(name = "application_id")
    private UUID applicationId;

    @NotNull
    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @NotNull
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @CreationTimestamp
    @Column(name = "submission_timestamp")
    private LocalDateTime submissionTimestamp;

    @UpdateTimestamp
    @Column(name = "last_modified_timestamp")
    private LocalDateTime lastModifiedTimestamp;

    @Column(name = "hr_feedback", columnDefinition = "TEXT")
    private String hrFeedback;

    // Constructors
    public Application() {
        this.applicationId = UUID.randomUUID();
    }

    public Application(UUID candidateId, UUID jobId, String resumePath, String coverLetter) {
        this();
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.resumePath = resumePath;
        this.coverLetter = coverLetter;
    }

    // Getters and Setters
    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
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

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public String getHrFeedback() {
        return hrFeedback;
    }

    public void setHrFeedback(String hrFeedback) {
        this.hrFeedback = hrFeedback;
    }

    @Override
    public String toString() {
        return "Application{" +
                "applicationId=" + applicationId +
                ", candidateId=" + candidateId +
                ", jobId=" + jobId +
                ", status=" + status +
                '}';
    }
}