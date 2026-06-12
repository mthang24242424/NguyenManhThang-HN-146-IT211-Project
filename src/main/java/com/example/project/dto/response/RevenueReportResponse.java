package com.example.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueReportResponse {

    private int year;
    private int month;
    private long totalBookings;
    private BigDecimal totalRevenue;
}
