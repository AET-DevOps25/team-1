package de.tum.devops.application.service;

import de.tum.devops.application.dto.*;
import de.tum.devops.application.entity.Application;
import de.tum.devops.application.entity.ApplicationStatus;
import de.tum.devops.application.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UserService userService;
    private final JobService jobService;
    private final FileStorageService fileStorageService;

    public ApplicationService(ApplicationRepository applicationRepository,
            UserService userService,
            JobService jobService,
            FileStorageService fileStorageService) {
        this.applicationRepository = applicationRepository;
        this.userService = userService;
        this.jobService = jobService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Submit new application (Candidates only)
     */
    public ApplicationDto submitApplication(SubmitApplicationRequest request,
            MultipartFile resume,
            UUID candidateId) {
        logger.info("Submitting application for job: {} by candidate: {}", request.getJobId(), candidateId);

        // Check if job exists and is open
        if (!jobService.isJobOpenForApplications(request.getJobId())) {
            throw new IllegalArgumentException("Job is not available for applications");
        }

        // Check for duplicate application
        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobId())) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        // Store resume file
        String resumePath = null;
        if (resume != null && !resume.isEmpty()) {
            resumePath = fileStorageService.storeFile(resume, candidateId);
        }

        // Create application
        Application application = new Application(
                candidateId,
                request.getJobId(),
                resumePath,
                request.getCoverLetter());

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
            String hrFeedback,
            UUID hrUserId) {
        logger.info("Updating application status: {} to {} by HR: {}", applicationId, newStatus, hrUserId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        application.setStatus(newStatus);
        if (hrFeedback != null) {
            application.setHrFeedback(hrFeedback);
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

        // Only allow withdrawal if pending
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException("You can only withdraw pending applications");
        }

        // Delete resume file if exists
        if (application.getResumePath() != null) {
            fileStorageService.deleteFile(application.getResumePath());
        }

        applicationRepository.delete(application);
        logger.info("Application withdrawn successfully: {}", applicationId);
    }

    /**
     * Convert Application entity to ApplicationDto
     */
    private ApplicationDto convertToDto(Application application) {
        // Get candidate information
        UserDto candidate = userService.getUserById(application.getCandidateId());

        // Get job information
        JobDto job = jobService.getJobById(application.getJobId());

        return new ApplicationDto(
                application.getApplicationId(),
                candidate,
                job,
                application.getStatus(),
                application.getResumePath(),
                application.getCoverLetter(),
                application.getSubmissionTimestamp(),
                application.getLastModifiedTimestamp(),
                application.getHrFeedback());
    }
}