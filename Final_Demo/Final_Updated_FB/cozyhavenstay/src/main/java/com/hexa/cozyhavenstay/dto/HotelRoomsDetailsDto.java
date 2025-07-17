// src/main/java/com/hexa/cozyhavenstay/dto/HotelRoomsDetailsDto.java
package com.hexa.cozyhavenstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomsDetailsDto {
    private HotelDetailsDto hotel; // <-- CHANGED: This should be HotelDetailsDto to match the service
    private List<RoomAvailabilityDto> availableRooms; // List of available rooms with calculated fares
}