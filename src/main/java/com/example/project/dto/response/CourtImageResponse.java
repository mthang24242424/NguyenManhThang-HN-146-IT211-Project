package com.example.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CourtImageResponse {

    private Long id;
    private String imageUrl;
    private LocalDateTime uploadedAt;
}
