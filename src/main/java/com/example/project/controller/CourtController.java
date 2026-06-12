package com.example.project.controller;

import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.CourtResponse;
import com.example.project.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // GET /api/v1/courts
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourtResponse>>> getAllCourts(
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {

        List<CourtResponse> courts = availableOnly
                ? courtService.getAvailableCourts()
                : courtService.getAllCourts();

        return ResponseEntity.ok(ApiResponse.success("Courts retrieved successfully", courts));
    }

    // GET /api/v1/courts/1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtResponse>> getCourtById(@PathVariable Long id) {
        CourtResponse court = courtService.getCourtById(id);
        return ResponseEntity.ok(ApiResponse.success("Court retrieved successfully", court));
    }
}
