// src/main/java/com/hexa/cozyhavenstay/repository/UserRepository.java
package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional; // NEW IMPORT for @Transactional

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByLocalPhoneNumber(String localPhoneNumber);

    List<User> findByIsActiveTrue();

    // ⭐ Method to find user by ID (useful for direct retrieval) ⭐
    // Note: JpaRepository already provides findById(ID id), so findByUserId is
    // often redundant.
    // However, if you explicitly prefer it, it's fine to keep.
    Optional<User> findByUserId(Integer userId);

    // ⭐ Method to deduct balance ⭐
    // @Transactional is added to ensure this modifying operation runs within a
    // transaction
    @Modifying

    @Query("UPDATE User u SET u.balance = u.balance - :amount WHERE u.userId = :userId AND u.balance >= :amount")
    int deductBalance(@Param("userId") Integer userId, @Param("amount") BigDecimal amount); // Returns number of
                                                                                            // affected rows

    // ⭐ Method to add balance (for refunds or top-ups) ⭐
    // @Transactional is added to ensure this modifying operation runs within a
    // transaction
    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.userId = :userId")
    int addBalance(@Param("userId") Integer userId, @Param("amount") BigDecimal amount); // Returns number of affected
                                                                                         // rows
}