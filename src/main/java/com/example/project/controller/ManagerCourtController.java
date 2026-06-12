package com.example.project.controller;

import com.example.project.dto.request.CourtRequest;
import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.CourtImageResponse;
import com.example.project.dto.response.CourtResponse;
import com.example.project.service.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/courts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerCourtController {

    private final CourtService courtService;

    // POST /api/v1/manager/courts
    @PostMapping
    public ResponseEntity<ApiResponse<CourtResponse>> createCourt(
            @Valid @RequestBody CourtRequest request) {

        CourtResponse court = courtService.createCourt(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Court created successfully", court));
    }

    // PUT /api/v1/manager/courts/1
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtResponse>> updateCourt(
            @PathVariable Long id,
            @Valid @RequestBody CourtRequest request) {

        CourtResponse court = courtService.updateCourt(id, request);
        return ResponseEntity.ok(ApiResponse.success("Court updated successfully", court));
    }

    // DELETE /api/v1/manager/courts/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/manager/courts/1/images (FR-09)
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CourtImageResponse>> uploadCourtImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        CourtImageResponse image = courtService.uploadCourtImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", image));
    }

    // POST /api/v1/manager/courts/1/images/batch (FR-09)
    @PostMapping(value = "/{id}/images/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<CourtImageResponse>>> uploadCourtImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {

        List<CourtImageResponse> images = courtService.uploadCourtImages(id, files);
        return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", images));
    }

    // DELETE /api/v1/manager/courts/1/images/2
    @DeleteMapping("/{courtId}/images/{imageId}")
    public ResponseEntity<Void> deleteCourtImage(
            @PathVariable Long courtId,
            @PathVariable Long imageId) {

        courtService.deleteCourtImage(courtId, imageId);
        return ResponseEntity.noContent().build();
    }
}
