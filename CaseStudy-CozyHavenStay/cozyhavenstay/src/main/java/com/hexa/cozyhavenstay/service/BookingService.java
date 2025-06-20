package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.BookingRequestDto;
import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    /**
     * Creates a new booking based on the provided request details.
     * This method will handle room availability checks, fare calculation,
     * and persist the booking and associated booked room details.
     *
     * @param requestDto The DTO containing booking details like user ID, hotel ID, room IDs, dates, and guest counts.
     * @return BookingResponseDto containing confirmation details of the created booking.
     */
    BookingResponseDto createBooking(BookingRequestDto requestDto);

    /**
     * Retrieves a specific booking by its ID.
     *
     * @param bookingId The ID of the booking to retrieve.
     * @return An Optional containing the Booking entity if found, otherwise empty.
     */
    Optional<Booking> getBookingById(Integer bookingId);

    /**
     * Retrieves all bookings made by a specific user.
     *
     * @param userId The ID of the user whose bookings are to be retrieved.
     * @return A list of Booking entities associated with the given user ID.
     */
    List<Booking> getBookingsByUserId(Integer userId);

    /**
     * Cancels a booking with the given ID.
     *
     * @param bookingId The ID of the booking to cancel.
     * @return BookingResponseDto reflecting the cancelled booking status.
     */
    BookingResponseDto cancelBooking(Integer bookingId);

    /**
     * Retrieves all bookings in the system. (Typically for admin purposes).
     *
     * @return A list of all Booking entities.
     */
    List<Booking> getAllBookings();

    /**
     * Retrieves the booking history for a specific user, returning a list of BookingResponseDto.
     * This method provides a summarized view suitable for API responses.
     *
     * @param userId The ID of the user whose booking history is to be retrieved.
     * @return A list of BookingResponseDto, summarizing the user's bookings.
     */
    List<BookingResponseDto> getBookingHistoryForUser(Integer userId);

    /**
     * Retrieves all bookings in the system, converting them into BookingResponseDto objects.
     * This is typically for administrative purposes, providing a comprehensive list of all bookings.
     * @return A list of BookingResponseDto, summarizing all bookings.
     */
    List<BookingResponseDto> getAllBookingsDetails();

    /**
     * Retrieves the detailed information for a single booking by its ID,
     * converted into a BookingResponseDto.
     *
     * @param bookingId The ID of the booking to retrieve.
     * @return BookingResponseDto containing the details of the requested booking.
     * @throws java.util.NoSuchElementException if the booking with the given ID is not found.
     */
    BookingResponseDto getBookingDetailsById(Integer bookingId);

    /**
     * Retrieves upcoming bookings for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of BookingResponseDto for upcoming bookings.
     */
    List<BookingResponseDto> getUpcomingBookingsForUser(Integer userId);

    /**
     * Retrieves past bookings for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of BookingResponseDto for past bookings.
     */
    List<BookingResponseDto> getPastBookingsForUser(Integer userId);

    // --- NEW METHOD TO ADD FOR HOTEL OWNER VIEW ---
    /**
     * Retrieves a list of bookings for a specific hotel, intended for the hotel owner.
     * @param hotelId The ID of the hotel.
     * @return A list of BookingResponseDto for bookings related to the specified hotel.
     */
    List<BookingResponseDto> listBookingsForHotelOwner(Integer hotelId); // <-- ADD THIS LINE
    
    BookingResponseDto approveRefund(Integer bookingId);
}