package de.tum.devops.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight unit tests for {@link DocumentTextExtractorService} that avoid
 * heavy dependencies such as PDFBox or Apache POI.  They exercise helper
 * methods that are pure Java and always available, ensuring CI pipelines run.
 */
class DocumentTextExtractorServiceUTest {

    private DocumentTextExtractorService service;

    @BeforeEach
    void setup() {
        service = new DocumentTextExtractorService();
    }

    @Test
    void supportedExtensionsReturnTrue() {
        assertTrue(service.isTextExtractionSupported("file.pdf"));
        assertTrue(service.isTextExtractionSupported("file.doc"));
        assertTrue(service.isTextExtractionSupported("file.docx"));
    }

    @Test
    void unsupportedExtensionReturnsFalse() {
        assertFalse(service.isTextExtractionSupported("image.png"));
    }

    @Test
    void textPreviewShorterThanLimitReturnedUnchanged() {
        String txt = "Short text";
        assertEquals(txt, service.getTextPreview(txt));
    }

    @Test
    void textPreviewLongerThanLimitIsTruncated() {
        String longTxt = "a".repeat(600);
        String preview = service.getTextPreview(longTxt);
        assertEquals(503, preview.length());
        assertTrue(preview.endsWith("..."));
    }
}