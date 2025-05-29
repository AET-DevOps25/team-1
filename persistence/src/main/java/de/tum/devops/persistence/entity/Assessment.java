package de.tum.devops.persistence.entity;

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

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "final_score")
    private Float finalScore;

    @Column(name = "resume_analysis", columnDefinition = "TEXT")
    private String resumeAnalysis;

    @Column(name = "interview_summary", columnDefinition = "TEXT")
    private String interviewSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", columnDefinition = "recommendation_enum")
    private RecommendationEnum recommendation;

    @CreationTimestamp
    @Column(name = "creation_timestamp", updatable = false)
    private LocalDateTime creationTimestamp;

    @UpdateTimestamp
    @Column(name = "last_modified_timestamp")
    private LocalDateTime lastModifiedTimestamp;

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

    public Float getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Float finalScore) {
        this.finalScore = finalScore;
    }

    public String getResumeAnalysis() {
        return resumeAnalysis;
    }

    public void setResumeAnalysis(String resumeAnalysis) {
        this.resumeAnalysis = resumeAnalysis;
    }

    public String getInterviewSummary() {
        return interviewSummary;
    }

    public void setInterviewSummary(String interviewSummary) {
        this.interviewSummary = interviewSummary;
    }

    public RecommendationEnum getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(RecommendationEnum recommendation) {
        this.recommendation = recommendation;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "assessmentId=" + assessmentId +
                ", applicationId=" + applicationId +
                ", resumeScore=" + resumeScore +
                ", interviewScore=" + interviewScore +
                ", finalScore=" + finalScore +
                ", recommendation=" + recommendation +
                '}';
    }
}