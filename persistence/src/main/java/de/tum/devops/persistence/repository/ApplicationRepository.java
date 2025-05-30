package de.tum.devops.persistence.repository;

import de.tum.devops.persistence.entity.Application;
import de.tum.devops.persistence.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Application entity
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    /**
     * Find applications by candidate ID
     */
    Page<Application> findByCandidateId(UUID candidateId, Pageable pageable);

    /**
     * Find applications by job ID
     */
    Page<Application> findByJobId(UUID jobId, Pageable pageable);

    /**
     * Find applications by status with pagination
     */
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by status list (for filtering)
     */
    @Query("SELECT a FROM Application a WHERE a.status IN :statuses")
    Page<Application> findByStatusIn(@Param("statuses") ApplicationStatus[] statuses, Pageable pageable);

    /**
     * Find applications by candidate and job (to prevent duplicates)
     */
    Optional<Application> findByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    /**
     * Check if candidate has already applied for job
     */
    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    /**
     * Count applications by status
     */
    long countByStatus(ApplicationStatus status);

    /**
     * Count applications by job ID
     */
    long countByJobId(UUID jobId);

    /**
     * Count applications by candidate ID
     */
    long countByCandidateId(UUID candidateId);

    /**
     * Find all applications for jobs created by specific HR user
     */
    @Query("SELECT a FROM Application a JOIN Job j ON a.jobId = j.jobId WHERE j.hrCreatorId = :hrUserId")
    Page<Application> findApplicationsForHrJobs(@Param("hrUserId") UUID hrUserId, Pageable pageable);
}