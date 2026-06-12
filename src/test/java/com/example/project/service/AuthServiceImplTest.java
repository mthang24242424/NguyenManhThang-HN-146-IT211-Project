package com.example.project.service;

import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import com.example.project.exception.ConflictException;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtService;
import com.example.project.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerCreatesCustomerAndTokens() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john")
                .password("secret1")
                .email("john@example.com")
                .fullName("John Doe")
                .phoneNumber("0912345678")
                .build();
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.CUSTOMER, userCaptor.getValue().getRole());
        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john")
                .password("secret1")
                .email("john@example.com")
                .fullName("John Doe")
                .build();
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void loginAuthenticatesAndReturnsTokens() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("secret1");
        User user = user("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshTokenRotatesRefreshToken() {
        User user = user("john");
        when(tokenBlacklistService.isRevoked("old-refresh")).thenReturn(false);
        when(jwtService.isRefreshToken("old-refresh")).thenReturn(true);
        when(jwtService.extractUsername("old-refresh")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.isTokenExpired("old-refresh")).thenReturn(false);
        when(jwtService.getRemainingTime("old-refresh")).thenReturn(60_000L);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");

        AuthResponse response = authService.refreshToken("old-refresh");

        verify(tokenBlacklistService).revoke("old-refresh", 60_000L);
        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        assertFalse(response.getRefreshToken().equals("old-refresh"));
    }

    @Test
    void logoutRevokesTokenForRemainingTime() {
        when(jwtService.getRemainingTime("access-token")).thenReturn(30_000L);

        authService.logout("access-token");

        verify(tokenBlacklistService).revoke("access-token", 30_000L);
    }

    private User user(String username) {
        return User.builder()
                .id(1L)
                .username(username)
                .password("encoded")
                .email(username + "@example.com")
                .fullName("John Doe")
                .phoneNumber("0912345678")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
