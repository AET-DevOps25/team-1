package de.tum.devops.application.persistence.repository;

import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Application entity
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    /**
     * Find applications by candidate ID with pagination
     */
    Page<Application> findByCandidateId(UUID candidateId, Pageable pageable);

    /**
     * Find applications by job ID with pagination
     */
    Page<Application> findByJobId(UUID jobId, Pageable pageable);

    /**
     * Find applications by status with pagination
     */
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by candidate ID and status
     */
    Page<Application> findByCandidateIdAndStatus(UUID candidateId, ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by job ID and status
     */
    Page<Application> findByJobIdAndStatus(UUID jobId, ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by candidate ID and job ID
     */
    Page<Application> findByCandidateIdAndJobId(UUID candidateId, UUID jobId, Pageable pageable);

    /**
     * Find applications by job ID, status and candidate ID (for complex filtering)
     */
    Page<Application> findByJobIdAndStatusAndCandidateId(UUID jobId, ApplicationStatus status, UUID candidateId, Pageable pageable);

    /**
     * Find applications by candidate ID, job ID and status (different parameter order for service method)
     */
    Page<Application> findByCandidateIdAndJobIdAndStatus(UUID candidateId, UUID jobId, ApplicationStatus status, Pageable pageable);

    /**
     * Check if candidate has already applied for a specific job
     */
    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    /**
     * Find all applications for a specific job
     */
    List<Application> findByJobIdOrderBySubmittedAtDesc(UUID jobId);

    /**
     * Find applications with chat sessions (for HR to see which have interviews)
     */
    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.chatSession WHERE a.chatSession IS NOT NULL")
    List<Application> findApplicationsWithChatSessions();

    /**
     * Find application by ID with all related entities fetched
     */
    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.chatSession LEFT JOIN FETCH a.assessment WHERE a.applicationId = :applicationId")
    Optional<Application> findByIdWithRelations(@Param("applicationId") UUID applicationId);

    /**
     * Count applications by status
     */
    long countByStatus(ApplicationStatus status);

    /**
     * Count applications for a specific job
     */
    long countByJobId(UUID jobId);
}