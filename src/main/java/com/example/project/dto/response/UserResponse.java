package com.example.project.dto.response;

import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
