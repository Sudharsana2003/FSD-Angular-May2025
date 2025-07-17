package com.hexa.cozyhavenstay.repository;

import com.hexa.cozyhavenstay.model.BookedRoomDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookedRoomDetailRepository extends JpaRepository<BookedRoomDetail, Integer> {

    /**
     * Custom query to find booked rooms for a specific hotel and date range.
     * A room is considered booked if its associated booking overlaps with the requested check-in/check-out dates.
     * It specifically checks for 'CONFIRMED' or 'PENDING_PAYMENT' booking statuses.
     *
     * @param hotelId The ID of the hotel.
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @return A list of BookedRoomDetail entities for rooms booked within the specified period.
     */
    @Query("SELECT brd FROM BookedRoomDetail brd " +
           "JOIN brd.booking b " +
           "WHERE brd.hotel.hotelId = :hotelId " + // Assumes BookedRoomDetail has a direct 'hotel' association
           "AND b.bookingStatus IN ('CONFIRMED', 'PENDING_PAYMENT') " + // Only consider confirmed/pending bookings
           "AND (" +
           "    (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)" + // Standard overlap condition
           ")")
    List<BookedRoomDetail> findBookedRoomsForHotelAndDateRange(
            @Param("hotelId") Integer hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    /**
     * Checks if a specific room (by room ID) is booked for any part of the given date range.
     * Considers bookings with 'CONFIRMED' or 'PENDING_PAYMENT' status.
     *
     * @param roomId The ID of the room to check.
     * @param checkInDate The desired check-in date.
     * @param checkOutDate The desired check-out date.
     * @return true if the room is booked for any overlapping dates, false otherwise.
     */
    @Query("SELECT COUNT(brd) > 0 FROM BookedRoomDetail brd " +
           "JOIN brd.booking b " +
           "WHERE brd.room.roomId = :roomId AND " +
           "b.bookingStatus IN ('CONFIRMED', 'PENDING_PAYMENT') AND " + // Only consider confirmed/pending bookings
           "((b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate))") // Standard overlap condition
    boolean isRoomBookedForDateRange(@Param("roomId") Integer roomId,
                                     @Param("checkInDate") LocalDate checkInDate,
                                     @Param("checkOutDate") LocalDate checkOutDate);
    
    // You might also need methods to find booked details for a specific booking ID
    List<BookedRoomDetail> findByBookingBookingId(Integer bookingId);
}