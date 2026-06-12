package com.example.project.service.impl;

import com.example.project.dto.request.UpdateUserRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import com.example.project.exception.ConflictException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // FR-05: Tìm kiếm + Phân trang
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        return userRepository.searchUsers(keyword, role, status, pageable)
                .map(this::toUserResponse);
    }

    // FR-05: Lấy user theo ID
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return toUserResponse(user);
    }

    // FR-05: Cập nhật user
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        // Kiểm tra email trùng (nếu thay đổi)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email '" + request.getEmail() + "' is already in use");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật các field nếu không null
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        User savedUser = userRepository.save(user);
        log.info("User updated: {}", savedUser.getUsername());

        return toUserResponse(savedUser);
    }

    // FR-05: Xóa user
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        log.info("User deleted: {}", user.getUsername());
    }

    // ===== Helpers =====

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // UC-02: Entity -> DTO mapping
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

