package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String jwtToken;
    private Integer userId;
    private String username;
    private String userType;
    private String message; // Optional: for success/error messages
}