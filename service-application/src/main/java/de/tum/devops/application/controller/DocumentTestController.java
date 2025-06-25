package de.tum.devops.application.controller;

import de.tum.devops.application.dto.ApiResponse;
import de.tum.devops.application.service.DocumentTextExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Test controller for document text extraction functionality
 * This can be removed in production
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class DocumentTestController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTestController.class);

    private final DocumentTextExtractorService documentTextExtractorService;

    public DocumentTestController(DocumentTextExtractorService documentTextExtractorService) {
        this.documentTextExtractorService = documentTextExtractorService;
    }

    /**
     * Test endpoint to extract text from uploaded document
     */
    @PostMapping("/extract-text")
    public ResponseEntity<ApiResponse<Object>> extractText(@RequestPart("file") MultipartFile file) {
        try {
            String extractedText = documentTextExtractorService.extractText(file);
            String textPreview = documentTextExtractorService.getTextPreview(extractedText);

            Object result = new Object() {
                public final String filename = file.getOriginalFilename();
                public final long fileSize = file.getSize();
                public final String contentType = file.getContentType();
                public final int textLength = extractedText.length();
                public final String preview = textPreview;
                public final boolean supported = documentTextExtractorService.isTextExtractionSupported(file.getOriginalFilename());
            };

            return ResponseEntity.ok(ApiResponse.success("Text extracted successfully", result));
        } catch (Exception e) {
            logger.error("Failed to extract text from file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to extract text: " + e.getMessage()));
        }
    }
}