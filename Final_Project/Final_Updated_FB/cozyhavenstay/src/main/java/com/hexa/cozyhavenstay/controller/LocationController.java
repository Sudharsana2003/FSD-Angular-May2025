package com.hexa.cozyhavenstay.controller;

import com.hexa.cozyhavenstay.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController // Marks this class as a REST Controller
@RequestMapping("/api/locations") // Base path for all endpoints in this controller
public class LocationController {

    private final LocationService locationService;

    // Constructor injection for LocationService
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Endpoint to provide auto-suggestions for hotel locations.
     * Accessible via GET /api/locations/suggest. Configured as permitAll() in SecurityConfig.
     * This also covers the functionality previously in HotelController.
     * @param query The partial or full location name entered by the user.
     * @return A list of matching location names.
     */
    @GetMapping("/suggest") // Maps GET requests to /api/locations/suggest
    // No @PreAuthorize needed. Configured as permitAll() in SecurityConfig.
    public ResponseEntity<List<String>> suggestLocations(
            @RequestParam(required = false) String query) {
        List<String> suggestions;
        if (query == null || query.trim().isEmpty()) {
            suggestions = locationService.getAllLocations();
        } else {
            suggestions = locationService.getSuggestedLocations(query);
        }
        return ResponseEntity.ok(suggestions);
    }
}