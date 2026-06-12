package com.example.project.dto.request;

import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Email(message = "Email invalid format")
    private String email;

    @Pattern(regexp = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$", message = "Phone number is invalid")
    private String phoneNumber;

    private Role role;

    private UserStatus status;
}
