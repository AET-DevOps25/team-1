package de.tum.devops.application.service;

import de.tum.devops.application.client.AuthWebClient;
import de.tum.devops.application.client.JobWebClient;
import de.tum.devops.application.dto.*;
import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.enums.ApplicationStatus;
import de.tum.devops.application.persistence.enums.ChatStatus;
import de.tum.devops.application.persistence.enums.DecisionEnum;
import de.tum.devops.application.persistence.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final JobWebClient jobWebClient;
    private final AuthWebClient authWebClient;
    private final FileStorageService fileStorageService;
    private final DocumentTextExtractorService documentTextExtractorService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobWebClient jobWebClient,
                              AuthWebClient authWebClient,
                              FileStorageService fileStorageService,
                              DocumentTextExtractorService documentTextExtractorService) {
        this.applicationRepository = applicationRepository;
        this.jobWebClient = jobWebClient;
        this.authWebClient = authWebClient;
        this.fileStorageService = fileStorageService;
        this.documentTextExtractorService = documentTextExtractorService;
    }

    public Application getById(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    @Transactional
    public ApplicationDto submitApplication(SubmitApplicationRequest request, UUID candidateId, MultipartFile resumeFile) {
        logger.info("Submitting application for job {} by candidate {}", request.getJobId(), candidateId);

        // 1. Validate resume file is provided
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        // 2. Check if candidate has already applied for this job
        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobId())) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        // 3. Verify job exists and is open
        jobWebClient.fetchJob(request.getJobId())
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .blockOptional()
                .orElseThrow(() -> new IllegalArgumentException("Job is not open for applications"));

        // 4. Extract text from resume file
        String resumeText;
        try {
            resumeText = documentTextExtractorService.extractText(resumeFile);
            logger.info("Successfully extracted {} characters from resume file", resumeText.length());
        } catch (Exception e) {
            logger.error("Failed to extract text from resume file", e);
            throw new IllegalArgumentException("Failed to process resume file: " + e.getMessage());
        }

        // 5. Store resume file
        String filePath = fileStorageService.store(resumeFile, candidateId + "_" + request.getJobId());

        // 6. Create and save application
        Application application = new Application();
        application.setApplicationId(UUID.randomUUID());
        application.setJobId(request.getJobId());
        application.setCandidateId(candidateId);
        application.setResumeText(resumeText);
        application.setResumeFilePath(filePath);
        application.setStatus(ApplicationStatus.SUBMITTED);

        Application savedApplication = applicationRepository.save(application);
        logger.info("Application submitted successfully with ID: {}", savedApplication.getApplicationId());

        // 7. save application
        // Update status to AI_SCREENING
        savedApplication.setStatus(ApplicationStatus.AI_SCREENING);
        savedApplication = applicationRepository.save(savedApplication);

        ApplicationDto applicationDto = convertToDto(savedApplication);
        hideImportantFieldsForCandidate(applicationDto);
        return applicationDto;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplications(int page, int size, UUID jobId, ApplicationStatus status, String userRole, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applicationPage;

        if ("HR".equals(userRole)) {
            // HR can see all applications with optional filtering
            if (jobId != null && status != null) {
                applicationPage = applicationRepository.findByJobIdAndStatus(jobId, status, pageable);
            } else if (jobId != null) {
                applicationPage = applicationRepository.findByJobId(jobId, pageable);
            } else if (status != null) {
                applicationPage = applicationRepository.findByStatus(status, pageable);
            } else {
                applicationPage = applicationRepository.findAll(pageable);
            }
        } else {
            // Candidates can only see their own applications with optional filtering
            if (jobId != null && status != null) {
                applicationPage = applicationRepository.findByCandidateIdAndJobIdAndStatus(userId, jobId, status, pageable);
            } else if (jobId != null) {
                applicationPage = applicationRepository.findByCandidateIdAndJobId(userId, jobId, pageable);
            } else if (status != null) {
                applicationPage = applicationRepository.findByCandidateIdAndStatus(userId, status, pageable);
            } else {
                applicationPage = applicationRepository.findByCandidateId(userId, pageable);
            }
        }

        Page<ApplicationDto> dtoPage = applicationPage.map(this::convertToDto);

        if (userRole != null && userRole.equals("CANDIDATE")) {
            dtoPage.getContent().forEach(this::hideImportantFieldsForCandidate);
        }

        return dtoPage;
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplicationById(UUID applicationId, String userRole, UUID userId) {
        // Use the enhanced query to fetch application with all related entities
        Application application = applicationRepository.findByIdWithRelations(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Skip security check for internal calls
        if ("CANDIDATE".equals(userRole) && !application.getCandidateId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        ApplicationDto applicationDto = convertToDto(application);

        if ("CANDIDATE".equals(userRole)) {
            hideImportantFieldsForCandidate(applicationDto);
        }

        return applicationDto;
    }

    @Transactional
    public ApplicationDto updateApplication(UUID applicationId, DecisionEnum hrDecision, String hrComments, UUID hrUserId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        application.setHrDecision(hrDecision);
        application.setHrComments(hrComments);
        // Potentially update status based on decision
        if (hrDecision == DecisionEnum.HIRED) {
            application.setStatus(ApplicationStatus.HIRED);
        } else if (hrDecision == DecisionEnum.REJECTED) {
            application.setStatus(ApplicationStatus.REJECTED);
        }

        Application updatedApplication = applicationRepository.save(application);
        return convertToDto(updatedApplication);
    }

    private ApplicationDto convertToDto(Application application) {
        // Fetch related data if needed, e.g., user details
        UserDto candidate = authWebClient.fetchUser(application.getCandidateId()).block();
        JobDto job = jobWebClient.fetchJob(application.getJobId()).block();

        ChatStatus chatStatus = application.getChatSession() != null ? application.getChatSession().getStatus() : null;

        AssessmentDto assessment = new AssessmentDto(application.getAssessment());

        return new ApplicationDto(
                application.getApplicationId(),
                application.getJobId(),
                application.getCandidateId(),
                application.getStatus(),
                application.getResumeText(),
                application.getResumeFilePath(),
                application.getHrDecision(),
                application.getHrComments(),
                chatStatus,
                application.getSubmittedAt(),
                application.getUpdatedAt(),
                candidate, // assuming ApplicationDto is updated to hold UserDto
                job, // assuming ApplicationDto is updated to hold JobDto
                assessment
        );
    }

    private void hideImportantFieldsForCandidate(ApplicationDto applicationDto) {
        applicationDto.setResumeText(null);
        applicationDto.setResumeFilePath(null);
        applicationDto.getJob().getHrCreator().setUserID(null);
        applicationDto.setAssessment(null);
    }
}