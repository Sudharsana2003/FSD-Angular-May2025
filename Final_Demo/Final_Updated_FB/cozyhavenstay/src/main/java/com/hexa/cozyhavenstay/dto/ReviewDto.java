package com.hexa.cozyhavenstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Integer reviewId;
    private Integer hotelId; // To link back to the hotel
    private String userName; // Name of the user who posted the review (for public display)
    private Byte rating;
    private String commentText;
    private LocalDateTime reviewDate;
    // We don't expose 'isActive' for public viewing, only active reviews are fetched.
}