// src/main/java/com/hexa/cozyhavenstay/service/BookingService.java
package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.BookingRequestDto;
import com.hexa.cozyhavenstay.dto.BookingResponseDto;
import com.hexa.cozyhavenstay.model.Booking;
import com.hexa.cozyhavenstay.exception.WalletException; // NEW IMPORT: You need to create this exception

import java.util.List;
import java.util.Optional;

public interface BookingService {

    // ⭐ RENAMED & MODIFIED: Reflects wallet deduction and throws WalletException ⭐
    BookingResponseDto createBookingAndDeductWallet(BookingRequestDto requestDto) throws WalletException;

    Optional<Booking> getBookingById(Integer bookingId);

    List<Booking> getBookingsByUserId(Integer userId);

    // MODIFIED: No change in signature, but implementation will handle refund
    BookingResponseDto cancelBooking(Integer bookingId);

    List<Booking> getAllBookings();

    List<BookingResponseDto> getBookingsByStatus(String status);

    List<BookingResponseDto> getAllBookingsDetails();

    List<BookingResponseDto> getBookingHistoryForUser(Integer userId, String status, Double minFare, Double maxFare);

    List<BookingResponseDto> getAllBookingsDetailsFiltered(String status, Double minFare, Double maxFare);

    BookingResponseDto getBookingDetailsById(Integer bookingId);

    List<BookingResponseDto> getUpcomingBookingsForUser(Integer userId);

    List<BookingResponseDto> getPastBookingsForUser(Integer userId);

    List<BookingResponseDto> listBookingsForHotelOwner(Integer hotelId);

    // MODIFIED: Implementation will handle refund by adding to balance
    BookingResponseDto approveRefund(Integer bookingId);

    BookingResponseDto updateBookingStatus(Integer bookingId, String newStatus);

    void deleteBooking(Integer bookingId);

    List<BookingResponseDto> getAllBookingsForUser(Integer userId);

    List<BookingResponseDto> getBookingsEligibleForReview(Integer userId);
}