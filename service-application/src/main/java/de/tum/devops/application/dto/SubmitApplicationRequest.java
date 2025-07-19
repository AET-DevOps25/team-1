package de.tum.devops.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for submitting new applications according to
 * api-openapi-original-design.yaml
 * <p>
 * Schema definition:
 * CreateApplicationRequest:
 * properties:
 * jobId: string (uuid)
 * resumeFile: binary (required)
 */
public class SubmitApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    // Constructors
    public SubmitApplicationRequest() {
    }

    public SubmitApplicationRequest(UUID jobId) {
        this.jobId = jobId;
    }

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
}