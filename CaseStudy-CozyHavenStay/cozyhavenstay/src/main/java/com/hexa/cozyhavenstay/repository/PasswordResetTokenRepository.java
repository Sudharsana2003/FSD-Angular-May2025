// src/main/java/com/hexa/cozyhavenstay/repository/PasswordResetTokenRepository.java
package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.PasswordResetToken;
import com.hexa.cozyhavenstay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user); // To check if a user already has an active token
}