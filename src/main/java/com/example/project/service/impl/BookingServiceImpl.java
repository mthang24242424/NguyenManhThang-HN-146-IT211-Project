package com.example.project.service.impl;

import com.example.project.dto.request.BookingRequest;
import com.example.project.dto.response.BookingResponse;
import com.example.project.entity.Booking;
import com.example.project.entity.Court;
import com.example.project.entity.TimeSlot;
import com.example.project.entity.User;
import com.example.project.enums.BookingStatus;
import com.example.project.exception.ConflictException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.BookingRepository;
import com.example.project.repository.CourtRepository;
import com.example.project.repository.TimeSlotRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;

    // ===== FR-06: Đặt lịch đánh cầu (UC-04) =====
    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // Lấy thông tin customer
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Kiểm tra sân tồn tại và đang hoạt động
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court", request.getCourtId()));

        if (!court.getAvailable()) {
            throw new IllegalArgumentException("Court is not available for booking");
        }

        // Kiểm tra time slot tồn tại
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", request.getTimeSlotId()));

        // UC-04: Kiểm tra xung đột lịch (409 Conflict)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getCourtId(),
                request.getTimeSlotId(),
                request.getBookingDate()
        );

        if (!conflicts.isEmpty()) {
            throw new ConflictException(
                    "Court " + court.getName() + " is already booked for this time slot on " + request.getBookingDate()
            );
        }

        // Tạo booking mới với trạng thái PENDING
        Booking booking = Booking.builder()
                .customer(customer)
                .court(court)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
                .status(BookingStatus.PENDING)
                .totalPrice(court.getPricePerSlot())
                .note(request.getNote())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.debug("Booking created with id: {}", saved.getId());

        return toBookingResponse(saved);
    }

    // ===== FR-07: Lịch sử đặt sân (customer) =====
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(String username, Pageable pageable) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return bookingRepository
                .findByCustomerIdOrderByCreatedAtDesc(customer.getId(), pageable)
                .map(this::toBookingResponse); // UC-02: Stream .map() thay for-loop
    }

    // ===== FR-08: Admin/Manager xem tất cả booking =====
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(
            BookingStatus status, LocalDate date, Long courtId, Pageable pageable) {
        return bookingRepository
                .findBookingsWithFilter(status, date, courtId, pageable)
                .map(this::toBookingResponse);
    }

    // ===== FR-08: Phê duyệt / Từ chối =====
    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        validateStatusTransition(booking.getStatus(), newStatus);

        booking.setStatus(newStatus);
        Booking updated = bookingRepository.save(booking);
        log.info("Booking {} status updated to {}", bookingId, newStatus);

        return toBookingResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return toBookingResponse(booking);
    }

    // ===== State Transition Validation (State Diagram) =====
    private void validateStatusTransition(BookingStatus current, BookingStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == BookingStatus.CONFIRMED || next == BookingStatus.REJECTED
                    || next == BookingStatus.CANCELLED;
            case CONFIRMED -> next == BookingStatus.CHECKED_IN || next == BookingStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Cannot transition booking from " + current + " to " + next);
        }
    }

    // ===== UC-02: Entity -> DTO mapping
    private BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFullName())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .timeSlotId(booking.getTimeSlot().getId())
                .startTime(booking.getTimeSlot().getStartTime())
                .endTime(booking.getTimeSlot().getEndTime())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .note(booking.getNote())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

