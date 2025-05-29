package de.tum.devops.assess.repository;

import de.tum.devops.assess.entity.Assessment;
import de.tum.devops.assess.entity.AssessmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Find assessments by status with pagination
     */
    Page<Assessment> findByStatus(AssessmentStatus status, Pageable pageable);

    /**
     * Find assessments by HR creator ID
     */
    Page<Assessment> findByHrCreatorId(UUID hrCreatorId, Pageable pageable);

    /**
     * Count assessments by status
     */
    long countByStatus(AssessmentStatus status);

    /**
     * Check if assessment exists for application
     */
    boolean existsByApplicationId(UUID applicationId);
}