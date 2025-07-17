package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List; // IMPORTANT: Ensure this import is present
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> { // Changed to Integer for RoomId

    // Count all rooms for a specific hotel
    long countByHotelHotelId(Integer hotelId); // Using standard JPA naming convention for nested properties

    /**
     * Counts the number of rooms in a given hotel that are booked for any part of the specified date range.
     * This query is more robust than relying on Room.isAvailable flag, as availability changes with dates.
     *
     * @param hotelId The ID of the hotel.
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @return The count of rooms booked for the specified dates in that hotel.
     */
    @Query("SELECT COUNT(DISTINCT r.roomId) FROM Room r JOIN r.bookedRoomDetails brd JOIN brd.booking b " +
           "WHERE r.hotel.hotelId = :hotelId " +
           "AND b.bookingStatus IN ('CONFIRMED', 'PENDING_PAYMENT') " +
           "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    Integer countBookedRoomsForHotelAndDates(@Param("hotelId") Integer hotelId,
                                              @Param("checkInDate") LocalDate checkInDate,
                                              @Param("checkOutDate") LocalDate checkOutDate);


    /**
     * Finds the minimum base fare among rooms of a specific hotel that are available for the given date range.
     *
     * @param hotelId The ID of the hotel.
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @return An Optional containing the minimum base fare, or empty if no rooms are available.
     */
    @Query("SELECT MIN(r.baseFarePerNight) FROM Room r " +
           "WHERE r.hotel.hotelId = :hotelId AND r.roomId NOT IN ( " +
           "    SELECT brd.room.roomId FROM BookedRoomDetail brd JOIN brd.booking b " +
           "    WHERE b.bookingStatus IN ('CONFIRMED', 'PENDING_PAYMENT') " +
           "    AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate " +
           ")")
    Optional<Double> findMinFareByHotelIdAndAvailability(@Param("hotelId") Integer hotelId,
                                                         @Param("checkInDate") LocalDate checkInDate,
                                                         @Param("checkOutDate") LocalDate checkOutDate);

    // --- NEW METHOD FOR ENDPOINT #3 ---
    /**
     * Custom query to fetch Rooms for a hotel along with their RoomType details.
     * This uses JOIN FETCH to bring RoomType in one query.
     * Filters for rooms marked as available in the Room entity itself.
     *
     * @param hotelId The ID of the hotel.
     * @return A list of Room entities associated with the given hotel and marked as available.
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.roomType WHERE r.hotel.hotelId = :hotelId AND r.isAvailable = true")
    List<Room> findByHotelIdAndIsAvailable(@Param("hotelId") Integer hotelId);
    // ------------------------------------
}