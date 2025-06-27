package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.entity.Assessment;
import de.tum.devops.application.persistence.enums.RecommendationEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssessmentDto {
    private UUID assessmentId;
    private UUID applicationId;
    private Float resumeScore;
    private Float interviewScore;
    private String resumeComment;
    private String interviewComment;
    private RecommendationEnum recommendation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AssessmentDto(Assessment assessment) {
        this.assessmentId = assessment.getAssessmentId();
        this.applicationId = assessment.getApplication().getApplicationId();
        this.resumeScore = assessment.getResumeScore();
        this.interviewScore = assessment.getInterviewScore();
        this.resumeComment = assessment.getResumeComment();
        this.interviewComment = assessment.getInterviewComment();
        this.recommendation = assessment.getRecommendation();
        this.createdAt = assessment.getCreatedAt();
        this.updatedAt = assessment.getUpdatedAt();
    }

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
}