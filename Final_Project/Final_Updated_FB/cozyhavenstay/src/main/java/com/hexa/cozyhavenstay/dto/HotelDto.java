package com.hexa.cozyhavenstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {
    private Integer hotelId;
    private String hotelName;
    private String location;
    private String address;
    private String description;
    private String contactCountryCode;
    private String contactLocalPhoneNumber;
    private String contactEmail;
    private Boolean isActive;
    private Double averageRating; // NEW: Average rating for the hotel
    private Double minPricePerNight; // NEW: Minimum price among available rooms
    private List<String> amenities; // List of amenity names for the hotel
    // You can add more fields from the Hotel entity if needed, like rating, image URL, etc.
}