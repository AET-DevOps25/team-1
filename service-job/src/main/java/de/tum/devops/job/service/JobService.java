package de.tum.devops.job.service;

import de.tum.devops.job.dto.*;
import de.tum.devops.job.entity.Job;
import de.tum.devops.job.entity.JobStatus;
import de.tum.devops.job.repository.JobRepository;
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
@Transactional
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final UserService userService;

    public JobService(JobRepository jobRepository, UserService userService) {
        this.jobRepository = jobRepository;
        this.userService = userService;
    }

    /**
     * Get paginated job list with filtering based on user role
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getJobs(int page, int size, JobStatus status, String userRole) {
        logger.info("Getting jobs - page: {}, size: {}, status: {}, userRole: {}", page, size, status, userRole);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Job> jobPage;

        if (status != null) {
            // Filter by specific status
            jobPage = jobRepository.findByStatus(status, pageable);
        } else {
            // Role-based filtering
            if ("HR".equals(userRole)) {
                // HR can see all jobs
                jobPage = jobRepository.findAll(pageable);
            } else {
                // Candidates can only see OPEN jobs
                JobStatus[] candidateVisibleStatuses = { JobStatus.OPEN };
                jobPage = jobRepository.findByStatusIn(candidateVisibleStatuses, pageable);
            }
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
    public JobDto createJob(CreateJobRequest request, UUID hrCreatorId) {
        logger.info("Creating job: {} by HR: {}", request.getTitle(), hrCreatorId);

        Job job = new Job(
                request.getTitle(),
                request.getDescription(),
                request.getRequirements(),
                hrCreatorId);

        if (request.getClosingDate() != null) {
            job.setClosingDate(request.getClosingDate());
        }

        job = jobRepository.save(job);
        logger.info("Job created successfully: {}", job.getJobId());

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
        if (request.getClosingDate() != null) {
            job.setClosingDate(request.getClosingDate());
        }
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        job.setLastModifiedTimestamp(LocalDateTime.now());
        job = jobRepository.save(job);

        logger.info("Job updated successfully: {}", jobId);
        return convertToDto(job);
    }

    /**
     * Close job (HR only)
     */
    public JobDto closeJob(UUID jobId, UUID hrUserId) {
        logger.info("Closing job: {} by HR: {}", jobId, hrUserId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("Job is already closed");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setLastModifiedTimestamp(LocalDateTime.now());
        job = jobRepository.save(job);

        logger.info("Job closed successfully: {}", jobId);
        return convertToDto(job);
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
        // Get HR creator information
        UserDto hrCreator = userService.getUserById(job.getHrCreatorId());

        return new JobDto(
                job.getJobId(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getStatus(),
                job.getCreationTimestamp(),
                job.getClosingDate(),
                job.getLastModifiedTimestamp(),
                hrCreator);
    }
}