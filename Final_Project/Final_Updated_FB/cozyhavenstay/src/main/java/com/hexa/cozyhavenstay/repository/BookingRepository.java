// src/main/java/com/hexa/cozyhavenstay/repository/BookingRepository.java
package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByUserUserId(Integer userId);

    List<Booking> findByHotelHotelId(Integer hotelId);

    // ⭐ CONFIRMED: This method is correctly present and will be used ⭐
    Optional<Booking> findByPaymentIdReference(String paymentIdReference);

    List<Booking> findByUserUserIdOrderByBookingDateDesc(Integer userId);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.checkOutDate > :currentDate " +
            "AND b.bookingStatus NOT IN ('CANCELLED', 'COMPLETED', 'REFUNDED') " +
            "ORDER BY b.checkInDate ASC")
    List<Booking> findUpcomingBookingsForUser(@Param("userId") Integer userId,
                                             @Param("currentDate") LocalDate currentDate);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND (b.checkOutDate <= :currentDate OR b.bookingStatus IN ('CANCELLED', 'COMPLETED', 'REFUNDED')) " +
            "ORDER BY b.checkInDate DESC")
    List<Booking> findPastBookingsForUser(@Param("userId") Integer userId,
                                         @Param("currentDate") LocalDate currentDate);

    List<Booking> findByUserUserIdAndBookingStatusIgnoreCase(Integer userId, String bookingStatus);

    List<Booking> findByBookingStatusIgnoreCase(String bookingStatus);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN Review r ON b.bookingId = r.booking.bookingId " +
            "WHERE b.user.userId = :userId " +
            "AND b.bookingStatus = 'COMPLETED' " +
            "AND b.checkOutDate <= CURRENT_DATE " +
            "AND r.reviewId IS NULL")
    List<Booking> findCompletedBookingsNotReviewedByUser(@Param("userId") Integer userId);
}