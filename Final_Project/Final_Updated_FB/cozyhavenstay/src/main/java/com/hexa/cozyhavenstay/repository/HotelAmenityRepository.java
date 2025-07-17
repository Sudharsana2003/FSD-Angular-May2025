package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.HotelAmenity;
import com.hexa.cozyhavenstay.model.HotelAmenityId; // Import the composite ID class
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, HotelAmenityId> {
    // Add custom query methods if needed, e.g., to find all amenities for a hotel
    // List<HotelAmenity> findByIdHotelId(Integer hotelId);
}