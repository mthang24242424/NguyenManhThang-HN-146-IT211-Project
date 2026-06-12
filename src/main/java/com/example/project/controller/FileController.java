package com.example.project.controller;

import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.FileUploadResponse;
import com.example.project.service.CloudStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final CloudStorageService cloudStorageService;

    // UC-05: Upload file lên Cloudinary
    // POST /api/v1/files/upload
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, String> result = cloudStorageService.upload(file);

        FileUploadResponse response = FileUploadResponse.builder()
                .imageUrl(result.get("imageUrl"))
                .publicId(result.get("publicId"))
                .build();

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }
}
