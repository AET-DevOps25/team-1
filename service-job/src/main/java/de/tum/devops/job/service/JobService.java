package de.tum.devops.job.service;

import de.tum.devops.job.client.AuthWebClient;
import de.tum.devops.job.dto.*;
import de.tum.devops.job.persistence.entity.Job;
import de.tum.devops.job.persistence.enums.JobStatus;
import de.tum.devops.job.persistence.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer for job management operations
 */
@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final AuthWebClient authWebClient;

    public JobService(JobRepository jobRepository, AuthWebClient authWebClient) {
        this.jobRepository = jobRepository;
        this.authWebClient = authWebClient;
    }

    /**
     * Get paginated job list with filtering based on user role
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getJobs(int page, int size, JobStatus status, String userRole) {
        logger.info("Getting jobs - page: {}, size: {}, status: {}, userRole: {}", page, size, status, userRole);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Job> jobPage;

        // Role-based filtering
        if ("HR".equals(userRole)) {
            // HR can see all jobs
            if (status != null) {
                // Filter by specific status
                jobPage = jobRepository.findByStatus(status, pageable);
            } else {
                jobPage = jobRepository.findAll(pageable);
            }
        } else {
            // Candidates can only see OPEN jobs
            JobStatus[] candidateVisibleStatuses = {JobStatus.OPEN};
            jobPage = jobRepository.findByStatusIn(candidateVisibleStatuses, pageable);
        }

        Page<JobDto> jobDtoPage = jobPage.map(this::convertToDto);

        return Map.of(
                "content", jobDtoPage.getContent(),
                "pageInfo", new PageInfo(
                        jobDtoPage.getNumber(),
                        jobDtoPage.getSize(),
                        jobDtoPage.getTotalElements(),
                        jobDtoPage.getTotalPages()));
    }

    /**
     * Create new job (HR only)
     */
    @Transactional
    public JobDto createJob(CreateJobRequest request, UUID hrCreatorId) {
        logger.info("Creating job: {} by HR: {}", request.getTitle(), hrCreatorId);

        Job job = new Job(
                request.getTitle(),
                request.getDescription(),
                request.getRequirements(),
                hrCreatorId);

        job = jobRepository.saveAndFlush(job);
        logger.info("Job created successfully: {}", job.getJobId());
        logger.info("Job details: {}", job);
        logger.info("Job created at: {}", job.getCreatedAt());

        return convertToDto(job);
    }

    /**
     * Get job details by ID
     */
    @Transactional(readOnly = true)
    public JobDto getJobById(UUID jobId, String userRole) {
        logger.info("Getting job details: {} for user role: {}", jobId, userRole);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // Role-based access control
        if ("CANDIDATE".equals(userRole) && job.getStatus() != JobStatus.OPEN) {
            throw new IllegalArgumentException("Job not accessible");
        }

        return convertToDto(job);
    }

    /**
     * Update job (HR only)
     */
    @Transactional
    public JobDto updateJob(UUID jobId, UpdateJobRequest request, UUID hrUserId) {
        logger.info("Updating job: {} by HR: {}", jobId, hrUserId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // Update fields if provided
        if (request.getTitle() != null) {
            job.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            job.setDescription(request.getDescription());
        }
        if (request.getRequirements() != null) {
            job.setRequirements(request.getRequirements());
        }
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        job.setUpdatedAt(LocalDateTime.now());
        job = jobRepository.save(job);

        logger.info("Job updated successfully: {}", jobId);
        return convertToDto(job);
    }

    /**
     * Close job (HR only)
     */
    @Transactional
    public JobDto closeJob(UUID jobId, UUID hrUserId) {
        logger.info("Closing job: {} by HR: {}", jobId, hrUserId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("Job is already closed");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setUpdatedAt(LocalDateTime.now());
        job = jobRepository.save(job);

        logger.info("Job closed successfully: {}", jobId);
        return convertToDto(job);
    }

    /**
     * Re-open job (HR only)
     */
    @Transactional
    public JobDto reopenJob(UUID jobId, UUID hrUserId) {
        logger.info("Re-opening job: {} by HR: {}", jobId, hrUserId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getStatus() == JobStatus.OPEN) {
            throw new IllegalArgumentException("Job is already open");
        }

        job.setStatus(JobStatus.OPEN);
        job.setUpdatedAt(LocalDateTime.now());
        job = jobRepository.save(job);

        logger.info("Job reopened successfully: {}", jobId);
        return convertToDto(job);
    }

    /**
     * Delete job (HR only)
     */
    @Transactional
    public void deleteJob(UUID jobId, UUID hrUserId) {
        logger.info("Deleting job: {} by HR: {}", jobId, hrUserId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        jobRepository.delete(job);
        logger.info("Job deleted: {}", jobId);
    }

    /**
     * Check if job exists and is open (for application validation)
     */
    @Transactional(readOnly = true)
    public boolean isJobOpenForApplications(UUID jobId) {
        return jobRepository.existsByJobIdAndStatus(jobId, JobStatus.OPEN);
    }

    /**
     * Convert Job entity to JobDto
     */
    private JobDto convertToDto(Job job) {
        // Synchronously fetch HR creator info via WebClient
        UserDto hrCreatorDto = authWebClient.fetchUser(job.getHrCreatorId())
                .blockOptional()
                .orElse(null);

        return new JobDto(
                job.getJobId(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                hrCreatorDto);
    }
}