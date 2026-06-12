package com.example.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Court ID is required")
    private Long courtId;

    @NotNull(message = "Time slot ID is required")
    private Long timeSlotId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be today or in the future")
    private LocalDate bookingDate;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}

