package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Integer bookingId;
    private Integer hotelId;
    private String hotelName;
    private String userName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalFare;
    private String bookingStatus;
    private LocalDateTime bookingDate;
    private List<String> bookedRoomNumbersAndTypes;
    private String refundStatus;

    // ⭐ NEW FIELD ADDED ⭐
    private BigDecimal refundAmount; // This field is needed for the refund logic

    private Integer numberOfAdults;
    private Integer numberOfChildren;

    private String razorpayOrderId; // Add this field

}