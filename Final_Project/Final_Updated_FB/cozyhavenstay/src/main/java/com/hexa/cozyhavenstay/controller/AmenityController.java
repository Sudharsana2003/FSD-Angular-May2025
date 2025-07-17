package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.dto.AmenityDto;
import com.hexa.cozyhavenstay.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/amenities") // Base path for amenity-related endpoints
public class AmenityController {

    private final AmenityService amenityService;

    @Autowired // Use constructor injection
    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    // Endpoint to get all amenities for public view
    // GET /api/amenities
    @GetMapping
    // No @PreAuthorize needed here. This endpoint will be permitAll() configured in SecurityConfig.
    public ResponseEntity<List<AmenityDto>> getAllAmenities() {
        try {
            List<AmenityDto> amenities = amenityService.getAllAmenities();
            if (amenities.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Or HttpStatus.OK with empty list
            }
            return ResponseEntity.ok(amenities);
        } catch (Exception e) {
            // Log the exception for debugging: log.error("Error fetching all amenities", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}