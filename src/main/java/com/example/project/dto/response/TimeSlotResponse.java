package com.example.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class TimeSlotResponse {

    private Long id;
    private Long courtId;
    private String courtName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
}
