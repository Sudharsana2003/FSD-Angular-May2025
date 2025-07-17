package com.hexa.cozyhavenstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityDto {
    private Integer roomId; // Corresponds to room.roomId
    private String roomNumber; // The specific room number (if applicable to display, otherwise remove)
    private String roomType; // e.g., "Standard", "Deluxe", "Suite"
    private String roomDescription; // From RoomType entity
    private BigDecimal roomSizeSqm;
    private BigDecimal roomSizeSqft;
    private String bedPreference; // e.g., "King Bed", "Twin Beds"
    private Integer maxPeople; // Max occupancy for this room
    private BigDecimal baseFarePerNight; // Base fare for this room type
    private BigDecimal calculatedTotalFareForStay; // Calculated fare for the entire stay, including extra person charges
    private Boolean isAvailable; // True if available for the given dates
    private Integer availableCount; // How many physical rooms of this type are available for the dates
    private Boolean isAc;
}