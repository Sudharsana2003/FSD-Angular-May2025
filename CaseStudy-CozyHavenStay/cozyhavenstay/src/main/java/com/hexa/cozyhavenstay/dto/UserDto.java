package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer userId; // Only for response, not for registration input

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
             message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and no whitespace")
    private String password;

    @NotBlank(message = "First name cannot be empty")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty") // Ensure this matches your validation for lastName
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    private String fullName; // This is often derived, make sure not marked @NotBlank if derived.

    @Size(max = 20, message = "Gender cannot exceed 20 characters")
    private String gender;

    @NotBlank(message = "Country code cannot be empty")
    @Size(max = 10, message = "Country code cannot exceed 10 characters")
    private String countryCode;

    // --- CHANGE STARTS HERE ---
    @NotBlank(message = "Local phone number cannot be empty")
    @Size(max = 20, message = "Local phone number cannot exceed 20 characters")
    private String localPhoneNumber; // Renamed from phoneNumber
    // --- CHANGE ENDS HERE ---

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "User type cannot be empty")
    @Pattern(regexp = "GUEST|HOTEL_OWNER|ADMIN", message = "User type must be GUEST, HOTEL_OWNER, or ADMIN")
    private String userType;

    private LocalDateTime registrationDate; // For response/internal use
    private LocalDateTime lastLoginDate; // For response/internal use
    private Boolean isActive; // For response/internal use
}