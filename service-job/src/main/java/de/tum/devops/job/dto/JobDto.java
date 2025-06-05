package de.tum.devops.job.dto;

import de.tum.devops.persistence.enums.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job data transfer object for API responses
 */
public class JobDto {

    private UUID jobID;
    private String title;
    private String description;
    private String requirements;
    private JobStatus status;
    private LocalDateTime creationTimestamp;
    private LocalDate closingDate;
    private LocalDateTime lastModifiedTimestamp;
    private UserDto hrCreator;

    // Constructors
    public JobDto() {
    }

    public JobDto(UUID jobID, String title, String description, String requirements,
            JobStatus status, LocalDateTime creationTimestamp, LocalDate closingDate,
            LocalDateTime lastModifiedTimestamp, UserDto hrCreator) {
        this.jobID = jobID;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.status = status;
        this.creationTimestamp = creationTimestamp;
        this.closingDate = closingDate;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        this.hrCreator = hrCreator;
    }

    // Getters and Setters
    public UUID getJobID() {
        return jobID;
    }

    public void setJobID(UUID jobID) {
        this.jobID = jobID;
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

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public LocalDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public UserDto getHrCreator() {
        return hrCreator;
    }

    public void setHrCreator(UserDto hrCreator) {
        this.hrCreator = hrCreator;
    }
}