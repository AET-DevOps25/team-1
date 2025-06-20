package de.tum.devops.assess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating assessments
 */
public class CreateAssessmentRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @NotBlank(message = "Assessment type is required")
    private String assessmentType;

    private Integer maxScore;

    private String assessmentData;

    public CreateAssessmentRequest() {
    }

    public CreateAssessmentRequest(UUID applicationId, String assessmentType, Integer maxScore, String assessmentData) {
        this.applicationId = applicationId;
        this.assessmentType = assessmentType;
        this.maxScore = maxScore;
        this.assessmentData = assessmentData;
    }

    // Getters and setters
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

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public String getAssessmentData() {
        return assessmentData;
    }

    public void setAssessmentData(String assessmentData) {
        this.assessmentData = assessmentData;
    }
}