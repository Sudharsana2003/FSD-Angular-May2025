// src/main/java/com/hexa/cozyhavenstay/dto/HotelDetailsDto.java
package com.hexa.cozyhavenstay.dto;

import lombok.Data; // Make sure you have Lombok set up in your project
import java.util.List;

@Data // This Lombok annotation generates all getters, setters, equals, hashCode, and toString
public class HotelDetailsDto {
    private Integer hotelId;
    private String hotelName;
    private String location;
    private String address;
    private String description;
    private String contactCountryCode;      // <-- ADD THIS FIELD
    private String contactLocalPhoneNumber; // <-- ADD THIS FIELD
    private String contactEmail;            // <-- ADD THIS FIELD
    private Boolean isActive;               // <-- ADD THIS FIELD
    private List<String> amenities;
    private Double averageRating;           // <-- ENSURE THIS IS Double (wrapper class)
    private Double minPricePerNight;        // <-- ENSURE THIS IS Double (wrapper class)
    // Add any other fields you want to expose for a detailed view
}