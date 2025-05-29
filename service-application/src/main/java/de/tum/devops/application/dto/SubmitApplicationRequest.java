package de.tum.devops.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for submitting new applications
 */
public class SubmitApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    private String coverLetter;

    // Constructors
    public SubmitApplicationRequest() {
    }

    public SubmitApplicationRequest(UUID jobId, String coverLetter) {
        this.jobId = jobId;
        this.coverLetter = coverLetter;
    }

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}