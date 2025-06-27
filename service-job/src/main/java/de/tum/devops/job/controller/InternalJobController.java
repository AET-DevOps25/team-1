package de.tum.devops.job.controller;

import de.tum.devops.job.dto.ApiResponse;
import de.tum.devops.job.dto.JobDto;
import de.tum.devops.job.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal job info endpoint (cluster-internal, no auth).
 */
@RestController
@RequestMapping("/internal/api/v1/jobs")
public class InternalJobController {

    private static final Logger logger = LoggerFactory.getLogger(InternalJobController.class);

    private final JobService jobService;

    public InternalJobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * GET /internal/api/v1/jobs/{jobId} – fetch job details for internal services.
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobDto>> getJobInternal(@PathVariable UUID jobId) {
        try {
            JobDto dto = jobService.getJobById(jobId, null);
            return ResponseEntity.ok(ApiResponse.success("OK", dto));
        } catch (IllegalArgumentException ex) {
            logger.warn("Job not found: {}", ex.getMessage());
            return ResponseEntity.status(404).body(ApiResponse.notFound(ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Error fetching job {}: {}", jobId, ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.internalError("Failed to fetch job"));
        }
    }
}