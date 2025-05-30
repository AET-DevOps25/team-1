package de.tum.devops.persistence.repository;

import de.tum.devops.persistence.entity.Assessment;
import de.tum.devops.persistence.entity.RecommendationEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Assessment entity
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    /**
     * Find assessments by application ID
     */
    Page<Assessment> findByApplicationId(UUID applicationId, Pageable pageable);

    /**
     * Find assessments by recommendation with pagination
     */
    Page<Assessment> findByRecommendation(RecommendationEnum recommendation, Pageable pageable);

    /**
     * Find assessments by candidate ID through application relationship
     */
    @Query("SELECT a FROM Assessment a JOIN Application app ON a.applicationId = app.applicationId WHERE app.candidateId = :candidateId")
    Page<Assessment> findByApplicationCandidateId(@Param("candidateId") UUID candidateId, Pageable pageable);

    /**
     * Count assessments by recommendation
     */
    long countByRecommendation(RecommendationEnum recommendation);

    /**
     * Check if assessment exists for application
     */
    boolean existsByApplicationId(UUID applicationId);
}