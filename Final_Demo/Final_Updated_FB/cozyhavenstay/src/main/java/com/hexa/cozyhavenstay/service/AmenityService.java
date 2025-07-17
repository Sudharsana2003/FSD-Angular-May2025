package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.AmenityDto;
import java.util.List;

public interface AmenityService {
    List<AmenityDto> getAllAmenities();
    // No getAmenityById needed for public Browse typically, but you can add it.
}