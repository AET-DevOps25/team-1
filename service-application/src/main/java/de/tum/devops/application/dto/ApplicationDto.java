package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application DTO according to api-documentation.yaml
 * 
 * Schema definition:
 * ApplicationDto:
 * properties:
 * applicationID: string (uuid)
 * submissionTimestamp: string (date-time)
 * status: string (enum: [SUBMITTED, AI_SCREENING, AI_INTERVIEW, COMPLETED,
 * SHORTLISTED, REJECTED, HIRED])
 * resumeContent: string
 * originalResumeFilename: string
 * lastModifiedTimestamp: string (date-time)
 * candidate: UserDto
 * job: JobDto
 * assessment: AssessmentDto (optional, can be null)
 */
public class ApplicationDto {

    private UUID applicationID;
    private LocalDateTime submissionTimestamp;
    private ApplicationStatus status;
    private String resumeContent;
    private String originalResumeFilename;
    private LocalDateTime lastModifiedTimestamp;
    private UserDto candidate;
    private JobDto job;
    private Object assessment;

    // Constructors
    public ApplicationDto() {
    }

    public ApplicationDto(UUID applicationID, LocalDateTime submissionTimestamp,
            ApplicationStatus status, String resumeContent,
            String originalResumeFilename, LocalDateTime lastModifiedTimestamp,
            UserDto candidate, JobDto job, Object assessment) {
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

    // Getters and Setters
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

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
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

    public Object getAssessment() {
        return assessment;
    }

    public void setAssessment(Object assessment) {
        this.assessment = assessment;
    }
}
