package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Review;
import com.hexa.cozyhavenstay.model.Hotel;
import com.hexa.cozyhavenstay.model.User; // Ensure User is imported
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * Calculates the average rating for a given hotel.
     *
     * @param hotelId The ID of the hotel.
     * @return An Optional containing the average rating (as a Double), or empty if no reviews exist.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.hotelId = :hotelId")
    Optional<Double> findAverageRatingByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * Finds all active reviews for a specific hotel, ordered by review date descending.
     * This is intended for public display.
     *
     * @param hotel The Hotel entity for which to find reviews.
     * @return A list of active reviews for the specified hotel.
     */
    List<Review> findByHotelAndIsActiveTrueOrderByReviewDateDesc(Hotel hotel);

    // --- ADD THIS NEW METHOD ---
    /**
     * Finds all reviews by a specific user's ID, ordered by review date descending.
     *
     * @param userId The ID of the user.
     * @return A list of reviews submitted by the specified user.
     */
    List<Review> findByUserUserIdOrderByReviewDateDesc(Integer userId); // Added this line

    // --- OPTIONAL: Add this if you want to prevent multiple reviews per booking per user ---
    /**
     * Finds a review by booking ID and user ID.
     * Useful for checking if a review already exists for a specific booking by a specific user.
     *
     * @param bookingId The ID of the booking.
     * @param userId The ID of the user.
     * @return An Optional containing the review if found, otherwise empty.
     */
    Optional<Review> findByBookingBookingIdAndUserUserId(Integer bookingId, Integer userId);
    // --------------------------------------------------------------------------------------
}