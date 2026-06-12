package com.example.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotPasswordResponse {

    private String message;
    private String resetToken;
}
