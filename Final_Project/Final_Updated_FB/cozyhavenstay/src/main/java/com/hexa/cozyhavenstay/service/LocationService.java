// src/main/java/com/hexa/cozyhavenstay/service/LocationService.java
package com.hexa.cozyhavenstay.service;

import java.util.List;

public interface LocationService {
    // This method gets all unique locations from the database
    List<String> getAllLocations();

    // This method gets locations that match a given query
    List<String> getSuggestedLocations(String query);

    // You might have other location-related methods here if needed
}