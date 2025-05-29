package de.tum.devops.assess.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assessment entity mapping to assessments table
 */
@Entity
@Table(name = "assessments", indexes = {
        @Index(name = "idx_assessments_application_id", columnList = "application_id"),
        @Index(name = "idx_assessments_status", columnList = "status"),
        @Index(name = "idx_assessments_hr_creator_id", columnList = "hr_creator_id")
})
public class Assessment {

    @Id
    @Column(name = "assessment_id")
    private UUID assessmentId;

    @NotNull
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @NotNull
    @Column(name = "assessment_type", nullable = false, length = 100)
    private String assessmentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssessmentStatus status = AssessmentStatus.PENDING;

    @Column(name = "score")
    private Integer score;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "assessment_data", columnDefinition = "TEXT")
    private String assessmentData;

    @CreationTimestamp
    @Column(name = "creation_timestamp")
    private LocalDateTime creationTimestamp;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @UpdateTimestamp
    @Column(name = "last_modified_timestamp")
    private LocalDateTime lastModifiedTimestamp;

    @NotNull
    @Column(name = "hr_creator_id", nullable = false)
    private UUID hrCreatorId;

    // Constructors
    public Assessment() {
        this.assessmentId = UUID.randomUUID();
    }

    public Assessment(UUID applicationId, String assessmentType, UUID hrCreatorId) {
        this();
        this.applicationId = applicationId;
        this.assessmentType = assessmentType;
        this.hrCreatorId = hrCreatorId;
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

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAssessmentData() {
        return assessmentData;
    }

    public void setAssessmentData(String assessmentData) {
        this.assessmentData = assessmentData;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public UUID getHrCreatorId() {
        return hrCreatorId;
    }

    public void setHrCreatorId(UUID hrCreatorId) {
        this.hrCreatorId = hrCreatorId;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "assessmentId=" + assessmentId +
                ", applicationId=" + applicationId +
                ", assessmentType='" + assessmentType + '\'' +
                ", status=" + status +
                '}';
    }
}