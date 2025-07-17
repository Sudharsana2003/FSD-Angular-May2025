package com.hexa.cozyhavenstay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HotelRequest {

    @NotBlank(message = "Hotel name cannot be blank")
    @Size(max = 255, message = "Hotel name cannot exceed 255 characters")
    private String hotelName;

    @NotBlank(message = "Location cannot be blank")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Size(max = 65535, message = "Description is too long") // LONGTEXT can be very long
    private String description;

    @NotBlank(message = "Contact country code cannot be blank")
    @Size(max = 5, message = "Contact country code cannot exceed 5 characters")
    private String contactCountryCode;

    @NotBlank(message = "Contact local phone number cannot be blank")
    @Size(max = 20, message = "Contact local phone number cannot exceed 20 characters")
    private String contactLocalPhoneNumber;

    @Email(message = "Invalid contact email format")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    private String contactEmail;

    // isActive is typically managed by the system, not directly in request for creation/update
    // For update, you might allow it if an owner can toggle their hotel's active status.
    private Boolean isActive;
}