package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Integer> {
    // Add custom query methods if needed
}