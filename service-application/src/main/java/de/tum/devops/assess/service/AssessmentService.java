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
 * Service layer for assessment management operations (merged into
 * service-application)
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

    // --- Core business methods copied from original service-assess ---

    public AssessmentDto createAssessmentForApplication(UUID applicationId) {
        logger.info("Creating assessment for application: {}", applicationId);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (assessmentRepository.existsByApplicationId(applicationId)) {
            throw new IllegalArgumentException("Assessment already exists for this application");
        }
        Assessment assessment = new Assessment(applicationId);
        assessment = assessmentRepository.save(assessment);
        return convertToDto(assessment);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssessments(int page, int size, RecommendationEnum recommendation,
            String userRole, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Assessment> assessmentPage;
        if (recommendation != null) {
            assessmentPage = assessmentRepository.findByRecommendation(recommendation, pageable);
        } else if ("CANDIDATE".equals(userRole)) {
            assessmentPage = assessmentRepository.findByApplicationCandidateId(userId, pageable);
        } else {
            assessmentPage = assessmentRepository.findAll(pageable);
        }
        Page<AssessmentDto> dtoPage = assessmentPage.map(this::convertToDto);
        return Map.of(
                "content", dtoPage.getContent(),
                "pageInfo", new PageInfo(dtoPage.getNumber(), dtoPage.getSize(), dtoPage.getTotalElements(),
                        dtoPage.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public AssessmentDto getAssessmentById(UUID assessmentId, String userRole, UUID userId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        if ("CANDIDATE".equals(userRole)) {
            Application application = applicationRepository.findById(assessment.getApplicationId())
                    .orElseThrow(() -> new IllegalArgumentException("Application not found"));
            if (!application.getCandidateId().equals(userId)) {
                throw new IllegalArgumentException("Access denied");
            }
        }
        return convertToDto(assessment);
    }

    public AssessmentDto updateAssessmentScore(UUID assessmentId, Float resumeScore, Float interviewScore,
            Float finalScore, String resumeAnalysis, String interviewSummary,
            RecommendationEnum recommendation) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        if (resumeScore != null)
            assessment.setResumeScore(resumeScore);
        if (interviewScore != null)
            assessment.setInterviewScore(interviewScore);
        if (finalScore != null)
            assessment.setFinalScore(finalScore);
        if (resumeAnalysis != null)
            assessment.setResumeAnalysis(resumeAnalysis);
        if (interviewSummary != null)
            assessment.setInterviewSummary(interviewSummary);
        if (recommendation != null)
            assessment.setRecommendation(recommendation);
        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);
        return convertToDto(assessment);
    }

    public AssessmentDto startAssessment(UUID assessmentId, UUID candidateId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        Application application = applicationRepository.findById(assessment.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (!application.getCandidateId().equals(candidateId)) {
            throw new IllegalArgumentException("Access denied");
        }
        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);
        return convertToDto(assessment);
    }

    public AssessmentDto submitAssessment(UUID assessmentId, String assessmentData, UUID candidateId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        Application application = applicationRepository.findById(assessment.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (!application.getCandidateId().equals(candidateId)) {
            throw new IllegalArgumentException("Access denied");
        }
        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);
        return convertToDto(assessment);
    }

    public AssessmentDto scoreAssessment(UUID assessmentId, Integer score, String feedback, UUID hrUserId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        if (score != null) {
            assessment.setFinalScore(score.floatValue());
        }
        if (feedback != null) {
            assessment.setInterviewSummary(feedback);
        }
        assessment.setLastModifiedTimestamp(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);
        return convertToDto(assessment);
    }

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