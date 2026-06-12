package com.example.project.dto.response;

import com.example.project.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class TimeSlotReportResponse {

    private Long bookingId;
    private Long courtId;
    private String courtName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private String customerName;
    private BigDecimal totalPrice;
}
