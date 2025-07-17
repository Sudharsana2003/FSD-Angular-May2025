package com.hexa.cozyhavenstay.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

    @NotNull(message = "Hotel ID cannot be null")
    private Integer hotelId; // To link review to a hotel

    @NotNull(message = "Booking ID cannot be null, review must be associated with a completed stay.")
    private Integer bookingId; // Link to the specific booking that this review is for

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Byte rating; // Matches 'Byte' type in your Review entity

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentText; // Matches 'commentText' in your Review entity
}