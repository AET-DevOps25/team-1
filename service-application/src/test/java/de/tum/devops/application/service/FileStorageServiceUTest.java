package de.tum.devops.application.service;

import de.tum.devops.application.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple tests for {@link FileStorageService} focusing on utility methods that are
 * deterministic and do not require real file uploads.
 */
class FileStorageServiceUTest {

    @TempDir
    Path tempDir; // JUnit creates a temporary directory for us

    private FileStorageService service;

    @BeforeEach
    void setup() {
        // Mock properties so the service writes into the temp directory and has tiny limits
        FileStorageProperties props = Mockito.mock(FileStorageProperties.class);
        Mockito.when(props.getUploadDir()).thenReturn(tempDir.toString());
        Mockito.when(props.getMaxFileSize()).thenReturn(1024L); // 1 KiB for convenience

        service = new FileStorageService(props);
    }

    @Test
    void getContentTypeRecognisesPdf() {
        assertEquals("application/pdf", service.getContentType("brochure.pdf"));
    }

    @Test
    void getContentTypeUnknownDefaultsToOctetStream() {
        assertEquals("application/octet-stream", service.getContentType("archive.zip"));
    }

    @Test
    void getFilePathResolvesInsideUploadDir() {
        Path path = service.getFilePath("test.txt");
        assertTrue(path.toAbsolutePath().startsWith(tempDir.toAbsolutePath()));
    }
}