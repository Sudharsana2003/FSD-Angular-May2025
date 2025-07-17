package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // IMPORTANT: Ensure this import is present
import com.hexa.cozyhavenstay.model.User;
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    @Query("SELECT DISTINCT h.location FROM Hotel h")
    List<String> findAllDistinctLocations();

    @Query("SELECT DISTINCT h.location FROM Hotel h WHERE LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<String> findDistinctLocationsByLocationContainingIgnoreCase(@Param("location") String location);

    
    // Custom method to find hotels by owner
    List<Hotel> findByOwnerUser(User ownerUser);
    
    // Optional: Find by hotelName and location (if needed for uniqueness checks)
    Optional<Hotel> findByHotelNameAndLocation(String hotelName, String location);
    /**
     * Finds Hotels in a given location that have at least 'minRequiredRooms' rooms
     * which are NOT booked for the entire specified check-in and check-out dates.
     * This query also eagerly fetches hotel amenities and their amenity details
     * to avoid LazyInitializationException or ConcurrentModificationException.
     *
     * @param location The desired location (city/area).
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @param minRequiredRooms The minimum number of rooms required by the user.
     * @return A list of Hotel entities matching the criteria.
     */
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "LEFT JOIN FETCH h.hotelAmenities ha " +
           "LEFT JOIN FETCH ha.amenity " +
           "WHERE LOWER(h.location) = LOWER(:location) " +
           "AND h.hotelId IN ( " +
           "    SELECT r.hotel.hotelId FROM Room r " +
           "    WHERE r.hotel.hotelId = h.hotelId AND r.roomId NOT IN ( " +
           "        SELECT brd.room.roomId FROM BookedRoomDetail brd JOIN brd.booking b " +
           "        WHERE b.bookingStatus IN ('CONFIRMED', 'PENDING_PAYMENT') " +
           "        AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate " +
           "    ) " +
           "    GROUP BY r.hotel.hotelId HAVING COUNT(r.roomId) >= :minRequiredRooms " +
           ")")
    List<Hotel> findAvailableHotelsByLocationAndDates(
            @Param("location") String location,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("minRequiredRooms") Integer minRequiredRooms);

    // --- NEW METHOD FOR ENDPOINT #3 ---
    /**
     * Custom query to fetch Hotel details along with its associated amenities
     * This uses a JOIN FETCH to bring amenities in one query, avoiding N+1 problems.
     *
     * @param hotelId The ID of the hotel to fetch.
     * @return An Optional containing the Hotel with amenities, or empty if not found.
     */
    @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.hotelAmenities ha LEFT JOIN FETCH ha.amenity WHERE h.hotelId = :hotelId")
    Optional<Hotel> findByIdWithAmenities(@Param("hotelId") Integer hotelId);
    // ------------------------------------
}