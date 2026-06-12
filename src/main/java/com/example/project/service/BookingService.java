package com.example.project.service;

import com.example.project.dto.request.BookingRequest;
import com.example.project.dto.response.BookingResponse;
import com.example.project.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BookingService {

    // FR-06: Đặt lịch (customer)
    BookingResponse createBooking(BookingRequest request, String username);

    // FR-07: Lịch sử đặt sân
    Page<BookingResponse> getMyBookings(String username, Pageable pageable);

    // FR-08: Admin/Manager duyệt lịch
    Page<BookingResponse> getAllBookings(BookingStatus status, LocalDate date, Long courtId, Pageable pageable);

    BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus);

    BookingResponse getBookingById(Long id);
}

