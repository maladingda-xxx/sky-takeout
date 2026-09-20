package com.sky.takeout.service.impl;

import com.sky.takeout.config.UploadProperties;
import com.sky.takeout.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDirectory;

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        UploadProperties properties = new UploadProperties();
        properties.setPath(tempDirectory.toString());
        properties.setUrlPrefix("/uploads");
        properties.setMaxFileSize(1024 * 1024);
        fileStorageService = new FileStorageServiceImpl(properties);
    }

    @Test
    void shouldStoreValidPngAndReturnUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.png",
                "image/png",
                createPngBytes()
        );

        String url = fileStorageService.uploadImage(file);
        String filename = url.substring("/uploads/".length());

        assertTrue(url.matches("^/uploads/[a-f0-9]{32}\\.png$"));
        assertTrue(Files.exists(tempDirectory.resolve(filename)));
    }

    @Test
    void shouldRejectUnsupportedExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.txt",
                "text/plain",
                createPngBytes()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileStorageService.uploadImage(file)
        );

        assertEquals(400, exception.getCode());
        assertEquals("Only JPG, PNG, and GIF images are allowed", exception.getMessage());
    }

    @Test
    void shouldRejectFakeImageContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.png",
                "image/png",
                "not an image".getBytes()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileStorageService.uploadImage(file)
        );

        assertEquals(400, exception.getCode());
        assertEquals("File content is not a valid image", exception.getMessage());
    }

    @Test
    void shouldRejectOversizedFileBeforeImageParsing() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                new byte[1024 * 1024 + 1]
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileStorageService.uploadImage(file)
        );

        assertEquals(400, exception.getCode());
        assertEquals("File size must not exceed 5 MB", exception.getMessage());
    }

    @Test
    void shouldIgnoreUnsafeOriginalPath() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../unsafe.png",
                "image/png",
                createPngBytes()
        );

        String url = fileStorageService.uploadImage(file);

        assertTrue(url.matches("^/uploads/[a-f0-9]{32}\\.png$"));
        assertTrue(Files.exists(tempDirectory.resolve(url.substring("/uploads/".length()))));
    }

    private byte[] createPngBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
