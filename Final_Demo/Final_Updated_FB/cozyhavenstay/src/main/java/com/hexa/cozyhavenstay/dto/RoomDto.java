package com.hexa.cozyhavenstay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // Keep BigDecimal for fare/size consistency
// import java.time.LocalDateTime; // Only if you want to expose timestamps in DTO

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Integer roomId;
    private Integer hotelId; // Assuming you want hotelId in the DTO
    private String roomNumber;
    private RoomTypeDto roomType; // Nested DTO for room type details
    private BigDecimal roomSizeSqm;
    private BigDecimal roomSizeSqft;
    private String bedPreference;

    // --- CHANGE THIS: from Byte to Integer ---
    private Integer maxPeople; // Changed from Byte to Integer
    // ----------------------------------------

    private BigDecimal baseFarePerNight;
    private Boolean isAc;
    private Boolean isAvailable;

    // You might or might not want to include createdAt/updatedAt in the DTO for response
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
}