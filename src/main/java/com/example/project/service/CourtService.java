package com.example.project.service;

import com.example.project.dto.request.CourtRequest;
import com.example.project.dto.response.CourtImageResponse;
import com.example.project.dto.response.CourtResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourtService {

    List<CourtResponse> getAllCourts();

    List<CourtResponse> getAvailableCourts();

    CourtResponse getCourtById(Long id);

    CourtResponse createCourt(CourtRequest request);

    CourtResponse updateCourt(Long id, CourtRequest request);

    void deleteCourt(Long id);

    CourtImageResponse uploadCourtImage(Long courtId, MultipartFile file);

    void deleteCourtImage(Long courtId, Long imageId);
}
