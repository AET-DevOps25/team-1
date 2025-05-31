package de.tum.devops.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for submitting new applications according to
 * api-documentation.yaml
 * 
 * Schema definition:
 * CreateApplicationRequest:
 * properties:
 * jobID: string (uuid)
 * resumeContent: string (required)
 * originalResumeFilename: string (optional)
 */
public class SubmitApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobID;

    @NotBlank(message = "Resume content is required")
    private String resumeContent;

    private String originalResumeFilename;

    // Constructors
    public SubmitApplicationRequest() {
    }

    public SubmitApplicationRequest(UUID jobID, String resumeContent, String originalResumeFilename) {
        this.jobID = jobID;
        this.resumeContent = resumeContent;
        this.originalResumeFilename = originalResumeFilename;
    }

    // Getters and Setters
    public UUID getJobID() {
        return jobID;
    }

    public void setJobID(UUID jobID) {
        this.jobID = jobID;
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
}