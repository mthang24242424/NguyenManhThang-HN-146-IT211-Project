package com.example.project.service.impl;

import com.example.project.dto.request.ChangePasswordRequest;
import com.example.project.dto.request.ForgotPasswordRequest;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.request.ResetPasswordRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.dto.response.ForgotPasswordResponse;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.PasswordResetToken;
import com.example.project.entity.TokenBlacklist;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import com.example.project.exception.ConflictException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.TokenBlacklistRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtService;
import com.example.project.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ===== FR-04: Đăng ký =====
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra trùng username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' already exists");
        }

        // Kiểm tra trùng email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' already registered");
        }

        // Tạo user mới (mặc định là CUSTOMER)
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt strength 10
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Trả về token ngay sau khi đăng ký
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===== UC-01: Đăng nhập =====
    @Override
    public AuthResponse login(LoginRequest request) {
        // Spring Security sẽ throw BadCredentialsException nếu sai
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in: {}", user.getUsername());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===== Refresh Token =====
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("Refresh token expired, please login again");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    // ===== UC-03: Đăng xuất - Blacklist token =====
    @Override
    @Transactional
    public void logout(String token) {
        if (tokenBlacklistRepository.existsByToken(token)) {
            return; // Already revoked
        }

        long remainingMs = jwtService.getRemainingTime(token);
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusNanos(remainingMs * 1_000_000);

        TokenBlacklist blacklisted = TokenBlacklist.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();

        tokenBlacklistRepository.save(blacklisted);
        log.info("Token revoked successfully");
    }

    // ===== FR-10: Đổi mật khẩu =====
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", username);
    }

    // ===== FR-10: Quên mật khẩu =====
    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with email not found"));

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated for: {}", user.getEmail());

        return ForgotPasswordResponse.builder()
                .message("Password reset token generated. Use it within 1 hour.")
                .resetToken(token)
                .build();
    }

    // ===== FR-10: Đặt lại mật khẩu bằng token =====
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        log.info("Password reset successfully for: {}", user.getUsername());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }
}

