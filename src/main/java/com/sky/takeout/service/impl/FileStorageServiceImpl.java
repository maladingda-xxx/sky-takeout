package com.sky.takeout.service.impl;

import com.sky.takeout.config.UploadProperties;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif");

    private final UploadProperties uploadProperties;

    public FileStorageServiceImpl(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        validateFile(file);

        String extension = resolveExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        Path uploadDirectory = Paths.get(uploadProperties.getPath())
                .toAbsolutePath()
                .normalize();
        Path target = uploadDirectory.resolve(filename).normalize();

        if (!target.startsWith(uploadDirectory)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid upload path"
            );
        }

        try {
            Files.createDirectories(uploadDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target);
            }
        } catch (IOException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to store uploaded image"
            );
        }

        return normalizeUrlPrefix() + "/" + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "File must not be empty"
            );
        }

        if (file.getSize() > uploadProperties.getMaxFileSize()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "File size must not exceed 5 MB"
            );
        }

        resolveExtension(file.getOriginalFilename());
        validateImageContent(file);
    }

    private void validateImageContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "File content is not a valid image"
                );
            }
        } catch (IOException exception) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "File content is not a valid image"
            );
        }
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Image filename must not be blank"
            );
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Image filename must include an extension"
            );
        }

        String extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Only JPG, PNG, and GIF images are allowed"
            );
        }

        return extension;
    }

    private String normalizeUrlPrefix() {
        String prefix = uploadProperties.getUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            return "/uploads";
        }

        String normalized = prefix.startsWith("/") ? prefix : "/" + prefix;
        return normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
