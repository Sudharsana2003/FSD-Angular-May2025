package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // --- CHANGE STARTS HERE ---
    Optional<User> findByLocalPhoneNumber(String localPhoneNumber); // Changed from findByPhoneNumber
    // --- CHANGE ENDS HERE ---

    List<User> findByIsActiveTrue(); // Custom method to fetch active users
}