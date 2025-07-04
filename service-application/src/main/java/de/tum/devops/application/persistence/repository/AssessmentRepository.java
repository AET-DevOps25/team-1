package de.tum.devops.application.persistence.repository;

import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.entity.Assessment;
import de.tum.devops.application.persistence.enums.RecommendationEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Assessment entity
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    
    /**
     * Find assessment by application ID
     */
    Optional<Assessment> findByApplicationApplicationId(UUID applicationId);
    
    /**
     * Find assessment by application
     */
    Optional<Assessment> findByApplication(Application application);
    
    /**
     * Find assessments by recommendation
     */
    List<Assessment> findByRecommendation(RecommendationEnum recommendation);
    
    /**
     * Find assessments with resume score above threshold
     */
    List<Assessment> findByResumeScoreGreaterThanEqual(Float minScore);
    
    /**
     * Find assessments with interview score above threshold
     */
    List<Assessment> findByInterviewScoreGreaterThanEqual(Float minScore);
    
    /**
     * Find assessments with both scores above thresholds
     */
    @Query("SELECT a FROM Assessment a WHERE a.resumeScore >= :minResumeScore AND a.interviewScore >= :minInterviewScore")
    List<Assessment> findByScoresAboveThreshold(@Param("minResumeScore") Float minResumeScore, 
                                               @Param("minInterviewScore") Float minInterviewScore);
    
    /**
     * Check if assessment exists for application
     */
    boolean existsByApplicationApplicationId(UUID applicationId);
    
    /**
     * Find assessments ordered by overall performance (average of scores)
     */
    @Query("SELECT a FROM Assessment a WHERE a.resumeScore IS NOT NULL AND a.interviewScore IS NOT NULL ORDER BY (a.resumeScore + a.interviewScore) DESC")
    List<Assessment> findAllOrderByOverallScoreDesc();
    
    /**
     * Count assessments by recommendation
     */
    long countByRecommendation(RecommendationEnum recommendation);
    
    /**
     * Find top performing assessments (for analytics)
     */
    @Query("SELECT a FROM Assessment a WHERE a.resumeScore IS NOT NULL AND a.interviewScore IS NOT NULL AND (a.resumeScore + a.interviewScore) / 2 >= :threshold ORDER BY (a.resumeScore + a.interviewScore) DESC")
    List<Assessment> findTopPerformers(@Param("threshold") Float threshold);
}