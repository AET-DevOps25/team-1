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
 * resumeText: string (required)
 * resumeFile: binary (optional)
 */
public class SubmitApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @NotBlank(message = "Resume text is required")
    private String resumeText;

    // Constructors
    public SubmitApplicationRequest() {
    }

    public SubmitApplicationRequest(UUID jobId, String resumeText) {
        this.jobId = jobId;
        this.resumeText = resumeText;
    }

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
}