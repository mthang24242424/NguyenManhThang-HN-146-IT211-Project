package com.example.project.service;

import com.example.project.dto.request.ChangePasswordRequest;
import com.example.project.dto.request.ForgotPasswordRequest;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.request.ResetPasswordRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.dto.response.ForgotPasswordResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String token);

    void changePassword(String username, ChangePasswordRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
