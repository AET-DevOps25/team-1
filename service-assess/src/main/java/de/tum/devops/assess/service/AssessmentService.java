package de.tum.devops.assess.service;

import de.tum.devops.assess.dto.AssessmentDto;
import de.tum.devops.assess.dto.PageInfo;
import de.tum.devops.persistence.entity.Application;
import de.tum.devops.persistence.entity.Assessment;
import de.tum.devops.persistence.enums.RecommendationEnum;
import de.tum.devops.persistence.repository.ApplicationRepository;
import de.tum.devops.persistence.repository.AssessmentRepository;
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
    private final ApplicationRepository applicationRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             ApplicationRepository applicationRepository) {
        this.assessmentRepository = assessmentRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Create assessment for application
     */
    public AssessmentDto createAssessmentForApplication(UUID applicationId) {
        logger.info("Creating assessment for application: {}", applicationId);

        // Verify application exists
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Check if assessment already exists
        if (assessmentRepository.existsByApplicationId(applicationId)) {
            throw new IllegalArgumentException("Assessment already exists for this application");
        }

        Assessment assessment = new Assessment(applicationId);
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment created successfully: {}", assessment.getAssessmentId());
        return convertToDto(assessment);
    }

    /**
     * Get assessments with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAssessments(int page, int size, RecommendationEnum recommendation,
                                              String userRole, UUID userId) {
        logger.info("Getting assessments - page: {}, size: {}, recommendation: {}, userRole: {}",
                page, size, recommendation, userRole);

        Pageable pageable = PageRequest.of(page, size);
        Page<Assessment> assessmentPage;

        if (recommendation != null) {
            assessmentPage = assessmentRepository.findByRecommendation(recommendation, pageable);
        } else if ("CANDIDATE".equals(userRole)) {
            // Candidates can only see their own assessments
            assessmentPage = assessmentRepository.findByApplicationCandidateId(userId, pageable);
        } else {
            // HR can see all assessments
            assessmentPage = assessmentRepository.findAll(pageable);
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
     * Get assessment by ID
     */
    @Transactional(readOnly = true)
    public AssessmentDto getAssessmentById(UUID assessmentId, String userRole, UUID userId) {
        logger.info("Getting assessment: {} for user role: {}", assessmentId, userRole);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        // Role-based access control
        if ("CANDIDATE".equals(userRole)) {
            // Verify candidate owns this assessment through application
            Application application = applicationRepository.findById(assessment.getApplicationId())
                    .orElseThrow(() -> new IllegalArgumentException("Application not found"));

            if (!application.getCandidateId().equals(userId)) {
                throw new IllegalArgumentException("Access denied");
            }
        }

        return convertToDto(assessment);
    }

    /**
     * Update assessment scores and analysis
     */
    public AssessmentDto updateAssessmentScore(UUID assessmentId, Float resumeScore, Float interviewScore,
                                               Float finalScore, String resumeAnalysis, String interviewSummary,
                                               RecommendationEnum recommendation) {
        logger.info("Updating assessment scores: {}", assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        // Update fields if provided
        if (resumeScore != null) {
            assessment.setResumeScore(resumeScore);
        }
        if (interviewScore != null) {
            assessment.setInterviewScore(interviewScore);
        }
        if (finalScore != null) {
            assessment.setFinalScore(finalScore);
        }
        if (resumeAnalysis != null) {
            assessment.setResumeAnalysis(resumeAnalysis);
        }
        if (interviewSummary != null) {
            assessment.setInterviewSummary(interviewSummary);
        }
        if (recommendation != null) {
            assessment.setRecommendation(recommendation);
        }

        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment updated successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Start assessment process (for candidates)
     */
    public AssessmentDto startAssessment(UUID assessmentId, UUID candidateId) {
        logger.info("Starting assessment: {} for candidate: {}", assessmentId, candidateId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        // Verify candidate ownership
        Application application = applicationRepository.findById(assessment.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getCandidateId().equals(candidateId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Mark assessment as started (you might want to add a status field)
        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment started successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Submit assessment data (for candidates)
     */
    public AssessmentDto submitAssessment(UUID assessmentId, String assessmentData, UUID candidateId) {
        logger.info("Submitting assessment: {} for candidate: {}", assessmentId, candidateId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        // Verify candidate ownership
        Application application = applicationRepository.findById(assessment.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getCandidateId().equals(candidateId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Store assessment data (you might want to add fields for this)
        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment submitted successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Score assessment (for HR)
     */
    public AssessmentDto scoreAssessment(UUID assessmentId, Integer score, String feedback, UUID hrUserId) {
        logger.info("Scoring assessment: {} by HR: {}", assessmentId, hrUserId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));

        // Convert integer score to float and set as final score
        if (score != null) {
            assessment.setFinalScore(score.floatValue());
        }

        if (feedback != null) {
            assessment.setInterviewSummary(feedback);
        }

        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);

        logger.info("Assessment scored successfully: {}", assessmentId);
        return convertToDto(assessment);
    }

    /**
     * Convert Assessment entity to AssessmentDto
     */
    private AssessmentDto convertToDto(Assessment assessment) {
        return new AssessmentDto(
                assessment.getAssessmentId(),
                assessment.getApplicationId(),
                assessment.getResumeScore(),
                assessment.getInterviewScore(),
                assessment.getFinalScore(),
                assessment.getResumeAnalysis(),
                assessment.getInterviewSummary(),
                assessment.getRecommendation(),
                assessment.getCreationTimestamp(),
                assessment.getLastModifiedTimestamp());
    }
}