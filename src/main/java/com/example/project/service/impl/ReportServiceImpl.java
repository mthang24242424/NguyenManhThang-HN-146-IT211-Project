package com.example.project.service.impl;

import com.example.project.dto.response.RevenueReportResponse;
import com.example.project.dto.response.TimeSlotReportResponse;
import com.example.project.entity.Booking;
import com.example.project.enums.BookingStatus;
import com.example.project.repository.BookingRepository;
import com.example.project.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotReportResponse> getTimeSlotReport(LocalDate date, BookingStatus status) {
        return bookingRepository.findAll().stream()
                .filter(b -> date == null || b.getBookingDate().equals(date))
                .filter(b -> status == null || b.getStatus() == status)
                .map(this::toTimeSlotReport)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getMonthlyRevenue(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                .filter(b -> !b.getBookingDate().isBefore(start) && !b.getBookingDate().isAfter(end))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.CHECKED_IN)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = confirmedBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueReportResponse.builder()
                .year(year)
                .month(month)
                .totalBookings(confirmedBookings.size())
                .totalRevenue(totalRevenue)
                .build();
    }

    private TimeSlotReportResponse toTimeSlotReport(Booking booking) {
        return TimeSlotReportResponse.builder()
                .bookingId(booking.getId())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getTimeSlot().getStartTime())
                .endTime(booking.getTimeSlot().getEndTime())
                .status(booking.getStatus())
                .customerName(booking.getCustomer().getFullName())
                .totalPrice(booking.getTotalPrice())
                .build();
    }
}
