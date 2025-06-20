package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Add custom query methods if needed
}