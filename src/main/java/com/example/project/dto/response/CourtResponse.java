package com.example.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class CourtResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerSlot;
    private Boolean available;
    private List<CourtImageResponse> images;
    private List<TimeSlotResponse> timeSlots;
    private LocalDateTime createdAt;
}
