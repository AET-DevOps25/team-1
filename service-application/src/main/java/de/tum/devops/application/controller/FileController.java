package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.service.ApplicationService;
import de.tum.devops.application.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Controller for file operations (download, etc.)
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
     * Download resume file
     * Only HR can download any resume, candidates can only download their own
     */
    @GetMapping("/resumes/{filename}")
    @PreAuthorize("hasRole('HR') or hasRole('CANDIDATE')")
    public ResponseEntity<Resource> downloadResume(@PathVariable String filename,
                                                   Authentication authentication,
                                                   @AuthenticationPrincipal String userId) {
        try {
            logger.info("User {} requesting download of file: {}", userId, filename);

            // Security check: verify user has access to this file
            Optional<? extends GrantedAuthority> userRoleOptional = authentication.getAuthorities().stream().findFirst();
            String userRole = userRoleOptional.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")).orElse(null);
            if ("CANDIDATE".equals(userRole)) {
                // Candidates can only download their own resume files
                // Check if the filename contains the candidate's ID
                if (!filename.contains(userId)) {
                    logger.warn("Candidate {} attempted to access file not belonging to them: {}", userId, filename);
                    return ResponseEntity.status(403).build();
                }
            }

            // Check if file exists
            if (!fileStorageService.fileExists(filename)) {
                logger.warn("Requested file not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Get file path and create resource
            Path filePath = fileStorageService.getFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                logger.error("File not readable: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = fileStorageService.getContentType(filename);

            // Build response with appropriate headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileStorageService.getFileSize(filename)))
                    .body(resource);

        } catch (MalformedURLException e) {
            logger.error("Malformed URL for file: {}", filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error downloading file: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file information
     */
    @GetMapping("/resumes/{filename}/info")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<Object>> getFileInfo(@PathVariable String filename) {
        try {
            if (!fileStorageService.fileExists(filename)) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = fileStorageService.getFileSize(filename);
            String fileContentType = fileStorageService.getContentType(filename);

            Object fileInfo = new Object() {
                public final String fileName = filename;
                public final long size = fileSize;
                public final String contentType = fileContentType;
                public final boolean exists = true;
            };

            return ResponseEntity.ok(ApiResponse.success("File info retrieved", fileInfo));

        } catch (Exception e) {
            logger.error("Error getting file info for: {}", filename, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.internalError("Failed to get file information"));
        }
    }
}