package de.tum.devops.assess.dto;

import de.tum.devops.persistence.enums.RecommendationEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assessment DTO according to api-documentation.yaml
 * 
 * Schema definition:
 * AssessmentDto:
 * properties:
 * assessmentID: string (uuid)
 * applicationID: string (uuid)
 * resumeScore: number (float, 0-100)
 * interviewScore: number (float, 0-100)
 * finalScore: number (float, 0-100)
 * resumeAnalysis: string
 * interviewSummary: string
 * recommendation: string (enum: [RECOMMEND, CONSIDER, NOT_RECOMMEND])
 * creationTimestamp: string (date-time)
 * lastModifiedTimestamp: string (date-time)
 */
public class AssessmentDto {

    private UUID assessmentID;
    private UUID applicationID;
    private Float resumeScore;
    private Float interviewScore;
    private Float finalScore;
    private String resumeAnalysis;
    private String interviewSummary;
    private RecommendationEnum recommendation;
    private LocalDateTime creationTimestamp;
    private LocalDateTime lastModifiedTimestamp;

    // Constructors
    public AssessmentDto() {
    }

    public AssessmentDto(UUID assessmentID, UUID applicationID, Float resumeScore,
            Float interviewScore, Float finalScore, String resumeAnalysis,
            String interviewSummary, RecommendationEnum recommendation,
            LocalDateTime creationTimestamp, LocalDateTime lastModifiedTimestamp) {
        this.assessmentID = assessmentID;
        this.applicationID = applicationID;
        this.resumeScore = resumeScore;
        this.interviewScore = interviewScore;
        this.finalScore = finalScore;
        this.resumeAnalysis = resumeAnalysis;
        this.interviewSummary = interviewSummary;
        this.recommendation = recommendation;
        this.creationTimestamp = creationTimestamp;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    // Getters and Setters
    public UUID getAssessmentID() {
        return assessmentID;
    }

    public void setAssessmentID(UUID assessmentID) {
        this.assessmentID = assessmentID;
    }

    public UUID getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(UUID applicationID) {
        this.applicationID = applicationID;
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
}