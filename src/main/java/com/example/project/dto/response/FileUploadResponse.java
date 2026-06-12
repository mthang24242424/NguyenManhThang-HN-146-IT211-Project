package com.example.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FileUploadResponse {

    private String imageUrl;
    private String publicId;
}
