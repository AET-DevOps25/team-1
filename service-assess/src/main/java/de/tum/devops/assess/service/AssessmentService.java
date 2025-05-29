package de.tum.devops.assess.service;

import de.tum.devops.assess.dto.*;
import de.tum.devops.assess.entity.Assessment;
import de.tum.devops.assess.entity.AssessmentStatus;
import de.tum.devops.assess.repository.AssessmentRepository;
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
 * Service layer for assessment management operations
 */
@Service
@Transactional
public class AssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentRepository assessmentRepository;
    private final ApplicationService applicationService;
    private final UserService userService;

    public AssessmentService(AssessmentRepository assessmentRepository,
            ApplicationService applicationService,
            UserService userService) {
        this.assessmentRepository = assessmentRepository;
        this.applicationService = applicationService;
        this.userService = userService;
    }

    /**
     * Create new assessment (HR only)
     */
    public AssessmentDto createAssessment(CreateAssessmentRequest request, UUID hrCreatorId) {
        logger.info("Creating assessment for application: {} by HR: {}", request.getApplicationId(), hrCreatorId);

        // Validate application exists
        if (!applicationService.applicationExists(request.getApplicationId())) {
            throw new IllegalArgumentException("Application not found");
        }

        // Create assessment
        Assessment assessment = new Assessment(
                request.getApplicationId(),
                request.getAssessmentType(),
                hrCreatorId);

        if (request.getMaxScore() != null) {
            assessment.setMaxScore(request.getMaxScore());
        }

        if (request.getAssessmentData() != null) {
            assessment.setAssessmentData(request.getAssessmentData());
        }

        assessment = assessmentRepository.save(assessment);
        logger.info("Assessment created successfully: {}", assessment.getAssessmentId());

        return convertToDto(assessment);
    }

    /**
     * Get assessments with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAssessments(int page, int size, AssessmentStatus status, String userRole,
            UUID userId) {
        logger.info("Getting assessments - page: {}, size: {}, status: {}, userRole: {}", page, size, status, userRole);

        Pageable pageable = PageRequest.of(page, size);
        Page<Assessment> assessmentPage;

        if (status != null) {
            assessmentPage = assessmentRepository.findByStatus(status, pageable);
        } else if ("HR".equals(userRole)) {
            // HR can see all assessments
            assessmentPage = assessmentRepository.findAll(pageable);
        } else {
            // Candidates can only see their own assessments
            assessmentPage = assessmentRepository.findByApplicationId(userId, pageable); // This would need proper
                                                                                         // candidate filtering
        }

        Page<AssessmentDto> assessmentDtoPage = assessmentPage.map(this::convertToDto);

        return Map.of(
                "content", assessmentDtoPage.getContent(),
                "pageInfo", new PageInfo(
                        assessmentDtoPage.getNumber(),
                        assessmentDtoPage.getSize(),
                        assessmentDtoPage.getTotalElements(),
                        assessmentDtoPage.getTotalPages()));
    }

    /**
     * Get assessment details by ID
     */
    @Transactional(readOnly = true)
    public AssessmentDto getAssessmentById(UUID assessmentId, String userRole, UUID userId) {
        logger.info("Getting assessment details: {} for user: {} with role: {}", assessmentId, userId, userRole);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        // Role-based access control would need proper implementation
        // For now, allow all authenticated users to view assessments

        return convertToDto(assessment);
    }

    /**
     * Start assessment (Candidates only)
     */
    public AssessmentDto startAssessment(UUID assessmentId, UUID candidateId) {
        logger.info("Starting assessment: {} by candidate: {}", assessmentId, candidateId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        if (assessment.getStatus() != AssessmentStatus.PENDING) {
            throw new IllegalArgumentException("Assessment is not in pending status");
        }

        assessment.setStatus(AssessmentStatus.IN_PROGRESS);
        assessment.setStartTime(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment started successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Submit assessment results (Candidates only)
     */
    public AssessmentDto submitAssessment(UUID assessmentId, String assessmentData, UUID candidateId) {
        logger.info("Submitting assessment: {} by candidate: {}", assessmentId, candidateId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        if (assessment.getStatus() != AssessmentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Assessment is not in progress");
        }

        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setEndTime(LocalDateTime.now());
        assessment.setAssessmentData(assessmentData);
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment submitted successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Score assessment (HR only)
     */
    public AssessmentDto scoreAssessment(UUID assessmentId, Integer score, String feedback, UUID hrUserId) {
        logger.info("Scoring assessment: {} by HR: {}", assessmentId, hrUserId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        if (assessment.getStatus() != AssessmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Assessment must be completed before scoring");
        }

        assessment.setScore(score);
        assessment.setFeedback(feedback);
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment scored successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Convert Assessment entity to AssessmentDto
     */
    private AssessmentDto convertToDto(Assessment assessment) {
        // Get application information
        ApplicationDto application = applicationService.getApplicationById(assessment.getApplicationId());

        // Get HR creator information
        UserDto hrCreator = userService.getUserById(assessment.getHrCreatorId());

        return new AssessmentDto(
                assessment.getAssessmentId(),
                application,
                assessment.getAssessmentType(),
                assessment.getStatus(),
                assessment.getScore(),
                assessment.getMaxScore(),
                assessment.getFeedback(),
                assessment.getAssessmentData(),
                assessment.getCreationTimestamp(),
                assessment.getStartTime(),
                assessment.getEndTime(),
                assessment.getLastModifiedTimestamp(),
                hrCreator);
    }
}