package de.tum.devops.job.persistence.repository;

import de.tum.devops.job.persistence.entity.Job;
import de.tum.devops.job.persistence.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Job entity
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    /**
     * Find jobs by status with pagination
     */
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    /**
     * Find jobs by status list (for role-based filtering)
     */
    @Query("SELECT j FROM Job j WHERE j.status IN :statuses")
    Page<Job> findByStatusIn(@Param("statuses") JobStatus[] statuses, Pageable pageable);

    /**
     * Find jobs by HR creator ID
     */
    Page<Job> findByHrCreatorId(UUID hrCreatorId, Pageable pageable);

    /**
     * Find jobs by HR creator ID and status
     */
    Page<Job> findByHrCreatorIdAndStatus(UUID hrCreatorId, JobStatus status, Pageable pageable);

    /**
     * Count jobs by status
     */
    long countByStatus(JobStatus status);

    /**
     * Count jobs by HR creator
     */
    long countByHrCreatorId(UUID hrCreatorId);

    /**
     * Check if job exists and is open
     */
    boolean existsByJobIdAndStatus(UUID jobId, JobStatus status);
}