package com.example.project.service.impl;

import com.example.project.dto.request.CourtRequest;
import com.example.project.dto.response.CourtImageResponse;
import com.example.project.dto.response.CourtResponse;
import com.example.project.dto.response.TimeSlotResponse;
import com.example.project.entity.Court;
import com.example.project.entity.CourtImage;
import com.example.project.entity.TimeSlot;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.CourtImageRepository;
import com.example.project.repository.CourtRepository;
import com.example.project.repository.TimeSlotRepository;
import com.example.project.service.CloudStorageService;
import com.example.project.service.CourtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CloudStorageService cloudStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponse> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(this::toCourtResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponse> getAvailableCourts() {
        return courtRepository.findByAvailableTrue().stream()
                .map(this::toCourtResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourtResponse getCourtById(Long id) {
        return toCourtResponse(findCourtOrThrow(id));
    }

    @Override
    @Transactional
    public CourtResponse createCourt(CourtRequest request) {
        Court court = Court.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pricePerSlot(request.getPricePerSlot())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();

        Court saved = courtRepository.save(court);
        log.info("Court created: {}", saved.getName());
        return toCourtResponse(saved);
    }

    @Override
    @Transactional
    public CourtResponse updateCourt(Long id, CourtRequest request) {
        Court court = findCourtOrThrow(id);

        if (request.getName() != null) court.setName(request.getName());
        if (request.getDescription() != null) court.setDescription(request.getDescription());
        if (request.getPricePerSlot() != null) court.setPricePerSlot(request.getPricePerSlot());
        if (request.getAvailable() != null) court.setAvailable(request.getAvailable());

        Court saved = courtRepository.save(court);
        log.info("Court updated: {}", saved.getName());
        return toCourtResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCourt(Long id) {
        Court court = findCourtOrThrow(id);
        courtRepository.delete(court);
        log.info("Court deleted: {}", court.getName());
    }

    @Override
    @Transactional
    public CourtImageResponse uploadCourtImage(Long courtId, MultipartFile file) {
        Court court = findCourtOrThrow(courtId);
        Map<String, String> uploadResult = cloudStorageService.upload(file);

        CourtImage image = CourtImage.builder()
                .court(court)
                .imageUrl(uploadResult.get("imageUrl"))
                .publicId(uploadResult.get("publicId"))
                .build();

        CourtImage saved = courtImageRepository.save(image);
        log.info("Image uploaded for court {}: {}", courtId, saved.getImageUrl());

        return toCourtImageResponse(saved);
    }

    @Override
    @Transactional
    public List<CourtImageResponse> uploadCourtImages(Long courtId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one image file is required");
        }

        return files.stream()
                .map(file -> uploadCourtImage(courtId, file))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCourtImage(Long courtId, Long imageId) {
        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("CourtImage", imageId));

        if (!image.getCourt().getId().equals(courtId)) {
            throw new IllegalArgumentException("Image does not belong to this court");
        }

        if (image.getPublicId() != null) {
            cloudStorageService.delete(image.getPublicId());
        }

        courtImageRepository.delete(image);
        log.info("Image {} deleted from court {}", imageId, courtId);
    }

    private Court findCourtOrThrow(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court", id));
    }

    private CourtResponse toCourtResponse(Court court) {
        List<CourtImageResponse> images = courtImageRepository.findByCourtId(court.getId()).stream()
                .map(this::toCourtImageResponse)
                .collect(Collectors.toList());

        List<TimeSlotResponse> timeSlots = timeSlotRepository.findByCourtIdAndIsActiveTrue(court.getId()).stream()
                .map(this::toTimeSlotResponse)
                .collect(Collectors.toList());

        return CourtResponse.builder()
                .id(court.getId())
                .name(court.getName())
                .description(court.getDescription())
                .pricePerSlot(court.getPricePerSlot())
                .available(court.getAvailable())
                .images(images)
                .timeSlots(timeSlots)
                .createdAt(court.getCreatedAt())
                .build();
    }

    private CourtImageResponse toCourtImageResponse(CourtImage image) {
        return CourtImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .uploadedAt(image.getUploadedAt())
                .build();
    }

    private TimeSlotResponse toTimeSlotResponse(TimeSlot slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .courtId(slot.getCourt().getId())
                .courtName(slot.getCourt().getName())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isActive(slot.getIsActive())
                .build();
    }
}
