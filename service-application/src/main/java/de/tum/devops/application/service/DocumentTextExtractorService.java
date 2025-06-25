package de.tum.devops.application.service;

import de.tum.devops.application.exception.FileStorageException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service for extracting text from various document formats
 */
@Service
public class DocumentTextExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTextExtractorService.class);

    /**
     * Extract text from uploaded document file
     *
     * @param file The uploaded document file
     * @return Extracted text content
     * @throws FileStorageException if text extraction fails
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is missing");
        }

        String extension = getFileExtension(filename).toLowerCase();

        try (InputStream inputStream = file.getInputStream()) {
            return switch (extension) {
                case ".pdf" -> extractTextFromPdf(file.getBytes());
                case ".doc" -> extractTextFromDoc(inputStream);
                case ".docx" -> extractTextFromDocx(inputStream);
                default -> throw new IllegalArgumentException("Unsupported file format: " + extension);
            };
        } catch (IOException e) {
            logger.error("Failed to extract text from file: {}", filename, e);
            throw new FileStorageException("Failed to extract text from document", e);
        }
    }

    /**
     * Extract text from PDF file
     */
    private String extractTextFromPdf(byte[] fileBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Clean up the extracted text
            return cleanExtractedText(text);
        }
    }

    /**
     * Extract text from DOC file (older Word format)
     */
    private String extractTextFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {

            String text = extractor.getText();
            return cleanExtractedText(text);
        }
    }

    /**
     * Extract text from DOCX file (newer Word format)
     */
    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();
            return cleanExtractedText(text);
        }
    }

    /**
     * Clean and normalize extracted text
     */
    private String cleanExtractedText(String text) {
        if (text == null) {
            return "";
        }

        // Remove excessive whitespace and normalize line breaks
        text = text.replaceAll("\\r\\n", "\n")  // Normalize line breaks
                .replaceAll("\\r", "\n")      // Convert remaining \r to \n
                .replaceAll("\\n{3,}", "\n\n") // Reduce multiple line breaks to max 2
                .replaceAll("[ \\t]+", " ")    // Replace multiple spaces/tabs with single space
                .trim();                       // Remove leading/trailing whitespace

        // Ensure we have some content
        if (text.length() < 10) {
            throw new FileStorageException("Document appears to be empty or contains insufficient text");
        }

        return text;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex != -1) ? filename.substring(lastDotIndex) : "";
    }

    /**
     * Validate if the file format is supported for text extraction
     */
    public boolean isTextExtractionSupported(String filename) {
        if (filename == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".pdf") || extension.equals(".doc") || extension.equals(".docx");
    }

    /**
     * Get a preview of the extracted text (first 500 characters)
     */
    public String getTextPreview(String fullText) {
        if (fullText == null || fullText.isEmpty()) {
            return "";
        }

        if (fullText.length() <= 500) {
            return fullText;
        }

        return fullText.substring(0, 500) + "...";
    }
}