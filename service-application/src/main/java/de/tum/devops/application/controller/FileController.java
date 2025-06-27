package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.dto.FileInfoDto;
import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.service.ApplicationService;
import de.tum.devops.application.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Application - Resume File Controller
 */
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;
    private final ApplicationService applicationService;

    public FileController(FileStorageService fileStorageService, ApplicationService applicationService) {
        this.fileStorageService = fileStorageService;
        this.applicationService = applicationService;
    }

    /**
     * Download resume file by application ID
     * Only HR can download any resume, candidates can only download their own
     */
    @GetMapping("/applications/{applicationId}/resume")
    @PreAuthorize("hasRole('HR') or hasRole('CANDIDATE')")
    public ResponseEntity<Resource> downloadResume(@PathVariable UUID applicationId,
                                                   Authentication authentication,
                                                   @AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            logger.info("User {} requesting download of resume for application: {}", userId, applicationId);

            // Get application
            Application application = applicationService.getById(applicationId);

            // Security check: verify user has access to this application
            Optional<? extends GrantedAuthority> userRoleOptional = authentication.getAuthorities().stream().findFirst();
            String userRole = userRoleOptional.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")).orElse(null);

            if ("CANDIDATE".equals(userRole)) {
                // Candidates can only download their own resume files
                if (!application.getCandidateId().equals(UUID.fromString(userId))) {
                    logger.warn("Candidate {} attempted to access application not belonging to them: {}", userId, applicationId);
                    return ResponseEntity.status(403).build();
                }
            }

            // Check if application has a resume file
            String filename = application.getResumeFilePath();
            if (filename == null || filename.trim().isEmpty()) {
                logger.warn("No resume file found for application: {}", applicationId);
                return ResponseEntity.notFound().build();
            }

            // Check if file exists
            if (!fileStorageService.fileExists(filename)) {
                logger.warn("Resume file not found on disk: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Get file path and create resource
            Path filePath = fileStorageService.getFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                logger.error("Resume file not readable: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = fileStorageService.getContentType(filename);

            // Generate a user-friendly filename for download
            String downloadFilename = "resume_" + applicationId + getFileExtension(filename);

            // Build response with appropriate headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileStorageService.getFileSize(filename)))
                    .body(resource);

        } catch (MalformedURLException e) {
            logger.error("Malformed URL for application: {}", applicationId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error downloading resume for application: {}", applicationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get resume file information by application ID
     */
    @GetMapping("/applications/{applicationId}/resume/info")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<FileInfoDto>> getResumeFileInfo(@PathVariable UUID applicationId) {
        try {
            // Get application
            Application application = applicationService.getById(applicationId);

            // Check if application has a resume file
            String filename = application.getResumeFilePath();
            if (filename == null || filename.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("No resume file found for this application"));
            }

            // Check if file exists on disk
            if (!fileStorageService.fileExists(filename)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Resume file not found on disk"));
            }

            long fileSize = fileStorageService.getFileSize(filename);
            String fileContentType = fileStorageService.getContentType(filename);

            FileInfoDto fileInfo = new FileInfoDto(
                    application.getApplicationId(),
                    filename,
                    "resume_" + application.getApplicationId() + getFileExtension(filename),
                    fileSize,
                    fileContentType,
                    true,
                    application.getResumeText() != null ?
                            (application.getResumeText().length() > 200 ?
                                    application.getResumeText().substring(0, 200) + "..." :
                                    application.getResumeText()) : null,
                    application.getResumeText() != null ?
                            application.getResumeText().length() : 0
            );

            return ResponseEntity.ok(ApiResponse.success("Resume file info retrieved", fileInfo));

        } catch (Exception e) {
            logger.error("Error getting resume file info for application: {}", applicationId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalError("Failed to get resume file information"));
        }
    }

    /**
     * Helper method to get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex != -1) ? filename.substring(lastDotIndex) : "";
    }
}