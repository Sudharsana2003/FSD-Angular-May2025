package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private Integer reviewId;
    private Integer hotelId; // Added for convenience in response
    private String hotelName; // Derived from Hotel entity
    private Integer userId; // Added for convenience
    private String userName; // Derived from User entity
    private Integer bookingId; // Derived from Booking entity

    private Byte rating; // Matches 'Byte' type in your Review entity
    private String commentText; // Matches 'commentText' in your Review entity
    private LocalDateTime reviewDate;
    private Boolean isActive; // Status of the review

    // You might choose to include createdAt and updatedAt for admin views, but usually not for public display.
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
}