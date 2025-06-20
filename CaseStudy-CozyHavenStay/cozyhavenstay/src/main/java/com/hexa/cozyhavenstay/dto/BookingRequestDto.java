package com.hexa.cozyhavenstay.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty; // NEW IMPORT
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List; // NEW IMPORT

@Data
public class BookingRequestDto {

    @NotNull(message = "Hotel ID cannot be null")
    @Min(value = 1, message = "Hotel ID must be positive")
    private Integer hotelId;

    // --- CHANGE THIS FIELD ---
    @NotEmpty(message = "At least one room ID must be provided for the booking") // Use @NotEmpty for lists
    private List<Integer> roomIds; // Changed from Integer roomId

    @NotNull(message = "Check-in date cannot be null")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    @NotNull(message = "Number of adults cannot be null")
    @Min(value = 1, message = "Number of adults must be at least 1")
    private Integer numberOfAdults;

    @NotNull(message = "Number of children cannot be null")
    @Min(value = 0, message = "Number of children cannot be negative")
    private Integer numberOfChildren;
}