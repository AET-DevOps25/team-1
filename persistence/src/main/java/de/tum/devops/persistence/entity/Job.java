package de.tum.devops.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job entity mapping to jobs table
 * 
 * Database schema:
 * CREATE TABLE jobs (
 * job_id UUID PRIMARY KEY,
 * title VARCHAR(255) NOT NULL,
 * description TEXT NOT NULL,
 * requirements TEXT NOT NULL,
 * status job_status NOT NULL DEFAULT 'DRAFT',
 * creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 * closing_date DATE,
 * last_modified_timestamp TIMESTAMP,
 * hr_creator_id UUID REFERENCES users(user_id) NOT NULL
 * );
 */
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_jobs_hr_creator_id", columnList = "hr_creator_id")
})
public class Job {

    @Id
    @Column(name = "job_id", columnDefinition = "UUID")
    private UUID jobId;

    @NotBlank
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(name = "requirements", nullable = false, columnDefinition = "TEXT")
    private String requirements;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "job_status")
    private JobStatus status = JobStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "creation_timestamp", updatable = false)
    private LocalDateTime creationTimestamp;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @UpdateTimestamp
    @Column(name = "last_modified_timestamp")
    private LocalDateTime lastModifiedTimestamp;

    @NotNull
    @Column(name = "hr_creator_id", nullable = false, columnDefinition = "UUID")
    private UUID hrCreatorId;

    // Constructors
    public Job() {
        this.jobId = UUID.randomUUID();
    }

    public Job(String title, String description, String requirements, UUID hrCreatorId) {
        this();
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.hrCreatorId = hrCreatorId;
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

    public UUID getHrCreatorId() {
        return hrCreatorId;
    }

    public void setHrCreatorId(UUID hrCreatorId) {
        this.hrCreatorId = hrCreatorId;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId=" + jobId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", hrCreatorId=" + hrCreatorId +
                '}';
    }
}