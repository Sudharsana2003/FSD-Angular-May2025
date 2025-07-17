// src/main/java/com/hexa/cozyhavenstay/dto/HotelSearchResponseDto.java
package com.hexa.cozyhavenstay.dto;

import lombok.Data; // For Lombok's @Data annotation
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-argument constructor
public class HotelSearchResponseDto {
    private int id;
    private String name;
    private String location; // The city/area
    private String address;
    private String description;
    private Double rating; // You might want to add a rating system later
    private String imageUrl; // URL for the main image of the hotel
    private Integer availableRoomsCount; // Number of rooms available for the searched criteria
    private Double minFarePerNight; // The lowest fare of any available room type for the period
    private List<String> amenities; // List of amenities (e.g., "WiFi", "Parking", "Pool")
}