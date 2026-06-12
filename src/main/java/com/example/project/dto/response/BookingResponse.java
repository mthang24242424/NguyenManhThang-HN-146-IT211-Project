package com.example.project.dto.response;

import com.example.project.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long courtId;
    private String courtName;
    private Long timeSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate bookingDate;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private String note;
    private LocalDateTime createdAt;
}
