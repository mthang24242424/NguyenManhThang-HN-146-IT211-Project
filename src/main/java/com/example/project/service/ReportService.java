package com.example.project.service;

import com.example.project.dto.response.RevenueReportResponse;
import com.example.project.dto.response.TimeSlotReportResponse;
import com.example.project.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<TimeSlotReportResponse> getTimeSlotReport(LocalDate date, BookingStatus status);

    RevenueReportResponse getMonthlyRevenue(int year, int month);
}
