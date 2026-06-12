package com.example.project.controller;

import com.example.project.dto.request.CreateUserRequest;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.AuthResponse;
import com.example.project.dto.response.CourtImageResponse;
import com.example.project.dto.response.UserResponse;
import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import com.example.project.service.AuthService;
import com.example.project.service.CourtService;
import com.example.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerUnitTest {

    @Test
    void registerReturnsCreated() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        RegisterRequest request = RegisterRequest.builder().username("john").build();
        when(authService.register(request)).thenReturn(AuthResponse.builder().accessToken("token").build());

        ResponseEntity<ApiResponse<AuthResponse>> response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("token", response.getBody().getData().getAccessToken());
    }

    @Test
    void loginReturnsOk() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        LoginRequest request = new LoginRequest();
        when(authService.login(request)).thenReturn(AuthResponse.builder().accessToken("token").build());

        ResponseEntity<ApiResponse<AuthResponse>> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void refreshUsesRefreshTokenHeaderValue() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        when(authService.refreshToken("refresh")).thenReturn(AuthResponse.builder().refreshToken("new-refresh").build());

        ResponseEntity<ApiResponse<AuthResponse>> response = controller.refresh("refresh");

        verify(authService).refreshToken("refresh");
        assertEquals("new-refresh", response.getBody().getData().getRefreshToken());
    }

    @Test
    void logoutRevokesBearerToken() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");

        ResponseEntity<ApiResponse<Void>> response = controller.logout(request);

        verify(authService).logout("access-token");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void adminCreateUserReturnsCreated() {
        UserService userService = mock(UserService.class);
        AdminUserController controller = new AdminUserController(userService);
        CreateUserRequest request = CreateUserRequest.builder()
                .username("manager")
                .role(Role.MANAGER)
                .build();
        when(userService.createUser(request)).thenReturn(UserResponse.builder()
                .username("manager")
                .role(Role.MANAGER)
                .status(UserStatus.ACTIVE)
                .build());

        ResponseEntity<ApiResponse<UserResponse>> response = controller.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Role.MANAGER, response.getBody().getData().getRole());
    }

    @Test
    void managerUploadCourtImagesReturnsAllUploadedImages() {
        CourtService courtService = mock(CourtService.class);
        ManagerCourtController controller = new ManagerCourtController(courtService);
        MockMultipartFile first = new MockMultipartFile("files", "a.png", "image/png", new byte[] {1});
        MockMultipartFile second = new MockMultipartFile("files", "b.png", "image/png", new byte[] {2});
        List<CourtImageResponse> images = List.of(
                CourtImageResponse.builder().id(1L).imageUrl("https://cdn/a.png").build(),
                CourtImageResponse.builder().id(2L).imageUrl("https://cdn/b.png").build()
        );
        when(courtService.uploadCourtImages(10L, List.of(first, second))).thenReturn(images);

        ResponseEntity<ApiResponse<List<CourtImageResponse>>> response =
                controller.uploadCourtImages(10L, List.of(first, second));

        assertEquals(2, response.getBody().getData().size());
        assertEquals("https://cdn/b.png", response.getBody().getData().get(1).getImageUrl());
    }
}
