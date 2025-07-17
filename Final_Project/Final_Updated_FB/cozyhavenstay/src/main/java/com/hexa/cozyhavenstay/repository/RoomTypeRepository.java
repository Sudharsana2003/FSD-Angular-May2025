package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {
    // Add custom query methods if needed
}