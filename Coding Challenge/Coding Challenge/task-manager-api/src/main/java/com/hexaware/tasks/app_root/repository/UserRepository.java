package com.hexaware.tasks.app_root.repository; // CORRECTED package declaration

import com.hexaware.tasks.app_root.entity.User; // CORRECTED import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}