package de.tum.devops.job.service;

import de.tum.devops.job.client.AuthWebClient;
import de.tum.devops.job.dto.JobDto;
import de.tum.devops.job.dto.UserDto;
import de.tum.devops.job.persistence.entity.Job;
import de.tum.devops.job.persistence.enums.JobStatus;
import de.tum.devops.job.persistence.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Very lightweight unit tests for {@link JobService} that avoid Spring context.
 */
class JobServiceUTest {

    private JobService jobService;
    private JobRepository jobRepository;
    private AuthWebClient authWebClient;

    private UUID hrId;
    private Job job;

    @BeforeEach
    void setUp() {
        jobRepository = Mockito.mock(JobRepository.class);
        authWebClient = Mockito.mock(AuthWebClient.class);
        jobService = new JobService(jobRepository, authWebClient);

        hrId = UUID.randomUUID();
        job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setTitle("Software Engineer");
        job.setDescription("Dev work");
        job.setRequirements("Java");
        job.setStatus(JobStatus.OPEN);
        job.setHrCreatorId(hrId);
        job.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void candidateCannotSeeHrId() {
        // Arrange
        when(jobRepository.findById(job.getJobId())).thenReturn(Optional.of(job));
        UserDto hrDto = new UserDto(hrId, "HR One", "hr@example.com", "HR");
        when(authWebClient.fetchUser(hrId)).thenReturn(Mono.just(hrDto));

        // Act
        JobDto dto = jobService.getJobById(job.getJobId(), "CANDIDATE", false);

        // Assert
        assertNull(dto.getHrCreator().getUserID());
    }

    @Test
    void isJobOpenForApplicationsDelegatesToRepository() {
        // Arrange
        when(jobRepository.existsByJobIdAndStatus(job.getJobId(), JobStatus.OPEN)).thenReturn(true);

        // Act & Assert
        assertTrue(jobService.isJobOpenForApplications(job.getJobId()));
    }
}