package de.tum.devops.assess.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application data transfer object for embedded application information
 */
public class ApplicationDto {

    private UUID applicationID;
    private LocalDateTime submissionTimestamp;
    private String status;
    private String resumeContent;
    private String originalResumeFilename;
    private LocalDateTime lastModifiedTimestamp;
    private UserDto candidate;
    private JobDto job;
    private AssessmentDto assessment;

    // Constructors
    public ApplicationDto() {
    }

    public ApplicationDto(UUID applicationID, LocalDateTime submissionTimestamp, String status,
            String resumeContent, String originalResumeFilename, LocalDateTime lastModifiedTimestamp,
            UserDto candidate, JobDto job, AssessmentDto assessment) {
        this.applicationID = applicationID;
        this.submissionTimestamp = submissionTimestamp;
        this.status = status;
        this.resumeContent = resumeContent;
        this.originalResumeFilename = originalResumeFilename;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        this.candidate = candidate;
        this.job = job;
        this.assessment = assessment;
    }

    // Getters and setters
    public UUID getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(UUID applicationID) {
        this.applicationID = applicationID;
    }

    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResumeContent() {
        return resumeContent;
    }

    public void setResumeContent(String resumeContent) {
        this.resumeContent = resumeContent;
    }

    public String getOriginalResumeFilename() {
        return originalResumeFilename;
    }

    public void setOriginalResumeFilename(String originalResumeFilename) {
        this.originalResumeFilename = originalResumeFilename;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public UserDto getCandidate() {
        return candidate;
    }

    public void setCandidate(UserDto candidate) {
        this.candidate = candidate;
    }

    public JobDto getJob() {
        return job;
    }

    public void setJob(JobDto job) {
        this.job = job;
    }

    public AssessmentDto getAssessment() {
        return assessment;
    }

    public void setAssessment(AssessmentDto assessment) {
        this.assessment = assessment;
    }
}