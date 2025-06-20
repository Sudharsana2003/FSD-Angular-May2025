package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Integer userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String gender;
    private String countryCode;

    // --- THIS IS THE CRITICAL FIELD ---
    private String localPhoneNumber; // This field must be present
    // --- END CRITICAL FIELD ---

    private String address;
    private String userType; // This will map to RoleType.name()
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    private Boolean isActive;

    // This field is typically only for login response, not registration success response.
    private String jwtToken; // Optional: For login responses
    private String message; // Optional: For general messages

    // Constructor for login response specifically (example)
    public UserResponseDto(String jwtToken, Integer userId, String username, String userType, String message) {
        this.jwtToken = jwtToken;
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.message = message;
    }
}