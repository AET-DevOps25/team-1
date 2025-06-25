package de.tum.devops.application.persistence.entity;

import de.tum.devops.application.persistence.enums.RecommendationEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assessment entity mapping to assessments table
 * <p>
 * Database schema:
 * CREATE TABLE assessments (
 * assessment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 * application_id UUID NOT NULL,
 * resume_score FLOAT CHECK (resume_score BETWEEN 0 AND 100),
 * interview_score FLOAT CHECK (interview_score BETWEEN 0 AND 100),
 * resume_comment TEXT,
 * interview_comment TEXT,
 * recommendation recommendation_enum,
 * created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * updated_at TIMESTAMP
 * );
 */
@Entity
@Table(name = "assessments", indexes = {
        @Index(name = "idx_assessments_application_id", columnList = "application_id")
})
public class Assessment {

    @Id
    @Column(name = "assessment_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID assessmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

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

    @Enumerated(EnumType.STRING)
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
    }

    public Assessment(Application application) {
        this();
        this.application = application;
    }

    // Getters and Setters
    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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
                ", application=" + application +
                ", resumeScore=" + resumeScore +
                ", interviewScore=" + interviewScore +
                ", recommendation=" + recommendation +
                '}';
    }
}