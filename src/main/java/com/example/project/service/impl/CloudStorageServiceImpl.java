package com.example.project.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.project.exception.CloudStorageException;
import com.example.project.service.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudStorageServiceImpl implements CloudStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> upload(MultipartFile file) {
        validateFile(file);

        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "badminton-courts")
            );

            return Map.of(
                    "imageUrl", (String) result.get("secure_url"),
                    "publicId", (String) result.get("public_id")
            );
        } catch (IOException ex) {
            log.error("Failed to upload file to Cloudinary", ex);
            throw new CloudStorageException("Upload failed", ex);
        } catch (Exception ex) {
            log.error("Cloudinary service error", ex);
            throw new CloudStorageException("Cloud storage unavailable", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ex) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, ex);
            throw new CloudStorageException("Delete failed", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPG and PNG images are allowed");
        }
    }
}
