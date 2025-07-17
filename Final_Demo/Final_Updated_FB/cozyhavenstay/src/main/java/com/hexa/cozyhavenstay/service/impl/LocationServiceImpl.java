// src/main/java/com/hexa/cozyhavenstay/service/impl/LocationServiceImpl.java
package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.repository.HotelRepository;
import com.hexa.cozyhavenstay.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final HotelRepository hotelRepository;

    public LocationServiceImpl(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    public List<String> getAllLocations() {
        // This method simply returns all distinct locations directly from the repository
        return hotelRepository.findAllDistinctLocations().stream()
                .sorted() // Sort for consistent ordering
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSuggestedLocations(String query) {
        List<String> suggestedLocations;

        if (query == null || query.trim().isEmpty()) {
            // If the query is empty, return all distinct location names (same as getAllLocations)
            suggestedLocations = hotelRepository.findAllDistinctLocations();
        } else {
            // Otherwise, return distinct location names that contain the query (case-insensitive)
            suggestedLocations = hotelRepository.findDistinctLocationsByLocationContainingIgnoreCase(query);
        }

        // Sort the suggested locations alphabetically for better user experience
        return suggestedLocations.stream()
                .sorted()
                .collect(Collectors.toList());
    }
}