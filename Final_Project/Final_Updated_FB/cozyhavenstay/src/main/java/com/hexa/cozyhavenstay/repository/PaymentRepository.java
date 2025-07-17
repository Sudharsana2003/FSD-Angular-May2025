// package com.hexa.cozyhavenstay.repository;

package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // You can add custom query methods here if needed, e.g., find all payments for
    // a booking
    // List<Payment> findByBooking(Booking booking);
}