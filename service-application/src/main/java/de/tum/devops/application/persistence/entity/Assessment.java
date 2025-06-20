package de.tum.devops.application.persistence.entity;

import de.tum.devops.application.persistence.enums.RecommendationEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assessment entity mapping to assessments table
 * 
 * Database schema:
 * CREATE TABLE assessments (
 * assessment_id UUID PRIMARY KEY,
 * application_id UUID REFERENCES applications(application_id) NOT NULL,
 * resume_score FLOAT CHECK (resume_score BETWEEN 0 AND 100),
 * interview_score FLOAT CHECK (interview_score BETWEEN 0 AND 100),
 * final_score FLOAT CHECK (final_score BETWEEN 0 AND 100),
 * resume_analysis TEXT,
 * interview_summary TEXT,
 * recommendation recommendation_enum,
 * creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * last_modified_timestamp TIMESTAMP
 * );
 */
@Entity
@Table(name = "assessments", indexes = {
        @Index(name = "idx_assessments_application_id", columnList = "application_id")
})
public class Assessment {

    @Id
    @Column(name = "assessment_id", columnDefinition = "UUID")
    private UUID assessmentId;

    @NotNull
    @Column(name = "application_id", nullable = false, columnDefinition = "UUID")
    private UUID applicationId;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "resume_score")
    private Float resumeScore;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "interview_score")
    private Float interviewScore;

    @Column(name = "resume_comment", columnDefinition = "TEXT")
    private String resumeComment;

    @Column(name = "interview_comment", columnDefinition = "TEXT")
    private String interviewComment;

    @Column(name = "recommendation", columnDefinition = "recommendation_enum")
    private RecommendationEnum recommendation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Assessment() {
        this.assessmentId = UUID.randomUUID();
    }

    public Assessment(UUID applicationId) {
        this();
        this.applicationId = applicationId;
    }

    // Getters and Setters
    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public Float getResumeScore() {
        return resumeScore;
    }

    public void setResumeScore(Float resumeScore) {
        this.resumeScore = resumeScore;
    }

    public Float getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(Float interviewScore) {
        this.interviewScore = interviewScore;
    }

    public String getResumeComment() {
        return resumeComment;
    }

    public void setResumeComment(String resumeComment) {
        this.resumeComment = resumeComment;
    }

    public String getInterviewComment() {
        return interviewComment;
    }

    public void setInterviewComment(String interviewComment) {
        this.interviewComment = interviewComment;
    }

    public RecommendationEnum getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(RecommendationEnum recommendation) {
        this.recommendation = recommendation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "assessmentId=" + assessmentId +
                ", applicationId=" + applicationId +
                ", resumeScore=" + resumeScore +
                ", interviewScore=" + interviewScore +
                ", recommendation=" + recommendation +
                '}';
    }
}