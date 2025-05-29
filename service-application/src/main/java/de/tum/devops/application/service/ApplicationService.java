package de.tum.devops.application.service;

import de.tum.devops.application.dto.*;
import de.tum.devops.persistence.entity.Application;
import de.tum.devops.persistence.entity.ApplicationStatus;
import de.tum.devops.persistence.entity.Job;
import de.tum.devops.persistence.entity.User;
import de.tum.devops.persistence.repository.ApplicationRepository;
import de.tum.devops.persistence.repository.JobRepository;
import de.tum.devops.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer for application management operations
 */
@Service
@Transactional
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Submit new application (Candidates only)
     */
    public ApplicationDto submitApplication(SubmitApplicationRequest request, UUID candidateId) {
        logger.info("Submitting application for job: {} by candidate: {}", request.getJobID(), candidateId);

        // Check if job exists and is open
        if (!jobRepository.existsByJobIdAndStatus(request.getJobID(), de.tum.devops.persistence.entity.JobStatus.OPEN)) {
            throw new IllegalArgumentException("Job is not available for applications");
        }

        // Check for duplicate application
        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobID())) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        // Create application with resume content
        Application application = new Application(
                candidateId,
                request.getJobID(),
                request.getResumeContent(),
                request.getOriginalResumeFilename());

        application = applicationRepository.save(application);
        logger.info("Application submitted successfully: {}", application.getApplicationId());

        return convertToDto(application);
    }

    /**
     * Get applications for candidate (own applications)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCandidateApplications(UUID candidateId, int page, int size) {
        logger.info("Getting applications for candidate: {}", candidateId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applicationPage = applicationRepository.findByCandidateId(candidateId, pageable);

        Page<ApplicationDto> applicationDtoPage = applicationPage.map(this::convertToDto);

        return Map.of(
                "content", applicationDtoPage.getContent(),
                "pageInfo", new PageInfo(
                        applicationDtoPage.getNumber(),
                        applicationDtoPage.getSize(),
                        applicationDtoPage.getTotalElements(),
                        applicationDtoPage.getTotalPages()));
    }

    /**
     * Get applications for HR review (all applications or filtered)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getApplicationsForReview(ApplicationStatus status, int page, int size) {
        logger.info("Getting applications for HR review with status: {}", status);

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applicationPage;

        if (status != null) {
            applicationPage = applicationRepository.findByStatus(status, pageable);
        } else {
            applicationPage = applicationRepository.findAll(pageable);
        }

        Page<ApplicationDto> applicationDtoPage = applicationPage.map(this::convertToDto);

        return Map.of(
                "content", applicationDtoPage.getContent(),
                "pageInfo", new PageInfo(
                        applicationDtoPage.getNumber(),
                        applicationDtoPage.getSize(),
                        applicationDtoPage.getTotalElements(),
                        applicationDtoPage.getTotalPages()));
    }

    /**
     * Get application details by ID
     */
    @Transactional(readOnly = true)
    public ApplicationDto getApplicationById(UUID applicationId, UUID userId, String userRole) {
        logger.info("Getting application details: {} for user: {} with role: {}", applicationId, userId, userRole);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Role-based access control
        if ("CANDIDATE".equals(userRole) && !application.getCandidateId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: You can only view your own applications");
        }

        return convertToDto(application);
    }

    /**
     * Update application status (HR only)
     */
    public ApplicationDto updateApplicationStatus(UUID applicationId,
            ApplicationStatus newStatus,
            String hrComments,
            UUID hrUserId) {
        logger.info("Updating application status: {} to {} by HR: {}", applicationId, newStatus, hrUserId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        application.setStatus(newStatus);
        if (hrComments != null) {
            application.setHrComments(hrComments);
        }
        application.setLastModifiedTimestamp(LocalDateTime.now());

        application = applicationRepository.save(application);

        logger.info("Application status updated successfully: {}", applicationId);
        return convertToDto(application);
    }

    /**
     * Delete/withdraw application (Candidates only, if pending)
     */
    public void withdrawApplication(UUID applicationId, UUID candidateId) {
        logger.info("Withdrawing application: {} by candidate: {}", applicationId, candidateId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Verify ownership
        if (!application.getCandidateId().equals(candidateId)) {
            throw new IllegalArgumentException("You can only withdraw your own applications");
        }

        // Only allow withdrawal if submitted (pending review)
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new IllegalArgumentException("You can only withdraw submitted applications");
        }

        applicationRepository.delete(application);
        logger.info("Application withdrawn successfully: {}", applicationId);
    }

    /**
     * Convert Application entity to ApplicationDto
     */
    private ApplicationDto convertToDto(Application application) {
        // Get user and job information
        User user = userRepository.findById(application.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // Convert User to UserDto
        UserDto candidate = new UserDto(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().toString(),
                user.getCreationTimestamp()
        );

        // Get HR creator information for JobDto
        User hrCreator = userRepository.findById(job.getHrCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("HR creator not found"));

        // Convert HR creator to UserDto
        UserDto hrCreatorDto = new UserDto(
                hrCreator.getUserId(),
                hrCreator.getFullName(),
                hrCreator.getEmail(),
                hrCreator.getRole().toString(),
                hrCreator.getCreationTimestamp()
        );

        // Convert Job to JobDto
        JobDto jobDto = new JobDto(
                job.getJobId(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getStatus().toString(),
                job.getCreationTimestamp(),
                job.getClosingDate(),
                job.getLastModifiedTimestamp(),
                hrCreatorDto
        );

        return new ApplicationDto(
                application.getApplicationId(),
                application.getSubmissionTimestamp(),
                application.getStatus(),
                application.getResumeContent(),
                application.getOriginalResumeFilename(),
                application.getLastModifiedTimestamp(),
                candidate,
                jobDto,
                null // assessment will be populated separately if needed
        );
    }
}
