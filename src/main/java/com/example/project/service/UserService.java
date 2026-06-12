package com.example.project.service;

import com.example.project.dto.request.CreateUserRequest;
import com.example.project.dto.request.UpdateUserRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    // FR-05: CRUD
    UserResponse createUser(CreateUserRequest request);

    Page<UserResponse> getAllUsers(String keyword, Role role, UserStatus status, Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}

