package com.example.project.repository;

import com.example.project.entity.Booking;
import com.example.project.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Kiểm tra xung đột lịch (UC-04)
    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId " +
            "AND b.timeSlot.id = :timeSlotId " +
            "AND b.bookingDate = :bookingDate " +
            "AND b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findConflictingBookings(
            @Param("courtId") Long courtId,
            @Param("timeSlotId") Long timeSlotId,
            @Param("bookingDate") LocalDate bookingDate
    );

    // FR-07: Lịch sử đặt sân của customer
    Page<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    // FR-08: Lọc toàn bộ booking cho admin/manager
    @Query("SELECT b FROM Booking b WHERE " +
            "(:status IS NULL OR b.status = :status) " +
            "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
            "AND (:courtId IS NULL OR b.court.id = :courtId)")
    Page<Booking> findBookingsWithFilter(
            @Param("status") BookingStatus status,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("courtId") Long courtId,
            Pageable pageable
    );
}

