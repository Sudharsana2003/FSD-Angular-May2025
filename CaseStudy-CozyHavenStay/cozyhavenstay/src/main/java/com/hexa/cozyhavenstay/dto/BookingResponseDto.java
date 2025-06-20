// src/main/java/com/hexa/cozyhavenstay/dto/BookingResponseDto.java
package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List; // If you want to list booked room numbers/types

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Integer bookingId; // The ID of the newly created booking
    private String hotelName;
    private String userName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalFare;
    private String bookingStatus;
    private LocalDateTime bookingDate;
    private List<String> bookedRoomNumbersAndTypes; // E.g., ["Room 101 (Deluxe)", "Room 205 (Suite)"]
    private String refundStatus;
    // Add any other relevant confirmation details
}