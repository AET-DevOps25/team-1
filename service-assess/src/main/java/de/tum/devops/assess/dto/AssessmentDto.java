package de.tum.devops.assess.dto;

import de.tum.devops.assess.entity.AssessmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assessment data transfer object for API responses
 */
public class AssessmentDto {

    private UUID assessmentID;
    private ApplicationDto application;
    private String assessmentType;
    private AssessmentStatus status;
    private Integer score;
    private Integer maxScore;
    private String feedback;
    private String assessmentData;
    private LocalDateTime creationTimestamp;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastModifiedTimestamp;
    private UserDto hrCreator;

    // Constructors
    public AssessmentDto() {
    }

    public AssessmentDto(UUID assessmentID, ApplicationDto application, String assessmentType,
            AssessmentStatus status, Integer score, Integer maxScore, String feedback,
            String assessmentData, LocalDateTime creationTimestamp, LocalDateTime startTime,
            LocalDateTime endTime, LocalDateTime lastModifiedTimestamp, UserDto hrCreator) {
        this.assessmentID = assessmentID;
        this.application = application;
        this.assessmentType = assessmentType;
        this.status = status;
        this.score = score;
        this.maxScore = maxScore;
        this.feedback = feedback;
        this.assessmentData = assessmentData;
        this.creationTimestamp = creationTimestamp;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        this.hrCreator = hrCreator;
    }

    // Getters and Setters
    public UUID getAssessmentID() {
        return assessmentID;
    }

    public void setAssessmentID(UUID assessmentID) {
        this.assessmentID = assessmentID;
    }

    public ApplicationDto getApplication() {
        return application;
    }

    public void setApplication(ApplicationDto application) {
        this.application = application;
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

    public UserDto getHrCreator() {
        return hrCreator;
    }

    public void setHrCreator(UserDto hrCreator) {
        this.hrCreator = hrCreator;
    }
}