package de.tum.devops.job.dto;

import de.tum.devops.job.persistence.enums.JobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job data transfer object for API responses
 */
public class JobDto {

    private UUID jobId;
    private String title;
    private String description;
    private String requirements;
    private JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto hrCreator;

    // Constructors
    public JobDto() {
    }

    public JobDto(UUID jobId, String title, String description, String requirements,
                  JobStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, UserDto hrCreator) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hrCreator = hrCreator;
    }

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
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

    public UserDto getHrCreator() {
        return hrCreator;
    }

    public void setHrCreator(UserDto hrCreator) {
        this.hrCreator = hrCreator;
    }
}