package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Booking;
// import com.hexa.cozyhavenstay.model.User; // Only needed if passing User object directly, which we're not for new queries
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // NEW IMPORT: For @Query annotation
import org.springframework.data.repository.query.Param; // NEW IMPORT: For @Param annotation
import org.springframework.stereotype.Repository;

import java.time.LocalDate; // NEW IMPORT: For LocalDate parameters
import java.util.List;
import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // JpaRepository provides methods like save(), findById(), findAll(), deleteById() by default.

    // Find bookings by a specific user ID
    List<Booking> findByUserUserId(Integer userId);

    // Find bookings for a specific hotel ID
    List<Booking> findByHotelHotelId(Integer hotelId);

    // Find a booking by its payment ID reference
    Optional<Booking> findByPaymentIdReference(String paymentIdReference);

    // NEW: Find bookings by user ID, ordered by booking date descending (for history)
    List<Booking> findByUserUserIdOrderByBookingDateDesc(Integer userId);

    // NEW: Custom query for upcoming bookings for a user
    // Filters bookings where check-out date is in the future relative to currentDate
    // and status is not CANCELLED or COMPLETED, ordered by check-in date ascending.
    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
           "AND b.checkOutDate > :currentDate " +
           "AND b.bookingStatus NOT IN ('CANCELLED', 'COMPLETED') " +
           "ORDER BY b.checkInDate ASC")
    List<Booking> findUpcomingBookingsForUser(@Param("userId") Integer userId,
                                              @Param("currentDate") LocalDate currentDate);

    // NEW: Custom query for past bookings for a user
    // Filters bookings where check-out date is in the past relative to currentDate
    // OR status is CANCELLED or COMPLETED, ordered by check-in date descending.
    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
           "AND (b.checkOutDate <= :currentDate OR b.bookingStatus IN ('CANCELLED', 'COMPLETED')) " +
           "ORDER BY b.checkInDate DESC")
    List<Booking> findPastBookingsForUser(@Param("userId") Integer userId,
                                          @Param("currentDate") LocalDate currentDate);
    
    // --- CORRECTED METHOD FOR HOTEL OWNER VIEW ---
    /**
     * Finds all bookings directly associated with a specific hotel.
     * This relies on a direct ManyToOne relationship from Booking to Hotel.
     * @param hotelId The ID of the hotel.
     * @return A list of bookings for the specified hotel.
     */
    List<Booking> findByHotel_HotelId(Integer hotelId); // <--- CHANGED THIS LINE
    // 

    // You might still keep these if other parts of your application need more generic date range queries,
    // but the two @Query methods above are specifically for the "upcoming" and "past" logic.
    // List<Booking> findByUserAndCheckInDateAfterOrEqualOrderByCheckInDateAsc(User user, LocalDate date);
    // List<Booking> findByUserAndCheckOutDateBeforeOrderByCheckOutDateDesc(User user, LocalDate date);
}