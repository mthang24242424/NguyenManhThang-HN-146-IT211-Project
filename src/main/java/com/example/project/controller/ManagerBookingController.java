package com.example.project.controller;

import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.BookingResponse;
import com.example.project.enums.BookingStatus;
import com.example.project.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/manager/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerBookingController {

    private final BookingService bookingService;

    // FR-08: Manager xem và phê duyệt booking
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
            @RequestParam(required = false) Long courtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookingResponse> bookings = bookingService.getAllBookings(status, bookingDate, courtId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus newStatus) {

        BookingResponse updated = bookingService.updateBookingStatus(id, newStatus);
        return ResponseEntity.ok(ApiResponse.success(
                "Booking status updated to " + newStatus, updated));
    }
}
