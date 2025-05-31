package de.tum.devops.job.dto;

import de.tum.devops.persistence.entity.JobStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for updating existing jobs
 */
public class UpdateJobRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private String requirements;

    private LocalDate closingDate;

    private JobStatus status;

    // Constructors
    public UpdateJobRequest() {
    }

    public UpdateJobRequest(String title, String description, String requirements,
            LocalDate closingDate, JobStatus status) {
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.closingDate = closingDate;
        this.status = status;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}