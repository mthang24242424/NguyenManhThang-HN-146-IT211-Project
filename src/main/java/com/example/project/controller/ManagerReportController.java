package com.example.project.controller;

import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.RevenueReportResponse;
import com.example.project.dto.response.TimeSlotReportResponse;
import com.example.project.enums.BookingStatus;
import com.example.project.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerReportController {

    private final ReportService reportService;

    // UC-02: Báo cáo khung giờ đặt sân
    // GET /api/v1/manager/reports/time-slots?date=2026-06-08&status=CONFIRMED
    @GetMapping("/time-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotReportResponse>>> getTimeSlotReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) BookingStatus status) {

        List<TimeSlotReportResponse> report = reportService.getTimeSlotReport(date, status);
        return ResponseEntity.ok(ApiResponse.success("Time slot report retrieved successfully", report));
    }

    // UC-02: Báo cáo doanh thu theo tháng
    // GET /api/v1/manager/reports/revenue?year=2026&month=6
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getMonthlyRevenue(
            @RequestParam int year,
            @RequestParam int month) {

        RevenueReportResponse report = reportService.getMonthlyRevenue(year, month);
        return ResponseEntity.ok(ApiResponse.success("Revenue report retrieved successfully", report));
    }
}
