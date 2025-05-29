package de.tum.devops.application.dto;

import de.tum.devops.application.entity.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application data transfer object for API responses
 */
public class ApplicationDto {

    private UUID applicationID;
    private UserDto candidate;
    private JobDto job;
    private ApplicationStatus status;
    private String resumePath;
    private String coverLetter;
    private LocalDateTime submissionTimestamp;
    private LocalDateTime lastModifiedTimestamp;
    private String hrFeedback;

    // Constructors
    public ApplicationDto() {
    }

    public ApplicationDto(UUID applicationID, UserDto candidate, JobDto job,
            ApplicationStatus status, String resumePath, String coverLetter,
            LocalDateTime submissionTimestamp, LocalDateTime lastModifiedTimestamp,
            String hrFeedback) {
        this.applicationID = applicationID;
        this.candidate = candidate;
        this.job = job;
        this.status = status;
        this.resumePath = resumePath;
        this.coverLetter = coverLetter;
        this.submissionTimestamp = submissionTimestamp;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        this.hrFeedback = hrFeedback;
    }

    // Getters and Setters
    public UUID getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(UUID applicationID) {
        this.applicationID = applicationID;
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

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public String getHrFeedback() {
        return hrFeedback;
    }

    public void setHrFeedback(String hrFeedback) {
        this.hrFeedback = hrFeedback;
    }
}