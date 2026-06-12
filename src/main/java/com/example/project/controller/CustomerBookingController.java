package com.example.project.controller;

import com.example.project.dto.request.BookingRequest;
import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.BookingResponse;
import com.example.project.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerBookingController {

    private final BookingService bookingService;

    // ===== FR-06: Đặt lịch đánh cầu =====
    // POST /api/v1/customer/bookings
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookingResponse booking = bookingService.createBooking(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    // ===== FR-07: Xem lịch sử đặt sân =====
    // GET /api/v1/customer/bookings?page=0&size=10
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookingResponse> bookings = bookingService.getMyBookings(
                userDetails.getUsername(), pageable);

        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    // ===== Xem chi tiết một booking =====
    // GET /api/v1/customer/bookings/1
    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
    }
}

