package com.hexa.cozyhavenstay.service.impl;

import com.hexa.cozyhavenstay.dto.AmenityDto;
import com.hexa.cozyhavenstay.model.Amenity;
import com.hexa.cozyhavenstay.repository.AmenityRepository;
import com.hexa.cozyhavenstay.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmenityServiceImpl implements AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

    // Helper method to map Amenity entity to AmenityDto
    private AmenityDto mapToAmenityDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setAmenityId(amenity.getAmenityId());
        amenityDto.setAmenityName(amenity.getAmenityName());
        amenityDto.setDescription(amenity.getDescription());
        return amenityDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityDto> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(this::mapToAmenityDto)
                .collect(Collectors.toList());
    }
}